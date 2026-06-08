package com.impal.gabungyuk.auth.controller;

import com.impal.gabungyuk.auth.model.request.LoginUserRequest;
import com.impal.gabungyuk.auth.model.request.LoginGoogleRequest;
import com.impal.gabungyuk.auth.model.request.RegisterUserRequest;
import com.impal.gabungyuk.auth.model.request.UpdateUserRequest;
import com.impal.gabungyuk.auth.model.response.AuthUserResponse;
import com.impal.gabungyuk.core.model.SuccessResponse;
import com.impal.gabungyuk.auth.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

@RestController
public class UserController {

    private final UserService userService;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UserController.class);

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(       
            value = "/api/v1/users/register",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public SuccessResponse<AuthUserResponse> register(@RequestBody RegisterUserRequest request) {

        AuthUserResponse response = userService.register(request);

        return SuccessResponse.<AuthUserResponse>builder()
                .status(200)
                .message("User registered successfully")
                .data(response)
                .build();
    }

    @PostMapping(
            value = "/api/v1/users/login",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public SuccessResponse<AuthUserResponse> login(@RequestBody LoginUserRequest request) {
        AuthUserResponse response = userService.login(request);

        return SuccessResponse.<AuthUserResponse>builder()
                .status(200)
                .message("Login successful")
                .data(response)
                .build();
    }

    @PostMapping(
            value = "/api/v1/users/login/google",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public SuccessResponse<AuthUserResponse> loginWithGoogle(@RequestBody LoginGoogleRequest request) {
        AuthUserResponse response = userService.loginWithGoogle(request);

        return SuccessResponse.<AuthUserResponse>builder()
                .status(200)
                .message("Google login successful")
                .data(response)
                .build();
    }

    @GetMapping(
            value = "/api/v1/users/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public SuccessResponse<AuthUserResponse> getUserById(
            @PathVariable("id") Integer id,
            @RequestHeader("Authorization") String authorizationHeader
    ) {

        AuthUserResponse response = userService.getUserById(id, authorizationHeader);

        return SuccessResponse.<AuthUserResponse>builder()
                .status(200)
                .message("Get user successful")
                .data(response)
                .build();
    }

    @PatchMapping(
            value = "/api/v1/update/users/current",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public SuccessResponse<AuthUserResponse> updateUser(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestPart(value = "data", required = false) String dataJson,
            @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture
    ) {
        ObjectMapper mapper = new ObjectMapper();
        UpdateUserRequest request = null;

        if (dataJson != null && !dataJson.isEmpty()) {
            try {
                request = mapper.readValue(dataJson, UpdateUserRequest.class);
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid JSON: " + e.getMessage());
            }
        }

        AuthUserResponse response = userService.updateCurrentUser(
                authorizationHeader,
                request,
                profilePicture
        );

    private SuccessResponse<AuthUserResponse> buildUpdateUserResponse(AuthUserResponse response) {
        return SuccessResponse.<AuthUserResponse>builder()
                .status(200)
                .message("Update user successful")
                .data(response)
                .build();
    }

    @DeleteMapping(
            value = "/api/v1/delete/users/current",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public SuccessResponse<Object> deleteCurrentUser(@RequestHeader("Authorization") String authorizationHeader) {
        userService.deleteCurrentUser(authorizationHeader);

        return SuccessResponse.builder()
                .status(200)
                .message("Delete user successful")
                .data(java.util.Map.of("message", "User deleted successfully"))
                .build();
    }
}
