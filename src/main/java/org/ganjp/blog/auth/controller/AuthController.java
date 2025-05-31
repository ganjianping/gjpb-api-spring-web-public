package org.ganjp.blog.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.ganjp.blog.auth.model.dto.request.LoginRequest;
import org.ganjp.blog.auth.model.dto.request.LogoutRequest;
import org.ganjp.blog.auth.model.dto.request.RefreshTokenRequest;
import org.ganjp.blog.auth.model.dto.request.SignupRequest;
import org.ganjp.blog.common.model.ApiResponse;
import org.ganjp.blog.auth.model.dto.response.AuthTokenResponse;
import org.ganjp.blog.auth.model.dto.response.LoginResponse;
import org.ganjp.blog.auth.model.dto.response.SignupResponse;
import org.ganjp.blog.auth.model.dto.response.TokenRefreshResponse;
import org.ganjp.blog.auth.service.AuthService;
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
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {
        try {
            // Store request data for audit logging
            String username = extractUsernameFromLoginRequest(loginRequest);
            request.setAttribute("loginUsername", username);
            request.setAttribute("loginRequestData", sanitizeLoginRequest(loginRequest));

            LoginResponse loginResponse = authService.login(loginRequest);
            
            // Store response data and resource ID for audit logging
            request.setAttribute("loginResponseData", sanitizeLoginResponse(loginResponse));
            request.setAttribute("loginResourceId", extractUserIdFromResponse(loginResponse));
            
            ApiResponse<LoginResponse> response = ApiResponse.<LoginResponse>success(loginResponse, "User login successful");
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            // Store username even for failed login
            String username = extractUsernameFromLoginRequest(loginRequest);
            request.setAttribute("loginUsername", username);
            request.setAttribute("loginRequestData", sanitizeLoginRequest(loginRequest));
            
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage()); // Use the original exception message
            ApiResponse<LoginResponse> response = ApiResponse.<LoginResponse>error(401, "Unauthorized", errors);
            return ResponseEntity.status(401)
                    .body(response);
        } catch (Exception e) {
            // Store username even for failed login
            String username = extractUsernameFromLoginRequest(loginRequest);
            request.setAttribute("loginUsername", username);
            request.setAttribute("loginRequestData", sanitizeLoginRequest(loginRequest));
            
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
    public ResponseEntity<ApiResponse<SignupResponse>> signup(
            @Valid @RequestBody SignupRequest signupRequest,
            HttpServletRequest request) {
        try {
            // Store request data for audit logging
            request.setAttribute("loginUsername", signupRequest.getUsername());
            request.setAttribute("loginRequestData", sanitizeSignupRequest(signupRequest));

            SignupResponse signupResponse = authService.signup(signupRequest);
            
            // Store response data and resource ID for audit logging
            request.setAttribute("loginResponseData", sanitizeSignupResponse(signupResponse));
            request.setAttribute("loginResourceId", signupResponse.getId());
            
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
            // Store username even for failed signup
            request.setAttribute("loginUsername", signupRequest.getUsername());
            request.setAttribute("loginRequestData", sanitizeSignupRequest(signupRequest));
            
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            ApiResponse<SignupResponse> response = ApiResponse.<SignupResponse>error(400, "Registration failed", errors);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(response);
        } catch (Exception e) {
            // Store username even for failed signup
            request.setAttribute("loginUsername", signupRequest.getUsername());
            request.setAttribute("loginRequestData", sanitizeSignupRequest(signupRequest));
            
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            ApiResponse<SignupResponse> response = ApiResponse.<SignupResponse>error(500, "Internal Server Error", errors);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(response);
        }
    }

    /**
     * Logout the current user and invalidate their JWT token
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        try {
            authService.logout(request);
            
            ApiResponse<Void> response = ApiResponse.<Void>success(null, "User logout successful");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            ApiResponse<Void> response = ApiResponse.<Void>error(500, "Internal Server Error", errors);
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Enhanced login endpoint that returns both access and refresh tokens
     * This is the new recommended authentication method with token rotation support
     */
    @PostMapping("/login/dual-tokens")
    public ResponseEntity<ApiResponse<AuthTokenResponse>> loginWithDualTokens(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {
        try {
            // Store request data for audit logging
            String username = extractUsernameFromLoginRequest(loginRequest);
            request.setAttribute("loginUsername", username);
            request.setAttribute("loginRequestData", sanitizeLoginRequest(loginRequest));

            AuthTokenResponse authResponse = authService.loginWithDualTokens(loginRequest);
            
            // Store response data and resource ID for audit logging
            request.setAttribute("loginResponseData", sanitizeAuthTokenResponse(authResponse));
            request.setAttribute("loginResourceId", authResponse.getUsername());
            
            ApiResponse<AuthTokenResponse> response = ApiResponse.<AuthTokenResponse>success(authResponse, "User login successful with dual tokens");
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            // Store username even for failed login
            String username = extractUsernameFromLoginRequest(loginRequest);
            request.setAttribute("loginUsername", username);
            request.setAttribute("loginRequestData", sanitizeLoginRequest(loginRequest));
            
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            ApiResponse<AuthTokenResponse> response = ApiResponse.<AuthTokenResponse>error(401, "Unauthorized", errors);
            return ResponseEntity.status(401).body(response);
        } catch (Exception e) {
            // Store username even for failed login
            String username = extractUsernameFromLoginRequest(loginRequest);
            request.setAttribute("loginUsername", username);
            request.setAttribute("loginRequestData", sanitizeLoginRequest(loginRequest));
            
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            ApiResponse<AuthTokenResponse> response = ApiResponse.<AuthTokenResponse>error(500, "Internal Server Error", errors);
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Refresh access token using a valid refresh token
     * Implements token rotation - old refresh token is invalidated and new tokens are issued
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenRefreshResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest refreshRequest,
            HttpServletRequest request) {
        try {
            // Store request data for audit logging (excluding sensitive token data)
            request.setAttribute("refreshTokenRequest", sanitizeRefreshTokenRequest(refreshRequest));

            TokenRefreshResponse refreshResponse = authService.refreshToken(refreshRequest);
            
            // Store response data for audit logging
            request.setAttribute("refreshTokenResponse", sanitizeTokenRefreshResponse(refreshResponse));
            
            ApiResponse<TokenRefreshResponse> response = ApiResponse.<TokenRefreshResponse>success(refreshResponse, "Token refresh successful");
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            ApiResponse<TokenRefreshResponse> response = ApiResponse.<TokenRefreshResponse>error(401, "Invalid or expired refresh token", errors);
            return ResponseEntity.status(401).body(response);
        } catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            ApiResponse<TokenRefreshResponse> response = ApiResponse.<TokenRefreshResponse>error(500, "Internal Server Error", errors);
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Enhanced logout endpoint that revokes both access and refresh tokens
     * This is the new recommended logout method with complete token invalidation
     */
    @PostMapping("/logout/enhanced")
    public ResponseEntity<ApiResponse<Void>> enhancedLogout(
            @Valid @RequestBody LogoutRequest logoutRequest,
            HttpServletRequest request) {
        try {
            // Store request data for audit logging (excluding sensitive token data)
            request.setAttribute("enhancedLogoutRequest", sanitizeLogoutRequest(logoutRequest));

            authService.enhancedLogout(logoutRequest, request);
            
            ApiResponse<Void> response = ApiResponse.<Void>success(null, "Enhanced logout successful - all tokens revoked");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            ApiResponse<Void> response = ApiResponse.<Void>error(500, "Internal Server Error", errors);
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Helper method to extract username from login request
     */
    private String extractUsernameFromLoginRequest(LoginRequest loginRequest) {
        if (loginRequest.getUsername() != null && !loginRequest.getUsername().trim().isEmpty()) {
            return loginRequest.getUsername();
        } else if (loginRequest.getEmail() != null && !loginRequest.getEmail().trim().isEmpty()) {
            return loginRequest.getEmail();
        } else if (loginRequest.getMobileCountryCode() != null && loginRequest.getMobileNumber() != null) {
            return loginRequest.getMobileCountryCode() + "-" + loginRequest.getMobileNumber();
        }
        return null;
    }

    /**
     * Helper method to sanitize login request for audit logging (remove password)
     */
    private Object sanitizeLoginRequest(LoginRequest loginRequest) {
        Map<String, Object> sanitized = new HashMap<>();
        sanitized.put("username", loginRequest.getUsername());
        sanitized.put("email", loginRequest.getEmail());
        sanitized.put("mobileCountryCode", loginRequest.getMobileCountryCode());
        sanitized.put("mobileNumber", loginRequest.getMobileNumber());
        // Deliberately exclude password for security
        return sanitized;
    }

    /**
     * Helper method to sanitize login response for audit logging (remove token)
     */
    private Object sanitizeLoginResponse(LoginResponse loginResponse) {
        Map<String, Object> sanitized = new HashMap<>();
        sanitized.put("username", loginResponse.getUsername());
        sanitized.put("email", loginResponse.getEmail());
        sanitized.put("accountStatus", loginResponse.getAccountStatus());
        sanitized.put("lastLoginAt", loginResponse.getLastLoginAt());
        sanitized.put("roleCodes", loginResponse.getRoleCodes());
        // Deliberately exclude token for security
        return sanitized;
    }

    /**
     * Helper method to extract user ID from login response
     */
    private String extractUserIdFromResponse(LoginResponse loginResponse) {
        // Since LoginResponse doesn't contain user ID directly, 
        // we could try to extract it from the JWT token or return null
        // For now, return null as user ID is typically handled by security context
        return null;
    }

    /**
     * Helper method to sanitize signup request for audit logging (remove password)
     */
    private Object sanitizeSignupRequest(SignupRequest signupRequest) {
        Map<String, Object> sanitized = new HashMap<>();
        sanitized.put("username", signupRequest.getUsername());
        sanitized.put("email", signupRequest.getEmail());
        sanitized.put("mobileCountryCode", signupRequest.getMobileCountryCode());
        sanitized.put("mobileNumber", signupRequest.getMobileNumber());
        sanitized.put("nickname", signupRequest.getNickname());
        // Deliberately exclude password for security
        return sanitized;
    }

    /**
     * Helper method to sanitize signup response for audit logging
     */
    private Object sanitizeSignupResponse(SignupResponse signupResponse) {
        Map<String, Object> sanitized = new HashMap<>();
        sanitized.put("id", signupResponse.getId());
        sanitized.put("username", signupResponse.getUsername());
        sanitized.put("email", signupResponse.getEmail());
        sanitized.put("accountStatus", signupResponse.getAccountStatus());
        sanitized.put("active", signupResponse.getActive());
        return sanitized;
    }

    // NEW SANITIZATION METHODS FOR TOKEN ROTATION APIS

    /**
     * Helper method to sanitize auth token response for audit logging (remove sensitive tokens)
     */
    private Object sanitizeAuthTokenResponse(AuthTokenResponse authTokenResponse) {
        Map<String, Object> sanitized = new HashMap<>();
        sanitized.put("username", authTokenResponse.getUsername());
        sanitized.put("email", authTokenResponse.getEmail());
        sanitized.put("accountStatus", authTokenResponse.getAccountStatus());
        sanitized.put("lastLoginAt", authTokenResponse.getLastLoginAt());
        sanitized.put("roleCodes", authTokenResponse.getRoleCodes());
        sanitized.put("tokenType", authTokenResponse.getTokenType());
        sanitized.put("expiresIn", authTokenResponse.getExpiresIn());
        // Deliberately exclude access and refresh tokens for security
        return sanitized;
    }

    /**
     * Helper method to sanitize refresh token request for audit logging (remove sensitive token)
     */
    private Object sanitizeRefreshTokenRequest(RefreshTokenRequest refreshRequest) {
        Map<String, Object> sanitized = new HashMap<>();
        sanitized.put("tokenPresent", refreshRequest.getRefreshToken() != null && !refreshRequest.getRefreshToken().trim().isEmpty());
        // Deliberately exclude actual refresh token for security
        return sanitized;
    }

    /**
     * Helper method to sanitize token refresh response for audit logging (remove sensitive tokens)
     */
    private Object sanitizeTokenRefreshResponse(TokenRefreshResponse refreshResponse) {
        Map<String, Object> sanitized = new HashMap<>();
        sanitized.put("tokenType", refreshResponse.getTokenType());
        sanitized.put("expiresIn", refreshResponse.getExpiresIn());
        sanitized.put("tokensGenerated", true);
        // Deliberately exclude access and refresh tokens for security
        return sanitized;
    }

    /**
     * Helper method to sanitize logout request for audit logging (remove sensitive token)
     */
    private Object sanitizeLogoutRequest(LogoutRequest logoutRequest) {
        Map<String, Object> sanitized = new HashMap<>();
        sanitized.put("refreshTokenPresent", logoutRequest.getRefreshToken() != null && !logoutRequest.getRefreshToken().trim().isEmpty());
        // Deliberately exclude actual refresh token for security
        return sanitized;
    }
}

