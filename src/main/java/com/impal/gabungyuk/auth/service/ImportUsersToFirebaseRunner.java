package com.impal.gabungyuk.auth.service;

// Use the nested CreateRequest class on UserRecord for compatibility with the firebase-admin version
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.impal.gabungyuk.auth.entity.User;
import com.impal.gabungyuk.auth.respository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.boot.CommandLineRunner;

import java.util.List;

/**
 * Simple import runner to create corresponding Firebase Auth user accounts for
 * existing local users. This runner is intentionally enabled only when the
 * "import-users" profile is active to avoid accidental runs.
 *
 * Usage (run once):
 *   mvn spring-boot:run -Dspring-boot.run.profiles=import-users
 *
 * The runner will attempt to create a Firebase user for each local user that
 * does not already exist in Firebase (checked by email). It will set the
 * Firebase UID to the user's existing firebaseUid if present, otherwise to
 * "db-<idPengguna>". No passwords are set; you'll need to send password-reset
 * emails or import password hashes with the Firebase bulk importer if you want
 * to preserve user passwords.
 */
@Component
@Profile("import-users")
public class ImportUsersToFirebaseRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ImportUsersToFirebaseRunner.class);

    private final UserRepository userRepository;
    private final FirebaseAuthService firebaseAuthService;

    public ImportUsersToFirebaseRunner(UserRepository userRepository, FirebaseAuthService firebaseAuthService) {
        this.userRepository = userRepository;
        this.firebaseAuthService = firebaseAuthService;
    }

    @Override
    public void run(String... args) throws Exception {
        List<User> users = userRepository.findAll();
        log.info("Starting Firebase import for {} users", users.size());

        int created = 0;
        for (User u : users) {
            try {
                // skip if user already exists in Firebase
                try {
                    UserRecord existing = firebaseAuthService.getFirebaseAuthInstance().getUserByEmail(u.getEmail());
                    log.info("Firebase user already exists for email={}; uid={}", u.getEmail(), existing.getUid());
                    continue;
                } catch (FirebaseAuthException e) {
                    // user not found -> create
                }

                String targetUid = (u.getFirebaseUid() != null && !u.getFirebaseUid().isBlank()) ? u.getFirebaseUid() : ("db-" + u.getIdPengguna());

                UserRecord.CreateRequest req = new UserRecord.CreateRequest()
                        .setUid(targetUid)
                        .setEmail(u.getEmail())
                        .setDisplayName(u.getNamaLengkap())
                        .setEmailVerified(false);

                UserRecord createdUser = firebaseAuthService.getFirebaseAuthInstance().createUser(req);
                log.info("Created Firebase user email={} uid={}", u.getEmail(), createdUser.getUid());
                created++;
            } catch (Exception ex) {
                log.error("Failed to import user email={}: {}", u.getEmail(), ex.getMessage());
            }
        }

        log.info("Firebase import complete. created={}", created);

        // exit when running as a one-off import job
        System.exit(0);
    }
}
