package org.ganjp.blog.am.config;

import org.ganjp.blog.am.security.CustomAuthenticationProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to verify that SecurityConfig is properly using the CustomAuthenticationProvider
 */
@SpringBootTest
@ActiveProfiles("test")
class SecurityConfigCustomProviderTest {

    @Autowired
    private AuthenticationProvider authenticationProvider;

    @Autowired
    private CustomAuthenticationProvider customAuthenticationProvider;

    @Test
    void shouldUseCustomAuthenticationProvider() {
        // Verify that the injected AuthenticationProvider is actually our CustomAuthenticationProvider
        assertNotNull(authenticationProvider, "AuthenticationProvider should not be null");
        assertNotNull(customAuthenticationProvider, "CustomAuthenticationProvider should not be null");
        
        // The authenticationProvider bean should be the same instance as customAuthenticationProvider
        assertSame(customAuthenticationProvider, authenticationProvider, 
                   "SecurityConfig should return the CustomAuthenticationProvider instance");
        
        // Verify it's the right type
        assertTrue(authenticationProvider instanceof CustomAuthenticationProvider,
                   "AuthenticationProvider should be an instance of CustomAuthenticationProvider");
    }
}
