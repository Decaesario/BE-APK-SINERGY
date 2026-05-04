package com.impal.gabungyuk.auth.controller;

import com.impal.gabungyuk.auth.model.request.LoginUserRequest;
import com.impal.gabungyuk.auth.model.request.LoginGoogleRequest;
import com.impal.gabungyuk.auth.model.request.RegisterUserRequest;
import com.impal.gabungyuk.auth.model.request.UpdateUserRequest;
import com.impal.gabungyuk.auth.model.response.AuthUserResponse;
import com.impal.gabungyuk.core.model.SuccessResponse;
import com.impal.gabungyuk.auth.service.UserService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {

    private final UserService userService;

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
            value = "/api/v1/users/current",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public SuccessResponse<AuthUserResponse> getCurrentUser(@RequestHeader("Authorization") String authorizationHeader) {
        AuthUserResponse response = userService.getCurrentUser(authorizationHeader);

        return SuccessResponse.<AuthUserResponse>builder()
                .status(200)
                .message("Get user successful")
                .data(response)
                .build();
    }

    @RequestMapping(
            value = "/api/v1/update/users/current",
            method = {RequestMethod.PATCH, RequestMethod.PUT},
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public SuccessResponse<AuthUserResponse> updateUser(@RequestHeader("Authorization") String authorizationHeader,
                                                        @RequestBody UpdateUserRequest request) {
        AuthUserResponse response = userService.updateCurrentUser(authorizationHeader, request);

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
