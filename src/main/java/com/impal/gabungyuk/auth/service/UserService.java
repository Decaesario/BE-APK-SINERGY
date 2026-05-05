package com.impal.gabungyuk.auth.service;

import com.impal.gabungyuk.auth.entity.User;
import com.impal.gabungyuk.auth.model.response.AuthUserResponse;
import com.impal.gabungyuk.auth.model.request.LoginUserRequest;
import com.impal.gabungyuk.auth.model.request.RegisterUserRequest;
import com.impal.gabungyuk.auth.model.request.UpdateUserRequest;
import com.impal.gabungyuk.auth.respository.UserRepository;
import com.impal.gabungyuk.core.service.TokenService;
import jakarta.transaction.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final TokenService tokenService;

    public UserService(UserRepository userRepository, TokenService tokenService) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }

    // ===== HELPER METHODS =====

    // Convert String (comma-separated) ke List<String>
    private List<String> parseKeahlian(String keahlian) {
        if (keahlian == null || keahlian.isEmpty()) {
            return null;
        }
        return Arrays.asList(keahlian.split(","));
    }

    // Convert List<String> ke String (comma-separated)
    private String joinKeahlian(List<String> keahlian) {
        if (keahlian == null || keahlian.isEmpty()) {
            return null;
        }
        return String.join(",", keahlian);
    }

    @Transactional
    public AuthUserResponse register(RegisterUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        User user = User.builder()
                .namaLengkap(request.getNamaLengkap())
                .email(request.getEmail())
                .password(request.getPassword())
                // Tambahkan baris di bawah ini:
                .institusi(request.getInstitusi())
                .bio(request.getBio())
                .keahlian(joinKeahlian(request.getKeahlian()))
                .lokasi(request.getLokasi())
                .whatsapp(request.getWhatsapp())
                .instagram(request.getInstagram())
                .facebook(request.getFacebook())
                .linkedin(request.getLinkedin())
                .build();
                // .namaLengkap(request.getNamaLengkap())//ini guat tambah
                // // .username(request.getUsername())
                // .email(request.getEmail())
                // .password(request.getPassword())
                // .build();

        User savedUser = userRepository.save(user);
        return buildAuthResponse(savedUser);
    }

    @Transactional
    public AuthUserResponse login(LoginUserRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        if (!user.getPassword().equals(request.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        return buildAuthResponse(user);
    }

    @Transactional
    public AuthUserResponse getCurrentUser(String authorizationHeader) {
        Integer userId = tokenService.extractUserIdFromAuthorizationHeader(authorizationHeader);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return AuthUserResponse.builder()
                .userId(user.getIdPengguna())
                .namaLengkap(user.getNamaLengkap())
                .profilePicture(user.getProfilePicture())//ini guat tambah
                .institusi(user.getInstitusi())//ini guat tambah
                .bio(user.getBio())//ini guat tambah
                .keahlian(parseKeahlian(user.getKeahlian()))//ini guat tambah
                .lokasi(user.getLokasi())//ini guat tambah
                .whatsapp(user.getWhatsapp())//ini guat tambah
                .instagram(user.getInstagram())//ini guat tambah
                .facebook(user.getFacebook())//ini guat tambah
                .linkedin(user.getLinkedin())//ini guat tambah
                // .username(user.getUsername()) punya lu bar
                .email(user.getEmail())
                .build();
    }

    @Transactional
    public AuthUserResponse updateCurrentUser(
            String authorizationHeader,
            UpdateUserRequest request,
            MultipartFile profilePicture
    ) {
        Integer userId = tokenService.extractUserIdFromAuthorizationHeader(authorizationHeader);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        if (request == null) {
            request = new UpdateUserRequest();
        }

        boolean hasNamaLengkap = request.getNamaLengkap() != null;
        boolean hasEmail = request.getEmail() != null;
        boolean hasPassword = request.getPassword() != null;
        boolean hasProfilePicture = profilePicture != null && !profilePicture.isEmpty();
        boolean hasInstitusi = request.getInstitusi() != null;
        boolean hasBio = request.getBio() != null;
        boolean hasKeahlian = request.getKeahlian() != null;
        boolean hasLokasi = request.getLokasi() != null;
        boolean hasWhatsapp = request.getWhatsapp() != null;
        boolean hasInstagram = request.getInstagram() != null;
        boolean hasFacebook = request.getFacebook() != null;
        boolean hasLinkedin = request.getLinkedin() != null;

        if (!hasNamaLengkap && !hasEmail && !hasPassword && !hasProfilePicture
                && !hasInstitusi && !hasBio && !hasKeahlian && !hasLokasi
                && !hasWhatsapp && !hasInstagram && !hasFacebook && !hasLinkedin) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "At least one field must be provided"
            );
        }

        if (hasNamaLengkap) {
            user.setNamaLengkap(request.getNamaLengkap().trim());
        }

        if (hasEmail) {
            String email = request.getEmail().trim();

            if (!email.isBlank()) {
                if (!email.equals(user.getEmail()) && userRepository.existsByEmail(email)) {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Email already exists"
                    );
                }

                user.setEmail(email);
            }
        }

        if (hasPassword) {
            String password = request.getPassword();

            if (!password.isBlank()) {
                user.setPassword(password);
            }
        }

        if (hasProfilePicture) {
            try {
                String originalFilename = profilePicture.getOriginalFilename();

                if (originalFilename == null || originalFilename.isBlank()) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Invalid profile picture filename"
                    );
                }

                String fileName = System.currentTimeMillis() + "_" + originalFilename;
                String uploadDir = System.getProperty("user.home") + "/uploads/profile";

                Path uploadPath = Paths.get(uploadDir);

                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                Path filePath = uploadPath.resolve(fileName);

                Files.copy(
                        profilePicture.getInputStream(),
                        filePath,
                        StandardCopyOption.REPLACE_EXISTING
                );

                user.setProfilePicture("/uploads/profile/" + fileName);

            } catch (ResponseStatusException e) {
                throw e;
            } catch (Exception e) {
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Failed to upload profile picture: " + e.getMessage()
                );
            }
        }

        // Jangan pakai else user.setProfilePicture(null)
        // Karena kalau tidak upload foto, foto lama tetap dipertahankan.

        if (hasInstitusi) {
            user.setInstitusi(request.getInstitusi().trim());
        }

        if (hasBio) {
            user.setBio(request.getBio().trim());
        }

        if (hasKeahlian) {
            user.setKeahlian(joinKeahlian(request.getKeahlian()));
        }

        if (hasLokasi) {
            user.setLokasi(request.getLokasi().trim());
        }

        if (hasWhatsapp) {
            user.setWhatsapp(request.getWhatsapp().trim());
        }

        if (hasInstagram) {
            user.setInstagram(request.getInstagram().trim());
        }

        if (hasFacebook) {
            user.setFacebook(request.getFacebook().trim());
        }

        if (hasLinkedin) {
            user.setLinkedin(request.getLinkedin().trim());
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
            .instagram(user.getInstagram())
            .facebook(user.getFacebook())
            .linkedin(user.getLinkedin())    
            .token(tokenService.generateToken(user.getIdPengguna(), expiredAt))
            .expiredAt(expiredAt)
            .build();
    }
} 

    // punya akbar 
    // @Transactional
    // public AuthUserResponse updateCurrentUser(String authorizationHeader, UpdateUserRequest request) {
    //     Integer userId = tokenService.extractUserIdFromAuthorizationHeader(authorizationHeader);
    //     User user = userRepository.findById(userId)
    //             .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

    //     boolean hasUsername = request.getUsername() != null && !request.getUsername().isBlank();
    //     boolean hasEmail = request.getEmail() != null && !request.getEmail().isBlank();
    //     boolean hasPassword = request.getPassword() != null && !request.getPassword().isBlank();

    //     if (!hasUsername && !hasEmail && !hasPassword) {
    //         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one field must be provided");
    //     }

    //     if (hasUsername) {
    //         String username = request.getUsername().trim();
    //         if (!username.equals(user.getUsername()) && userRepository.existsByUsername(username)) {
    //             throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
    //         }
    //         user.setUsername(username);
    //     }

    //     if (hasEmail) {
    //         String email = request.getEmail().trim();
    //         if (!email.equals(user.getEmail()) && userRepository.existsByEmail(email)) {
    //             throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
    //         }
    //         user.setEmail(email);
    //     }

    //     if (hasPassword) {
    //         user.setPassword(request.getPassword());
    //     }

    //     User updatedUser = userRepository.save(user);
    //     return buildAuthResponse(updatedUser);
    // }

  


//     private AuthUserResponse buildAuthResponse(User user) {
//         long expiredAt = System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 7);
//         return AuthUserResponse.builder()
//                 .userId(user.getIdPengguna())
//                 .username(user.getUsername())
//                 .email(user.getEmail())
//                 .token(tokenService.generateToken(user.getIdPengguna(), expiredAt))
//                 .expiredAt(expiredAt)
//                 .build();
//     }
// }
