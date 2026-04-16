package com.impal.gabungyuk.service;

import com.impal.gabungyuk.entity.User;
import com.impal.gabungyuk.model.AuthUserResponse;
import com.impal.gabungyuk.model.LoginUserRequest;
import com.impal.gabungyuk.model.RegisterUserRequest;
import com.impal.gabungyuk.model.UpdateUserRequest;
import com.impal.gabungyuk.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final TokenService tokenService;

    public UserService(UserRepository userRepository, TokenService tokenService) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }

    @Transactional
    public AuthUserResponse register(RegisterUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(request.getPassword())
                .build();

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
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }

    @Transactional
    public AuthUserResponse updateCurrentUser(String authorizationHeader, UpdateUserRequest request) {
        Integer userId = tokenService.extractUserIdFromAuthorizationHeader(authorizationHeader);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        boolean hasUsername = request.getUsername() != null && !request.getUsername().isBlank();
        boolean hasEmail = request.getEmail() != null && !request.getEmail().isBlank();
        boolean hasPassword = request.getPassword() != null && !request.getPassword().isBlank();

        if (!hasUsername && !hasEmail && !hasPassword) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one field must be provided");
        }

        if (hasUsername) {
            String username = request.getUsername().trim();
            if (!username.equals(user.getUsername()) && userRepository.existsByUsername(username)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
            }
            user.setUsername(username);
        }

        if (hasEmail) {
            String email = request.getEmail().trim();
            if (!email.equals(user.getEmail()) && userRepository.existsByEmail(email)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
            }
            user.setEmail(email);
        }

        if (hasPassword) {
            user.setPassword(request.getPassword());
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
                .username(user.getUsername())
                .email(user.getEmail())
                .token(tokenService.generateToken(user.getIdPengguna(), expiredAt))
                .expiredAt(expiredAt)
                .build();
    }
}
