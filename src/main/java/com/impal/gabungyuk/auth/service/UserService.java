package com.impal.gabungyuk.auth.service;

import com.impal.gabungyuk.auth.entity.User;
import com.impal.gabungyuk.auth.model.request.LoginGoogleRequest;
import com.impal.gabungyuk.auth.model.request.LoginUserRequest;
import com.impal.gabungyuk.auth.model.request.RegisterUserRequest;
import com.impal.gabungyuk.auth.model.request.UpdateUserRequest;
import com.impal.gabungyuk.auth.model.response.AuthUserResponse;
import com.impal.gabungyuk.auth.respository.UserRepository;
import com.impal.gabungyuk.core.security.BCrypt;
import com.impal.gabungyuk.core.service.TokenService;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final String PROVIDER_MANUAL = "manual";
    private static final String PROVIDER_GOOGLE = "google";
    private static final String PROVIDER_BOTH = "both";

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final FirebaseAuthService firebaseAuthService;

    public UserService(UserRepository userRepository, TokenService tokenService, FirebaseAuthService firebaseAuthService) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.firebaseAuthService = firebaseAuthService;
    }

    @Transactional
    public AuthUserResponse register(RegisterUserRequest request) {
        String email = normalizeEmail(request.getEmail());
        String rawPassword = requirePassword(request.getPassword());

        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        User user = User.builder()
                .namaLengkap(resolveDisplayName(request.getNamaLengkap(), email))
                .email(email)
                .password(hashPassword(rawPassword))
                .provider(PROVIDER_MANUAL)
                .institusi(request.getInstitusi())
                .bio(request.getBio())
                .keahlian(serializeKeahlian(request.getKeahlian()))
                .lokasi(request.getLokasi())
                .whatsapp(request.getWhatsapp())
                .build();

        User savedUser = userRepository.save(user);
        return buildAuthResponse(savedUser);
    }

    @Transactional
    public AuthUserResponse login(LoginUserRequest request) {
        String email = normalizeEmail(request.getEmail());
        String rawPassword = requirePassword(request.getPassword());

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        String storedPassword = user.getPassword();
        if (storedPassword == null || storedPassword.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Password is not set for this account");
        }

        boolean passwordMatched;
        boolean shouldSave = false;
        if (isBcryptHash(storedPassword)) {
            passwordMatched = BCrypt.checkpw(rawPassword, storedPassword);
        } else {
            passwordMatched = storedPassword.equals(rawPassword);
            if (passwordMatched) {
                user.setPassword(hashPassword(rawPassword));
                shouldSave = true;
            }
        }

        if (!passwordMatched) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        String mergedProvider = mergeProvider(resolveCurrentProvider(user), PROVIDER_MANUAL);
        if (!mergedProvider.equals(user.getProvider())) {
            user.setProvider(mergedProvider);
            shouldSave = true;
        }

        if (shouldSave) {
            user = userRepository.save(user);
        }

        return buildAuthResponse(user);
    }

    @Transactional
    public AuthUserResponse loginWithGoogle(LoginGoogleRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
        }

        FirebaseAuthService.FirebaseIdentity identity = firebaseAuthService.verifyIdToken(request.getIdToken());
        User userByEmail = userRepository.findByEmail(identity.email()).orElse(null);
        User userByFirebaseUid = userRepository.findByFirebaseUid(identity.uid()).orElse(null);

        if (userByEmail != null && userByFirebaseUid != null
                && !userByEmail.getIdPengguna().equals(userByFirebaseUid.getIdPengguna())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Google account is already linked to another user"
            );
        }

        User user = userByEmail != null ? userByEmail : userByFirebaseUid;
        if (user == null) {
            User createdUser = userRepository.save(User.builder()
                    .namaLengkap(resolveDisplayName(identity.name(), identity.email()))
                    .email(identity.email())
                    .password(hashPassword("firebase:" + identity.uid() + ":" + System.nanoTime()))
                    .firebaseUid(identity.uid())
                    .provider(PROVIDER_GOOGLE)
                    .profilePicture(identity.picture())
                    .build());
            // Return Firebase ID token to client (client already has it, but return for compatibility)
            long expiredAt = System.currentTimeMillis() + (1000L * 60 * 60);
            return buildAuthResponse(createdUser, request.getIdToken(), expiredAt);
        }

        if (!identity.email().equals(user.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email does not match linked Google account");
        }

        boolean shouldSave = false;
        if (user.getFirebaseUid() == null || user.getFirebaseUid().isBlank()) {
            user.setFirebaseUid(identity.uid());
            shouldSave = true;
        } else if (!user.getFirebaseUid().equals(identity.uid())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Google account mismatch for this email");
        }

        String mergedProvider = mergeProvider(resolveCurrentProvider(user), PROVIDER_GOOGLE);
        if (!mergedProvider.equals(user.getProvider())) {
            user.setProvider(mergedProvider);
            shouldSave = true;
        }

        if (identity.name() != null && !identity.name().isBlank() && !identity.name().trim().equals(user.getNamaLengkap())) {
            user.setNamaLengkap(identity.name().trim());
            shouldSave = true;
        }

        if (identity.picture() != null && !identity.picture().isBlank()
                && !identity.picture().trim().equals(user.getProfilePicture())) {
            user.setProfilePicture(identity.picture().trim());
            shouldSave = true;
        }

        if (shouldSave) {
            user = userRepository.save(user);
        }

        long expiredAt = System.currentTimeMillis() + (1000L * 60 * 60);
        return buildAuthResponse(user, request.getIdToken(), expiredAt);
    }

    @Transactional
    public AuthUserResponse getUserById(Integer id, String authorizationHeader) {

        tokenService.extractUserIdFromAuthorizationHeader(authorizationHeader);

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return AuthUserResponse.builder()
                .userId(user.getIdPengguna())
                .namaLengkap(user.getNamaLengkap())
                .profilePicture(user.getProfilePicture())
                .institusi(user.getInstitusi())
                .bio(user.getBio())
                .keahlian(parseKeahlian(user.getKeahlian()))
                .lokasi(user.getLokasi())
                .whatsapp(user.getWhatsapp())
                .email(user.getEmail())
                .build();
    }

    @Transactional
    public AuthUserResponse updateCurrentUser(String authorizationHeader, UpdateUserRequest request) {
        Integer userId = tokenService.extractUserIdFromAuthorizationHeader(authorizationHeader);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        boolean hasNamaLengkap = request.getNamaLengkap() != null && !request.getNamaLengkap().isBlank();
        boolean hasEmail = request.getEmail() != null && !request.getEmail().isBlank();
        boolean hasPassword = request.getPassword() != null && !request.getPassword().isBlank();
        boolean hasProfilePicture = request.getProfilePicture() != null && !request.getProfilePicture().isBlank();
        boolean hasInstitusi = request.getInstitusi() != null && !request.getInstitusi().isBlank();
        boolean hasBio = request.getBio() != null && !request.getBio().isBlank();
        boolean hasKeahlian = request.getKeahlian() != null
                && request.getKeahlian().stream().anyMatch(item -> item != null && !item.isBlank());
        boolean hasLokasi = request.getLokasi() != null && !request.getLokasi().isBlank();
        boolean hasWhatsapp = request.getWhatsapp() != null && !request.getWhatsapp().isBlank();

        if (!hasNamaLengkap && !hasEmail && !hasPassword && !hasProfilePicture
                && !hasInstitusi && !hasBio && !hasKeahlian && !hasLokasi && !hasWhatsapp) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one field must be provided");
        }

        if (hasNamaLengkap) {
            user.setNamaLengkap(request.getNamaLengkap().trim());
        }

        if (hasEmail) {
            String email = normalizeEmail(request.getEmail());

            if (!email.equals(user.getEmail()) && userRepository.existsByEmail(email)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
            }

            user.setEmail(email);
        }

        if (hasPassword) {
            user.setPassword(hashPassword(request.getPassword().trim()));
            user.setProvider(mergeProvider(resolveCurrentProvider(user), PROVIDER_MANUAL));
        }

        if (hasProfilePicture) {
            user.setProfilePicture(request.getProfilePicture().trim());
        }

        if (hasInstitusi) {
            user.setInstitusi(request.getInstitusi().trim());
        }

        if (hasBio) {
            user.setBio(request.getBio().trim());
        }

        if (hasKeahlian) {
            user.setKeahlian(serializeKeahlian(request.getKeahlian()));
        }

        if (hasLokasi) {
            user.setLokasi(request.getLokasi().trim());
        }

        if (hasWhatsapp) {
            user.setWhatsapp(request.getWhatsapp().trim());
        }

        User updatedUser = userRepository.save(user);
        return buildAuthResponse(updatedUser);
    }

    @Transactional
    public void deleteCurrentUser(String authorizationHeader) {
        Integer userId = tokenService.extractUserIdFromAuthorizationHeader(authorizationHeader);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        userRepository.delete(user);
    }

    private AuthUserResponse buildAuthResponse(User user) {
        long expiredAt = System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 7);

        return AuthUserResponse.builder()
                .userId(user.getIdPengguna())
                .namaLengkap(user.getNamaLengkap())
                .email(user.getEmail())
                .profilePicture(user.getProfilePicture())
                .institusi(user.getInstitusi())
                .bio(user.getBio())
                .keahlian(parseKeahlian(user.getKeahlian()))
                .lokasi(user.getLokasi())
                .whatsapp(user.getWhatsapp())
                .token(tokenService.generateToken(user.getIdPengguna(), expiredAt))
                .expiredAt(expiredAt)
                .build();
    }

    private AuthUserResponse buildAuthResponse(User user, String token, long expiredAt) {
        return AuthUserResponse.builder()
                .userId(user.getIdPengguna())
                .namaLengkap(user.getNamaLengkap())
                .email(user.getEmail())
                .profilePicture(user.getProfilePicture())
                .institusi(user.getInstitusi())
                .bio(user.getBio())
                .keahlian(parseKeahlian(user.getKeahlian()))
                .lokasi(user.getLokasi())
                .whatsapp(user.getWhatsapp())
                .token(token)
                .expiredAt(expiredAt)
                .build();
    }

    private List<String> parseKeahlian(String keahlian) {
        if (keahlian == null || keahlian.isBlank()) {
            return null;
        }

        return Arrays.stream(keahlian.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
    }

    private String serializeKeahlian(List<String> keahlian) {
        if (keahlian == null) {
            return null;
        }

        String joined = keahlian.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .collect(Collectors.joining(","));

        return joined.isBlank() ? null : joined;
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }
        return email.trim().toLowerCase();
    }

    private String requirePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
        }
        return password.trim();
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

    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    private boolean isBcryptHash(String storedPassword) {
        return storedPassword.startsWith("$2a$")
                || storedPassword.startsWith("$2b$")
                || storedPassword.startsWith("$2y$");
    }

    private String mergeProvider(String currentProvider, String newProvider) {
        if (currentProvider == null || currentProvider.isBlank()) {
            return newProvider;
        }

        if (PROVIDER_BOTH.equals(currentProvider) || currentProvider.equals(newProvider)) {
            return currentProvider;
        }

        if ((PROVIDER_MANUAL.equals(currentProvider) && PROVIDER_GOOGLE.equals(newProvider))
                || (PROVIDER_GOOGLE.equals(currentProvider) && PROVIDER_MANUAL.equals(newProvider))) {
            return PROVIDER_BOTH;
        }

        return newProvider;
    }

    private String resolveCurrentProvider(User user) {
        if (user.getProvider() != null && !user.getProvider().isBlank()) {
            return user.getProvider();
        }

        boolean hasManual = user.getPassword() != null && !user.getPassword().isBlank();
        boolean hasGoogle = user.getFirebaseUid() != null && !user.getFirebaseUid().isBlank();

        if (hasManual && hasGoogle) {
            return PROVIDER_BOTH;
        }
        if (hasManual) {
            return PROVIDER_MANUAL;
        }
        if (hasGoogle) {
            return PROVIDER_GOOGLE;
        }

        return null;
    }
}
