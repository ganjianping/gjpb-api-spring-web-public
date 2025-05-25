package org.ganjp.blog.am.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ganjp.blog.am.model.dto.request.LoginRequest;
import org.ganjp.blog.am.model.entity.User;
import org.ganjp.blog.am.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String credential = authentication.getName();
        String password = authentication.getCredentials().toString();
        
        // Extract login request from authentication details
        LoginRequest loginRequest = extractLoginRequest(authentication);
        
        // Find the user based on provided credentials
        Optional<User> userOptional = findUser(credential, loginRequest);
        
        // If user not found, update failed login metrics and throw exception
        if (userOptional.isEmpty()) {
            // Record login failure for unknown user
            updateLoginFailureForUnknownUser(loginRequest, credential);
            throw new BadCredentialsException("Invalid username or password");
        }
        
        User user = userOptional.get();
        
        // Verify password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            // Record login failure for known user with wrong password
            updateLoginFailureForKnownUser(user);
            throw new BadCredentialsException("Invalid username or password");
        }
        
        // Check account status
        validateAccountStatus(user);
        
        // Return the authenticated token with authorities
        return new UsernamePasswordAuthenticationToken(
                user, 
                password, 
                user.getAuthorities()
        );
    }

    private LoginRequest extractLoginRequest(Authentication authentication) {
        if (authentication.getDetails() instanceof LoginRequest request) {
            return request;
        }
        return null;
    }
    
    private Optional<User> findUser(String credential, LoginRequest loginRequest) {
        if (loginRequest != null) {
            // First validate that only one login method is provided
            if (!loginRequest.isValidLoginMethod()) {
                throw new BadCredentialsException("Please provide exactly one login method: username, email, or mobile number");
            }
            
            if (loginRequest.getEmail() != null && !loginRequest.getEmail().isEmpty()) {
                // Email login
                return userRepository.findByEmail(loginRequest.getEmail());
            } else if (loginRequest.getMobileNumber() != null && !loginRequest.getMobileNumber().isEmpty()
                    && loginRequest.getMobileCountryCode() != null && !loginRequest.getMobileCountryCode().isEmpty()) {
                // Mobile login
                return userRepository.findByMobileCountryCodeAndMobileNumber(
                        loginRequest.getMobileCountryCode(), loginRequest.getMobileNumber());
            } else {
                // Username login
                return userRepository.findByUsername(loginRequest.getUsername());
            }
        } else {
            // Default to username login if no loginRequest is provided
            Optional<User> userOptional = userRepository.findByUsername(credential);
            
            // If not found, try with email as a fallback (if it looks like an email)
            if (userOptional.isEmpty() && credential != null && credential.contains("@")) {
                userOptional = userRepository.findByEmail(credential);
            }
            return userOptional;
        }
    }
    
    private void updateLoginFailureForUnknownUser(LoginRequest loginRequest, String credential) {
        String username = null;
        String email = null;
        String mobileCountryCode = null;
        String mobileNumber = null;
        
        // Extract identifiers from login request or credential
        if (loginRequest != null) {
            if (loginRequest.getEmail() != null && !loginRequest.getEmail().isEmpty()) {
                email = loginRequest.getEmail();
            } else if (loginRequest.getMobileNumber() != null && !loginRequest.getMobileNumber().isEmpty()
                    && loginRequest.getMobileCountryCode() != null && !loginRequest.getMobileCountryCode().isEmpty()) {
                mobileCountryCode = loginRequest.getMobileCountryCode();
                mobileNumber = loginRequest.getMobileNumber();
            } else {
                username = loginRequest.getUsername();
            }
        } else {
            username = credential;
            // If it looks like an email, treat it as one as well
            if (username != null && username.contains("@")) {
                email = username;
            }
        }
        
        // Update failed login metrics 
        try {
            LocalDateTime now = LocalDateTime.now();
            int rowsUpdated = userRepository.updateLoginFailure(username, email, mobileCountryCode, mobileNumber, now);
            log.info("Updated failed login metrics for user not found: username={}, email={}, mobile={}, rowsUpdated={}, timestamp={}", 
                     username, email, mobileNumber != null ? (mobileCountryCode + mobileNumber) : null, rowsUpdated, now);
            
            if (rowsUpdated == 0) {
                log.warn("No rows were updated when attempting to record login failure. This may indicate a problem with the query or that no matching users were found.");
            }
        } catch (Exception e) {
            log.error("Failed to update login failure metrics", e);
        }
    }
    
    private void updateLoginFailureForKnownUser(User user) {
        try {
            // Update failed login metrics
            LocalDateTime now = LocalDateTime.now();
            int rowsUpdated = userRepository.updateLoginFailure(user.getUsername(), user.getEmail(), 
                                            user.getMobileCountryCode(), user.getMobileNumber(), now);
            log.info("Updated failed login metrics for invalid password: user={}, rowsUpdated={}, timestamp={}", 
                    user.getUsername(), rowsUpdated, now);
            
            if (rowsUpdated == 0) {
                log.warn("No rows were updated when attempting to record login failure for known user {}. This may indicate a problem with the query.", user.getUsername());
            }
        } catch (Exception e) {
            log.error("Failed to update login failure metrics", e);
        }
    }
    
    private void validateAccountStatus(User user) {
        if (!user.isEnabled()) {
            throw new BadCredentialsException("Account is not active");
        }
        
        if (!user.isAccountNonLocked()) {
            throw new BadCredentialsException("Account is locked");
        }

        if (!user.isAccountNonExpired()) {
            throw new BadCredentialsException("Account has expired");
        }
        
        if (!user.isCredentialsNonExpired()) {
            throw new BadCredentialsException("Credentials have expired");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
