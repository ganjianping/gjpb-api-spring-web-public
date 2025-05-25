package org.ganjp.blog.am.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ganjp.blog.am.model.dto.request.LoginRequest;
import org.ganjp.blog.am.model.dto.request.SignupRequest;
import org.ganjp.blog.am.model.dto.response.LoginResponse;
import org.ganjp.blog.am.model.dto.response.SignupResponse;
import org.ganjp.blog.am.model.entity.Role;
import org.ganjp.blog.am.model.entity.User;
import org.ganjp.blog.am.model.entity.UserRole;
import org.ganjp.blog.am.model.enums.AccountStatus;
import org.ganjp.blog.am.repository.RoleRepository;
import org.ganjp.blog.am.repository.UserRepository;
import org.ganjp.blog.am.repository.UserRoleRepository;
import org.ganjp.blog.am.security.JwtUtils;
import org.ganjp.blog.common.exception.ResourceNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.UUID;

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
        
        // Determine the principal for authentication
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
        
        // Generate JWT token
        String jwt = jwtUtils.generateToken(user);
        
        // Get client IP address for login tracking
        String clientIp = getClientIp();
        
        // Update last login timestamp and IP
        LocalDateTime now = LocalDateTime.now();
        user.setLastLoginAt(now);
        user.setLastLoginIp(clientIp);
        user.setFailedLoginAttempts(0); // Reset failed attempts on successful login
        userRepository.save(user);
        
        // Return response with user details
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
                .build();
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
        if (signupRequest.getUsername() != null && userRepository.existsByUsername(signupRequest.getUsername())) {
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
}