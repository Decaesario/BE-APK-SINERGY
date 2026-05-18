package com.impal.gabungyuk.core.service;

import com.impal.gabungyuk.auth.entity.User;
import com.impal.gabungyuk.auth.respository.UserRepository;
import com.impal.gabungyuk.auth.service.FirebaseAuthService;
import com.impal.gabungyuk.core.security.BCrypt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class TokenService {

    private final String tokenSecret;
    private final FirebaseAuthService firebaseAuthService;
    private final UserRepository userRepository;
    @PersistenceContext
    private EntityManager entityManager;

    public TokenService(
            @Value("${app.auth.token-secret:change-this-secret-in-production}") String tokenSecret,
            FirebaseAuthService firebaseAuthService,
            UserRepository userRepository
    ) {
        this.tokenSecret = tokenSecret;
        this.firebaseAuthService = firebaseAuthService;
        this.userRepository = userRepository;
    }

    public String generateToken(Integer userId, long expiredAt) {
        String payload = userId + ":" + expiredAt;
        String signature = hmacSha256(payload);
        String rawToken = payload + ":" + signature;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(rawToken.getBytes(StandardCharsets.UTF_8));
    }

    public Integer extractUserIdFromAuthorizationHeader(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing Authorization header");
        }

        String token = authorizationHeader.trim();

        // Accept both "Bearer <token>" and raw token values to be more tolerant to clients
        if (token.regionMatches(true, 0, "Bearer ", 0, 7)) {
            token = token.substring(7).trim();
        }

        // strip possible surrounding quotes that some clients might include
        if ((token.startsWith("\"") && token.endsWith("\"")) || (token.startsWith("'") && token.endsWith("'"))) {
            token = token.substring(1, token.length() - 1).trim();
        }

        if (token.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing token");
        }

        // First try to parse as internal token
        try {
            return extractUserIdFromToken(token);
        } catch (ResponseStatusException e) {
            // fallthrough - try verifying as Firebase ID token
        }

        // Then treat token as Firebase ID token (verify signature + claims via Admin SDK)
        FirebaseAuthService.FirebaseIdentity identity = firebaseAuthService.verifyIdToken(token);
        return resolveOrCreateUserId(identity);
    }

    private Integer resolveOrCreateUserId(FirebaseAuthService.FirebaseIdentity identity) {
        User userByFirebaseUid = userRepository.findByFirebaseUid(identity.uid()).orElse(null);
        User userByEmail = userRepository.findByEmailIgnoreCase(identity.email()).orElse(null);

        if (userByFirebaseUid != null && userByEmail != null
                && !userByFirebaseUid.getIdPengguna().equals(userByEmail.getIdPengguna())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Google account conflicts with another local user");
        }

        if (userByFirebaseUid != null) {
            if (applyFirebaseProfile(userByFirebaseUid, identity)) {
                userByFirebaseUid = userRepository.save(userByFirebaseUid);
            }
            return userByFirebaseUid.getIdPengguna();
        }

        if (userByEmail != null) {
            if (userByEmail.getFirebaseUid() == null || userByEmail.getFirebaseUid().isBlank()) {
                userByEmail.setFirebaseUid(identity.uid());
                userByEmail.setProvider(mergeProvider(userByEmail.getProvider(), "google"));
                applyFirebaseProfile(userByEmail, identity);
                try {
                    User saved = userRepository.save(userByEmail);
                    return saved.getIdPengguna();
                } catch (DataIntegrityViolationException exception) {
                    User existing = recoverExistingUser(identity);
                    if (existing != null) {
                        return existing.getIdPengguna();
                    }
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "Data conflict while linking Firebase account: " + mostSpecificMessage(exception));
                }
            }
            if (!identity.uid().equals(userByEmail.getFirebaseUid())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Google account is already linked to another user");
            }
            if (applyFirebaseProfile(userByEmail, identity)) {
                userByEmail = userRepository.save(userByEmail);
            }
            return userByEmail.getIdPengguna();
        }

        User created = User.builder()
                .namaLengkap(resolveDisplayName(identity.name(), identity.email()))
                .email(identity.email())
                .password(buildFirebasePasswordPlaceholder(identity.uid()))
                .firebaseUid(identity.uid())
                .provider("google")
                .profilePicture(normalizeNullable(identity.picture()))
                .build();
        try {
            User saved = userRepository.save(created);
            return saved.getIdPengguna();
        } catch (DataIntegrityViolationException exception) {
            User existing = recoverExistingUser(identity);
            if (existing != null) {
                return existing.getIdPengguna();
            }
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Data conflict while creating local user: " + mostSpecificMessage(exception));
        }
    }

    private User recoverExistingUser(FirebaseAuthService.FirebaseIdentity identity) {
        entityManager.clear();
        User existingByFirebaseUid = userRepository.findByFirebaseUid(identity.uid()).orElse(null);
        if (existingByFirebaseUid != null) {
            return existingByFirebaseUid;
        }

        return userRepository.findByEmailIgnoreCase(identity.email()).orElse(null);
    }

    private String mostSpecificMessage(DataIntegrityViolationException exception) {
        Throwable root = exception.getMostSpecificCause();
        if (root == null || root.getMessage() == null || root.getMessage().isBlank()) {
            return "unknown constraint";
        }
        return root.getMessage().replace('\n', ' ').trim();
    }

    private String buildFirebasePasswordPlaceholder(String firebaseUid) {
        return BCrypt.hashpw("firebase:" + firebaseUid + ":" + System.nanoTime(), BCrypt.gensalt());
    }

    private boolean applyFirebaseProfile(User user, FirebaseAuthService.FirebaseIdentity identity) {
        boolean changed = false;

        String firebaseName = normalizeNullable(identity.name());
        if (firebaseName != null && !firebaseName.equals(user.getNamaLengkap())) {
            user.setNamaLengkap(firebaseName);
            changed = true;
        }

        String firebasePicture = normalizeNullable(identity.picture());
        if (firebasePicture != null && !firebasePicture.equals(user.getProfilePicture())) {
            user.setProfilePicture(firebasePicture);
            changed = true;
        }

        return changed;
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private Integer extractUserIdFromToken(String token) {
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);

            // parse token in a robust way: userId:expiredAt:signature
            int firstColon = decoded.indexOf(':');
            int lastColon = decoded.lastIndexOf(':');
            if (firstColon == -1 || lastColon == -1 || firstColon == lastColon) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
            }

            String userIdPart = decoded.substring(0, firstColon);
            String expiredAtPart = decoded.substring(firstColon + 1, lastColon);
            String signature = decoded.substring(lastColon + 1);

            int userId = Integer.parseInt(userIdPart);
            long expiredAt = Long.parseLong(expiredAtPart);

            String payload = userIdPart + ":" + expiredAtPart;
            String expectedSignature = hmacSha256(payload);

            if (!expectedSignature.equals(signature)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token signature");
            }

            if (System.currentTimeMillis() > expiredAt) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token has expired");
            }

            return userId;
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
    }

    private String hmacSha256(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(tokenSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(keySpec);
            byte[] hash = mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to sign token", exception);
        }
    }

    private String resolveDisplayName(String requestedName, String email) {
        if (requestedName != null && !requestedName.isBlank()) {
            return requestedName.trim();
        }

        int separator = email.indexOf('@');
        if (separator > 0) {
            return email.substring(0, separator);
        }

        return email;
    }

    private String mergeProvider(String currentProvider, String newProvider) {
        if (currentProvider == null || currentProvider.isBlank()) {
            return newProvider;
        }

        if ("both".equals(currentProvider) || currentProvider.equals(newProvider)) {
            return currentProvider;
        }

        if (("manual".equals(currentProvider) && "google".equals(newProvider))
                || ("google".equals(currentProvider) && "manual".equals(newProvider))) {
            return "both";
        }

        return newProvider;
    }
}
