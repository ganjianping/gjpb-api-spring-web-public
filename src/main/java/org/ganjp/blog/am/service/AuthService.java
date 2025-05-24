package org.ganjp.blog.am.service;

import lombok.RequiredArgsConstructor;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    public LoginResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwt = jwtUtils.generateToken(userDetails);

        return LoginResponse.builder()
                .token(jwt)
                .build();
    }

    /**
     * Register a new user with ROLE_USER role and email verification.
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
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        // Get the USER role
        Role userRole = roleRepository.findByCode("USER")
                .orElseThrow(() -> new ResourceNotFoundException("Role", "code", "USER"));

        // Generate email verification token
        String verificationToken = generateVerificationToken();
        LocalDateTime verificationExpiry = LocalDateTime.now().plusHours(24); // 24 hours to verify

        // Create new user account with all fields explicitly set to avoid missing defaults
        String uuid = UUID.randomUUID().toString();
        User user = User.builder()
                .id(uuid)
                .username(signupRequest.getUsername())
                .email(signupRequest.getEmail())
                .firstName(signupRequest.getFirstName())
                .lastName(signupRequest.getLastName())
                .password(passwordEncoder.encode(signupRequest.getPassword()))
                .accountStatus(AccountStatus.pending_verification)
                .verificationToken(verificationToken)
                .verificationTokenExpiresAt(verificationExpiry)
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
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .accountStatus(savedUser.getAccountStatus())
                .active(savedUser.isActive()) // Backward compatibility
                .message("Registration successful. Please check your email to verify your account.")
                .build();
    }

    /**
     * Generate a secure verification token for email verification
     */
    private String generateVerificationToken() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}