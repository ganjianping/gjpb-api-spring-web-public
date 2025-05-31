package org.ganjp.blog.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ganjp.blog.auth.model.dto.request.LoginRequest;
import org.ganjp.blog.auth.model.dto.request.LogoutRequest;
import org.ganjp.blog.auth.model.dto.request.RefreshTokenRequest;
import org.ganjp.blog.auth.model.dto.request.SignupRequest;
import org.ganjp.blog.auth.model.dto.response.AuthTokenResponse;
import org.ganjp.blog.auth.model.dto.response.LoginResponse;
import org.ganjp.blog.auth.model.dto.response.SignupResponse;
import org.ganjp.blog.auth.model.dto.response.TokenRefreshResponse;
import org.ganjp.blog.auth.model.entity.RefreshToken;
import org.ganjp.blog.auth.model.entity.Role;
import org.ganjp.blog.auth.model.entity.User;
import org.ganjp.blog.auth.model.entity.UserRole;
import org.ganjp.blog.auth.model.enums.AccountStatus;
import org.ganjp.blog.auth.repository.RoleRepository;
import org.ganjp.blog.auth.repository.UserRepository;
import org.ganjp.blog.auth.repository.UserRoleRepository;
import org.ganjp.blog.auth.security.JwtUtils;
import org.ganjp.blog.auth.security.TokenBlacklistService;
import org.ganjp.blog.common.exception.ResourceNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenBlacklistService tokenBlacklistService;
    private final RefreshTokenService refreshTokenService;

    /**
     * Authenticate a user and generate JWT token
     * This enhanced version supports login via:
     * - Username
     * - Email 
     * - Mobile number + country code
     * 
     * Uses CustomAuthenticationProvider for consistent authentication and failure tracking.
     */
    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {
        // Validate login request has only one authentication method
        if (!loginRequest.isValidLoginMethod()) {
            throw new BadCredentialsException("Please provide exactly one login method: username, email, or mobile number");
        }

        String principal;
        if (loginRequest.getEmail() != null && !loginRequest.getEmail().isEmpty()) {
            principal = loginRequest.getEmail();
        } else if (loginRequest.getUsername() != null && !loginRequest.getUsername().isEmpty()) {
            principal = loginRequest.getUsername();
        } else {
            // For mobile login, combine country code and mobile number as principal
            // Format: "65-1234567890" to ensure uniqueness across countries
            principal = loginRequest.getMobileCountryCode() + "-" + loginRequest.getMobileNumber();
        }
        
        // Create authentication token with login request details
        UsernamePasswordAuthenticationToken authToken = 
            new UsernamePasswordAuthenticationToken(principal, loginRequest.getPassword());
        authToken.setDetails(loginRequest);
        
        // Use AuthenticationManager (which delegates to CustomAuthenticationProvider)
        // This ensures consistent authentication logic and failure tracking
        Authentication authentication = authenticationManager.authenticate(authToken);
        
        // Set authentication in security context
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // Get the authenticated user
        User user = (User) authentication.getPrincipal();
        
        // Get client IP address for login tracking
        String clientIp = getClientIp();
        
        // Update last login timestamp and IP
        LocalDateTime now = LocalDateTime.now();
        user.setLastLoginAt(now);
        user.setLastLoginIp(clientIp);
        user.setFailedLoginAttempts(0); // Reset failed attempts on successful login
        userRepository.save(user);
        
        // Explicitly load user roles from the repository
        List<UserRole> activeUserRoles = userRoleRepository.findActiveUserRoles(user, LocalDateTime.now());
        
        // Extract role codes from explicitly loaded roles
        List<String> roleCodes = activeUserRoles.stream()
                .map(userRole -> userRole.getRole().getCode())
                .toList();
        
        // Create authorities from the active roles
        List<SimpleGrantedAuthority> authorities = activeUserRoles.stream()
                .map(userRole -> new SimpleGrantedAuthority("ROLE_" + userRole.getRole().getCode()))
                .collect(Collectors.toList());
        
        // Generate JWT token with explicit authorities
        String jwt = jwtUtils.generateTokenWithAuthorities(user, authorities, user.getId());
                
        // Log the found roles for debugging
        log.debug("Loaded {} active roles for user {}: {}", 
                roleCodes.size(), user.getUsername(), roleCodes);
                
        // Return response with user details and role codes
        return LoginResponse.builder()
                .token(jwt)
                .username(user.getUsername())
                .email(user.getEmail())
                .mobileCountryCode(user.getMobileCountryCode())
                .mobileNumber(user.getMobileNumber())
                .nickname(user.getNickname())
                .accountStatus(user.getAccountStatus())
                .lastLoginAt(user.getLastLoginAt())
                .lastLoginIp(user.getLastLoginIp())
                .lastFailedLoginAt(user.getLastFailedLoginAt())
                .failedLoginAttempts(user.getFailedLoginAttempts())
                .roleCodes(roleCodes)
                .build();
    }

    /**
     * Enhanced login method that returns both access and refresh tokens
     * This is the new authentication method that supports token rotation
     */
    @Transactional
    public AuthTokenResponse loginWithDualTokens(LoginRequest loginRequest) {
        // Validate login request has only one authentication method
        if (!loginRequest.isValidLoginMethod()) {
            throw new BadCredentialsException("Please provide exactly one login method: username, email, or mobile number");
        }

        String principal;
        if (loginRequest.getEmail() != null && !loginRequest.getEmail().isEmpty()) {
            principal = loginRequest.getEmail();
        } else if (loginRequest.getUsername() != null && !loginRequest.getUsername().isEmpty()) {
            principal = loginRequest.getUsername();
        } else {
            // For mobile login, combine country code and mobile number as principal
            principal = loginRequest.getMobileCountryCode() + "-" + loginRequest.getMobileNumber();
        }
        
        // Create authentication token
        UsernamePasswordAuthenticationToken authToken = 
            new UsernamePasswordAuthenticationToken(principal, loginRequest.getPassword());
        authToken.setDetails(loginRequest);
        
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(authToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // Get the authenticated user
        User user = (User) authentication.getPrincipal();
        
        // Get client IP address for login tracking
        String clientIp = getClientIp();
        
        // Update last login timestamp and IP
        LocalDateTime now = LocalDateTime.now();
        user.setLastLoginAt(now);
        user.setLastLoginIp(clientIp);
        user.setFailedLoginAttempts(0);
        userRepository.save(user);
        
        // Load user roles
        List<UserRole> activeUserRoles = userRoleRepository.findActiveUserRoles(user, LocalDateTime.now());
        List<String> roleCodes = activeUserRoles.stream()
                .map(userRole -> userRole.getRole().getCode())
                .toList();
        
        // Create authorities
        List<SimpleGrantedAuthority> authorities = activeUserRoles.stream()
                .map(userRole -> new SimpleGrantedAuthority("ROLE_" + userRole.getRole().getCode()))
                .collect(Collectors.toList());
        
        // Generate access token
        String accessToken = jwtUtils.generateTokenWithAuthorities(user, authorities, user.getId());
        
        // Generate refresh token using RefreshTokenService
        RefreshToken refreshTokenEntity = refreshTokenService.createRefreshToken(user.getId());
        String refreshToken = refreshTokenEntity.getTokenValue();
        
        log.debug("Generated dual tokens for user {}: access token and refresh token", user.getUsername());
        
        // Return enhanced response with both tokens
        return AuthTokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtils.extractExpirationTimestamp(accessToken) - System.currentTimeMillis())
                .username(user.getUsername())
                .email(user.getEmail())
                .mobileCountryCode(user.getMobileCountryCode())
                .mobileNumber(user.getMobileNumber())
                .nickname(user.getNickname())
                .accountStatus(user.getAccountStatus())
                .lastLoginAt(user.getLastLoginAt())
                .lastLoginIp(user.getLastLoginIp())
                .roleCodes(roleCodes)
                .build();
    }

    /**
     * Refresh access token using a valid refresh token
     * Implements token rotation - old refresh token is revoked and new tokens are issued
     */
    @Transactional
    public TokenRefreshResponse refreshToken(RefreshTokenRequest request) {
        String refreshTokenValue = request.getRefreshToken();
        
        // Validate refresh token
        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(refreshTokenValue)
                .orElseThrow(() -> new BadCredentialsException("Invalid or expired refresh token"));
        
        // Get user details
        User user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", refreshToken.getUserId()));
        
        // Check if user account is still active and valid
        if (!user.isActive() || user.getAccountStatus() != AccountStatus.active) {
            // Revoke the refresh token for security
            refreshTokenService.revokeRefreshToken(refreshTokenValue);
            throw new BadCredentialsException("User account is not active");
        }
        
        // Load user roles
        List<UserRole> activeUserRoles = userRoleRepository.findActiveUserRoles(user, LocalDateTime.now());
        List<String> roleCodes = activeUserRoles.stream()
                .map(userRole -> userRole.getRole().getCode())
                .toList();
        
        // Create authorities
        List<SimpleGrantedAuthority> authorities = activeUserRoles.stream()
                .map(userRole -> new SimpleGrantedAuthority("ROLE_" + userRole.getRole().getCode()))
                .collect(Collectors.toList());
        
        // Rotate tokens - revoke old refresh token and create new tokens
        RefreshToken newRefreshTokenEntity = refreshTokenService.rotateRefreshToken(refreshTokenValue, refreshToken.getUserId());
        String newRefreshToken = newRefreshTokenEntity.getTokenValue();
        
        // Generate new access token
        String newAccessToken = jwtUtils.generateAccessTokenForRefresh(user, authorities, user.getId());
        
        log.debug("Token refresh successful for user {}: rotated refresh token and generated new access token", user.getUsername());
        
        return TokenRefreshResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtils.extractExpirationTimestamp(newAccessToken) - System.currentTimeMillis())
                .build();
    }

    /**
     * Enhanced logout method that revokes both access and refresh tokens
     */
    @Transactional
    public void enhancedLogout(LogoutRequest logoutRequest, HttpServletRequest request) {
        try {
            // Extract and blacklist access token
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String accessToken = authHeader.substring(7);
                
                // Extract token information for blacklisting
                String tokenId = jwtUtils.extractTokenId(accessToken);
                long expirationTime = jwtUtils.extractExpirationTimestamp(accessToken);
                
                if (tokenId != null) {
                    tokenBlacklistService.blacklistToken(tokenId, expirationTime);
                    log.debug("Access token blacklisted: {}", tokenId);
                }
            }
            
            // Revoke refresh token if provided
            if (logoutRequest.getRefreshToken() != null && !logoutRequest.getRefreshToken().trim().isEmpty()) {
                refreshTokenService.revokeRefreshToken(logoutRequest.getRefreshToken());
                log.debug("Refresh token revoked during logout");
            }
            
        } catch (Exception e) {
            log.warn("Failed to fully process logout tokens: {}", e.getMessage());
            // Continue with logout even if token revocation fails
        }
        
        // Clear the security context
        SecurityContextHolder.clearContext();
        log.debug("Enhanced logout completed successfully");
    }
    
    /**
     * Get the client's IP address from the current request
     */
    private String getClientIp() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return "unknown";
            }
            
            HttpServletRequest request = attributes.getRequest();
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return xForwardedFor.split(",")[0].trim();
            }
            return request.getRemoteAddr();
        } catch (Exception e) {
            return "unknown";
        }
    }

    /**
     * Register a new user with ROLE_USER role
     *
     * @param signupRequest The signup request data
     * @return The created user data
     */
    @Transactional
    public SignupResponse signup(SignupRequest signupRequest) {
        // Check if username already exists
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            throw new IllegalArgumentException("Username is already taken");
        }

        // Check if email already exists
        if (signupRequest.getEmail() != null && userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        // Check if mobile number already exists
        if (signupRequest.getMobileCountryCode() != null && signupRequest.getMobileNumber() != null &&
            userRepository.existsByMobileCountryCodeAndMobileNumber(
                signupRequest.getMobileCountryCode(), signupRequest.getMobileNumber())) {
            throw new IllegalArgumentException("Mobile number is already registered");
        }

        // Get the USER role
        Role userRole = roleRepository.findByCode("USER")
                .orElseThrow(() -> new ResourceNotFoundException("Role", "code", "USER"));

        // Validate that username is provided (now required field)
        if (signupRequest.getUsername() == null || signupRequest.getUsername().isEmpty()) {
            throw new IllegalArgumentException("Username is required and must be provided");
        }

        // Create new user account with all fields explicitly set to avoid missing defaults
        String uuid = UUID.randomUUID().toString();
        User user = User.builder()
                .id(uuid)
                .username(signupRequest.getUsername())
                .email(signupRequest.getEmail())
                .mobileCountryCode(signupRequest.getMobileCountryCode())
                .mobileNumber(signupRequest.getMobileNumber())
                .password(passwordEncoder.encode(signupRequest.getPassword()))
                .nickname(signupRequest.getNickname())
                .accountStatus(AccountStatus.pending_verification)
                .passwordChangedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdBy(uuid)
                .updatedBy(uuid)
                .build();

        User savedUser = userRepository.save(user);

        // Create user role relationship
        UserRole userRoleEntity = UserRole.builder()
                .user(savedUser)
                .role(userRole)
                .grantedAt(LocalDateTime.now())
                .grantedBy(uuid)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdBy(uuid)
                .updatedBy(uuid)
                .active(true)
                .build();

        userRoleRepository.save(userRoleEntity);

        return SignupResponse.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .mobileCountryCode(savedUser.getMobileCountryCode())
                .mobileNumber(savedUser.getMobileNumber())
                .nickname(savedUser.getNickname())
                .accountStatus(savedUser.getAccountStatus())
                .active(savedUser.isActive())
                .build();
    }
    
    /**
     * Logout the current user by blacklisting their JWT token
     * This method extracts the JWT token from the request and adds it to the blacklist
     */
    @Transactional
    public void logout(HttpServletRequest request) {
        try {
            // Extract JWT token from Authorization header
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7); // Remove "Bearer " prefix
                
                // Extract token information for blacklisting
                String tokenId = jwtUtils.extractTokenId(token);
                long expirationTime = jwtUtils.extractExpirationTimestamp(token);
                
                if (tokenId != null && tokenBlacklistService != null) {
                    // Add token to blacklist
                    tokenBlacklistService.blacklistToken(tokenId, expirationTime);
                    log.debug("Token blacklisted successfully for user logout: {}", tokenId);
                } else {
                    log.warn("Could not blacklist token: tokenId={}, blacklistService={}", 
                            tokenId, tokenBlacklistService != null ? "available" : "null");
                }
            } else {
                log.debug("No Bearer token found in Authorization header for logout");
            }
        } catch (Exception e) {
            log.warn("Failed to blacklist token during logout: {}", e.getMessage());
            // Continue with logout even if blacklisting fails
        }
        
        // Clear the security context to invalidate the current authentication
        SecurityContextHolder.clearContext();
        log.debug("User logged out successfully, security context cleared");
    }
}