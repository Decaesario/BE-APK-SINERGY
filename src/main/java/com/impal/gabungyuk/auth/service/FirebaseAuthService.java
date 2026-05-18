package com.impal.gabungyuk.auth.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class FirebaseAuthService {

    private static final Logger log = LoggerFactory.getLogger(FirebaseAuthService.class);

    private final String configuredServiceAccountPath;
    private final String configuredGoogleApplicationCredentials;
    private volatile ServiceAccountMetadata serviceAccountMetadata;
    private volatile FirebaseAuth firebaseAuth;

    public FirebaseAuthService(
            @Value("${firebase.service-account.path:}") String configuredServiceAccountPath,
            @Value("${GOOGLE_APPLICATION_CREDENTIALS:}") String configuredGoogleApplicationCredentials
    ) {
        this.configuredServiceAccountPath = configuredServiceAccountPath;
        this.configuredGoogleApplicationCredentials = configuredGoogleApplicationCredentials;
    }

    public FirebaseIdentity verifyIdToken(String idToken) {
        if (idToken == null || idToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "idToken is required");
        }

        logTokenDiagnostics(idToken);

        FirebaseToken decodedToken;
        try {
            decodedToken = getFirebaseAuth().verifyIdToken(idToken);
            log.info("Firebase token verified successfully. uid={}, email={}", decodedToken.getUid(), decodedToken.getEmail());
        } catch (FirebaseAuthException exception) {
            log.warn("Firebase token verification failed. authErrorCode={}, message={}",
                    exception.getAuthErrorCode(), exception.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, buildFirebaseTokenErrorMessage(exception, idToken));
        } catch (IllegalArgumentException exception) {
            log.warn("Firebase token is malformed: {}", exception.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Firebase ID token malformed");
        }

        String email = decodedToken.getEmail();
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Firebase token does not contain email");
        }

        String name = decodedToken.getName();
        String picture = decodedToken.getPicture();
        return new FirebaseIdentity(decodedToken.getUid(), email.trim().toLowerCase(), name, picture);
    }

    private FirebaseAuth getFirebaseAuth() {
        if (firebaseAuth != null) {
            return firebaseAuth;
        }

        synchronized (this) {
            if (firebaseAuth != null) {
                return firebaseAuth;
            }

            ServiceAccountPath serviceAccount = resolveServiceAccountPath();
            log.info("Initializing Firebase Admin SDK with service account source={} path={}",
                    serviceAccount.source(), serviceAccount.path());
            serviceAccountMetadata = readServiceAccountMetadata(serviceAccount.path());
            log.info("Firebase service account metadata: project_id={}, client_email={}",
                    serviceAccountMetadata.projectId(), serviceAccountMetadata.clientEmail());

            try (InputStream serviceAccountStream = openServiceAccountStream(serviceAccount.path())) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccountStream))
                        .build();

                FirebaseApp app = getOrCreateFirebaseApp(options);
                firebaseAuth = FirebaseAuth.getInstance(app);
                log.info("Firebase Admin SDK initialized. appName={}", app.getName());
                return firebaseAuth;
            } catch (IOException exception) {
                log.error("Failed to initialize Firebase Admin SDK. source={} path={} error={}",
                        serviceAccount.source(), serviceAccount.path(), exception.getMessage());
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Failed to initialize Firebase Admin SDK: " + exception.getMessage()
                );
            }

        }

    }

    /**
     * Public accessor for the initialized FirebaseAuth instance. Useful for admin tasks
     * such as importing or managing users from server-side code.
     */
    public FirebaseAuth getFirebaseAuthInstance() {
        return getFirebaseAuth();
    }

    private FirebaseApp getOrCreateFirebaseApp(FirebaseOptions options) {
        List<FirebaseApp> apps = FirebaseApp.getApps();

        if (!apps.isEmpty()) {
            return apps.get(0);
        }

        return FirebaseApp.initializeApp(options);
    }

    private ServiceAccountPath resolveServiceAccountPath() {
        if (configuredServiceAccountPath != null && !configuredServiceAccountPath.isBlank()) {
            return new ServiceAccountPath(configuredServiceAccountPath.trim(), "spring-property:firebase.service-account.path");
        }

        String envPath = System.getenv("FIREBASE_SERVICE_ACCOUNT_PATH");
        if (envPath != null && !envPath.isBlank()) {
            return new ServiceAccountPath(envPath.trim(), "env:FIREBASE_SERVICE_ACCOUNT_PATH");
        }

        if (configuredGoogleApplicationCredentials != null && !configuredGoogleApplicationCredentials.isBlank()) {
            return new ServiceAccountPath(configuredGoogleApplicationCredentials.trim(), "spring-property:GOOGLE_APPLICATION_CREDENTIALS");
        }

        String googleCredentialsPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        if (googleCredentialsPath != null && !googleCredentialsPath.isBlank()) {
            return new ServiceAccountPath(googleCredentialsPath.trim(), "env:GOOGLE_APPLICATION_CREDENTIALS");
        }

        String googleCredentialsProperty = System.getProperty("GOOGLE_APPLICATION_CREDENTIALS");
        if (googleCredentialsProperty != null && !googleCredentialsProperty.isBlank()) {
            return new ServiceAccountPath(googleCredentialsProperty.trim(), "jvm-property:GOOGLE_APPLICATION_CREDENTIALS");
        }

        log.error("Firebase service account path not configured. configuredPropertyPresent={}, envFirebasePresent={}, configuredGooglePropertyPresent={}, envGooglePresent={}, jvmGooglePresent={}",
                hasText(configuredServiceAccountPath),
                hasText(System.getenv("FIREBASE_SERVICE_ACCOUNT_PATH")),
                hasText(configuredGoogleApplicationCredentials),
                hasText(System.getenv("GOOGLE_APPLICATION_CREDENTIALS")),
                hasText(System.getProperty("GOOGLE_APPLICATION_CREDENTIALS")));
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Firebase service account path is not configured");
    }

    private InputStream openServiceAccountStream(String serviceAccountPath) throws IOException {
        Path path = Path.of(serviceAccountPath).toAbsolutePath().normalize();
        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            throw new IOException("Firebase service account file not found at " + path);
        }
        return Files.newInputStream(path);
    }

    private String buildFirebaseTokenErrorMessage(FirebaseAuthException exception, String idToken) {
        String authErrorCode = exception.getAuthErrorCode() != null
                ? exception.getAuthErrorCode().name()
                : null;

        String reason = switch (authErrorCode) {
            case "EXPIRED_ID_TOKEN" -> "Firebase ID token expired";
            case "REVOKED_ID_TOKEN" -> "Firebase ID token has been revoked";
            case "INVALID_ID_TOKEN" -> "Firebase ID token is invalid";
            case "TENANT_ID_MISMATCH" -> "Firebase tenant mismatch";
            case "USER_DISABLED" -> "Firebase user is disabled";
            case "USER_NOT_FOUND" -> "Firebase user not found";
            default -> authErrorCode != null
                    ? "Firebase token verification failed (" + authErrorCode + ")"
                    : "Invalid Firebase ID token";
        };

        String message = appendFirebaseDetail(reason, exception.getMessage());
        return appendFirebaseDetail(message, buildProjectMismatchDetail(idToken));
    }

    private String appendFirebaseDetail(String reason, String detail) {
        if (detail == null || detail.isBlank()) {
            return reason;
        }
        return reason + ": " + detail.replace('\n', ' ').trim();
    }

    private void logTokenDiagnostics(String idToken) {
        TokenDiagnostics diagnostics = extractTokenDiagnostics(idToken);
        if (diagnostics == null) {
            return;
        }

        log.info(
                "Firebase token diagnostics: kid={}, alg={}, iss={}, aud={}, sub={}, exp={}, auth_time={}",
                diagnostics.kid(),
                diagnostics.alg(),
                diagnostics.iss(),
                diagnostics.aud(),
                diagnostics.sub(),
                diagnostics.exp(),
                diagnostics.authTime()
        );
    }

    private TokenDiagnostics extractTokenDiagnostics(String idToken) {
        String[] segments = idToken.split("\\.");
        if (segments.length != 3) {
            log.warn("JWT format invalid. segmentCount={}", segments.length);
            return null;
        }

        try {
            String header = parseJwtSegment(segments[0]);
            String payload = parseJwtSegment(segments[1]);
            return new TokenDiagnostics(
                    extractJsonValue(header, "kid"),
                    extractJsonValue(header, "alg"),
                    extractJsonValue(payload, "iss"),
                    extractJsonValue(payload, "aud"),
                    extractJsonValue(payload, "sub"),
                    extractJsonValue(payload, "exp"),
                    extractJsonValue(payload, "auth_time")
            );
        } catch (IllegalArgumentException exception) {
            log.warn("Failed to decode Firebase token diagnostics: {}", exception.getMessage());
            return null;
        }
    }

    private ServiceAccountMetadata readServiceAccountMetadata(String serviceAccountPath) {
        try {
            String serviceAccountJson = Files.readString(Path.of(serviceAccountPath).toAbsolutePath().normalize());
            return new ServiceAccountMetadata(
                    extractJsonValue(serviceAccountJson, "project_id"),
                    extractJsonValue(serviceAccountJson, "client_email")
            );
        } catch (IOException exception) {
            log.warn("Unable to read Firebase service account metadata from {}: {}", serviceAccountPath, exception.getMessage());
            return new ServiceAccountMetadata("<unknown>", "<unknown>");
        }
    }

    private String buildProjectMismatchDetail(String idToken) {
        TokenDiagnostics diagnostics = extractTokenDiagnostics(idToken);
        if (diagnostics == null) {
            return null;
        }

        String projectId = serviceAccountMetadata != null ? serviceAccountMetadata.projectId() : "<unknown>";
        if ("<null>".equals(projectId) || "<unknown>".equals(projectId)) {
            return "token.iss=" + diagnostics.iss() + ", token.aud=" + diagnostics.aud();
        }

        String expectedIss = "https://securetoken.google.com/" + projectId;
        String expectedAud = projectId;
        return "token.iss=" + diagnostics.iss()
                + ", token.aud=" + diagnostics.aud()
                + ", expected.iss=" + expectedIss
                + ", expected.aud=" + expectedAud
                + ", serviceAccount.project_id=" + projectId;
    }

    private String parseJwtSegment(String segment) {
        byte[] decoded = Base64.getUrlDecoder().decode(padBase64(segment));
        return new String(decoded, StandardCharsets.UTF_8);
    }

    private String padBase64(String value) {
        int remainder = value.length() % 4;
        if (remainder == 0) {
            return value;
        }
        return value + "=".repeat(4 - remainder);
    }

    private String extractJsonValue(String json, String field) {
        Pattern stringPattern = Pattern.compile("\"" + Pattern.quote(field) + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher stringMatcher = stringPattern.matcher(json);
        if (stringMatcher.find()) {
            return stringMatcher.group(1);
        }

        Pattern numberPattern = Pattern.compile("\"" + Pattern.quote(field) + "\"\\s*:\\s*([0-9]+)");
        Matcher numberMatcher = numberPattern.matcher(json);
        if (numberMatcher.find()) {
            return numberMatcher.group(1);
        }

        return "<null>";
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    public record FirebaseIdentity(String uid, String email, String name, String picture) {
    }

    private record ServiceAccountPath(String path, String source) {
    }

    private record ServiceAccountMetadata(String projectId, String clientEmail) {
    }

    private record TokenDiagnostics(String kid, String alg, String iss, String aud, String sub, String exp, String authTime) {
    }
}
