package org.ganjp.blog.am.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.ganjp.blog.am.model.dto.request.LoginRequest;
import org.ganjp.blog.am.model.dto.request.SignupRequest;
import org.ganjp.blog.common.model.ApiResponse;
import org.ganjp.blog.am.model.dto.response.LoginResponse;
import org.ganjp.blog.am.model.dto.response.SignupResponse;
import org.ganjp.blog.am.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse loginResponse = authService.login(loginRequest);
            ApiResponse<LoginResponse> response = ApiResponse.<LoginResponse>success(loginResponse, "User login successful");
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage()); // Use the original exception message
            ApiResponse<LoginResponse> response = ApiResponse.<LoginResponse>error(401, "Unauthorized", errors);
            return ResponseEntity.status(401)
                    .body(response);
        } catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            ApiResponse<LoginResponse> response = ApiResponse.<LoginResponse>error(500, "Internal Server Error", errors);
            return ResponseEntity.status(500)
                    .body(response);
        }
    }
    
    /**
     * Register a new user with ROLE_USER role
     *
     * @param signupRequest The signup request data
     * @return The created user data
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(@Valid @RequestBody SignupRequest signupRequest) {
        try {
            SignupResponse signupResponse = authService.signup(signupRequest);
            ApiResponse<SignupResponse> response = ApiResponse.<SignupResponse>builder()
                            .status(ApiResponse.Status.builder()
                                    .code(HttpStatus.CREATED.value())
                                    .message("User registered successfully")
                                    .errors(null)
                                    .build())
                            .data(signupResponse)
                            .meta(ApiResponse.Meta.builder()
                                    .serverDateTime(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                                            .format(java.time.LocalDateTime.now()))
                                    .build())
                            .build();
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            ApiResponse<SignupResponse> response = ApiResponse.<SignupResponse>error(400, "Registration failed", errors);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(response);
        } catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            ApiResponse<SignupResponse> response = ApiResponse.<SignupResponse>error(500, "Internal Server Error", errors);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(response);
        }
    }
}

