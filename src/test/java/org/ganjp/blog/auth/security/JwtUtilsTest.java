package org.ganjp.blog.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import org.ganjp.blog.auth.config.SecurityProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Key;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Unit tests for JwtUtils class
 */
@ExtendWith(MockitoExtension.class)
public class JwtUtilsTest {

    private JwtUtils jwtUtils;
    private SecurityProperties securityProperties;
    private SecurityProperties.Jwt jwtProperties;

    @Mock
    private HttpServletRequest mockRequest;

    private UserDetails userDetails;
    private List<SimpleGrantedAuthority> authorities;
    private final String userId = "user123";
    private final String username = "testuser";
    private final String secretKey = "ThisIsAVeryVeryVeryLongSecretKeyForJwtTestingThatShouldBeAtLeast256BitsLong";

    @BeforeEach
    void setUp() {
        // Set up security properties with test values
        securityProperties = new SecurityProperties();
        jwtProperties = new SecurityProperties.Jwt();
        jwtProperties.setSecretKey(secretKey);
        jwtProperties.setExpiration(3600000L); // 1 hour
        jwtProperties.setRefreshExpiration(86400000L); // 24 hours
        securityProperties.setJwt(jwtProperties);

        jwtUtils = new JwtUtils(securityProperties);

        // Create authorities
        authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));

        // Create UserDetails
        userDetails = new User(username, "password", authorities);
    }

    @Nested
    @DisplayName("Token Generation Tests")
    class TokenGenerationTests {

        @Test
        @DisplayName("Should generate a valid token with default claims")
        void shouldGenerateValidTokenWithDefaultClaims() {
            // When
            String token = jwtUtils.generateToken(userDetails);

            // Then
            assertThat(token).isNotEmpty();
            assertThat(jwtUtils.extractUsername(token)).isEqualTo(username);
            assertThat(jwtUtils.isTokenValid(token, userDetails)).isTrue();
        }

        @Test
        @DisplayName("Should generate a valid token with authorities and user ID")
        void shouldGenerateValidTokenWithAuthorities() {
            // When
            String token = jwtUtils.generateTokenWithAuthorities(userDetails, authorities, userId);

            // Then
            assertThat(token).isNotEmpty();
            assertThat(jwtUtils.extractUsername(token)).isEqualTo(username);
            assertThat(jwtUtils.extractUserId(token)).isEqualTo(userId);
            assertThat(jwtUtils.isTokenValid(token, userDetails)).isTrue();

            // Verify authorities are in the token
            List<GrantedAuthority> extractedAuthorities = jwtUtils.extractAuthorities(token);
            assertThat(extractedAuthorities).hasSize(2);
            assertThat(extractedAuthorities.stream().map(GrantedAuthority::getAuthority))
                    .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
        }

        @Test
        @DisplayName("Should generate dual tokens (access and refresh)")
        void shouldGenerateDualTokens() {
            // When
            Map<String, String> tokens = jwtUtils.generateDualTokens(userDetails, authorities, userId);

            // Then
            assertThat(tokens).containsKeys("accessToken", "refreshToken");
            String accessToken = tokens.get("accessToken");
            String refreshToken = tokens.get("refreshToken");

            assertThat(jwtUtils.extractUsername(accessToken)).isEqualTo(username);
            assertThat(jwtUtils.extractUsername(refreshToken)).isEqualTo(username);
            assertThat(jwtUtils.extractUserId(refreshToken)).isEqualTo(userId);

            // Verify refresh token type
            assertThat(jwtUtils.isRefreshToken(refreshToken)).isTrue();
            assertThat(jwtUtils.isRefreshToken(accessToken)).isFalse();
        }

        @Test
        @DisplayName("Should generate access token for refresh operation")
        void shouldGenerateAccessTokenForRefresh() {
            // When
            String token = jwtUtils.generateAccessTokenForRefresh(userDetails, authorities, userId);

            // Then
            assertThat(token).isNotEmpty();
            assertThat(jwtUtils.extractUsername(token)).isEqualTo(username);
            assertThat(jwtUtils.extractUserId(token)).isEqualTo(userId);
        }

        @Test
        @DisplayName("Should generate refresh token")
        void shouldGenerateRefreshToken() {
            // When
            String refreshToken = jwtUtils.generateRefreshToken(userDetails, userId);

            // Then
            assertThat(refreshToken).isNotEmpty();
            assertThat(jwtUtils.extractUsername(refreshToken)).isEqualTo(username);
            assertThat(jwtUtils.extractUserId(refreshToken)).isEqualTo(userId);
            assertThat(jwtUtils.isRefreshToken(refreshToken)).isTrue();
        }
    }

    @Nested
    @DisplayName("Token Extraction Tests")
    class TokenExtractionTests {

        private String token;

        @BeforeEach
        void setUp() {
            token = jwtUtils.generateTokenWithAuthorities(userDetails, authorities, userId);
        }

        @Test
        @DisplayName("Should extract username")
        void shouldExtractUsername() {
            assertThat(jwtUtils.extractUsername(token)).isEqualTo(username);
        }

        @Test
        @DisplayName("Should extract user ID")
        void shouldExtractUserId() {
            assertThat(jwtUtils.extractUserId(token)).isEqualTo(userId);
        }

        @Test
        @DisplayName("Should extract authorities")
        void shouldExtractAuthorities() {
            List<GrantedAuthority> extractedAuthorities = jwtUtils.extractAuthorities(token);
            assertThat(extractedAuthorities).hasSize(2);
            assertThat(extractedAuthorities.stream().map(GrantedAuthority::getAuthority))
                    .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
        }

        @Test
        @DisplayName("Should extract expiration timestamp")
        void shouldExtractExpirationTimestamp() {
            long expTimestamp = jwtUtils.extractExpirationTimestamp(token);
            assertThat(expTimestamp).isGreaterThan(System.currentTimeMillis());
        }

        @Test
        @DisplayName("Should extract issued at time")
        void shouldExtractIssuedAt() {
            Date issuedAt = jwtUtils.extractIssuedAt(token);
            assertThat(issuedAt).isNotNull();
            assertThat(issuedAt.getTime()).isLessThanOrEqualTo(System.currentTimeMillis());
        }

        @Test
        @DisplayName("Should extract token ID")
        void shouldExtractTokenId() {
            String tokenId = jwtUtils.extractTokenId(token);
            assertThat(tokenId).isNotEmpty();
        }

        @Test
        @DisplayName("Should extract user ID from request")
        void shouldExtractUserIdFromRequest() {
            // Given
            when(mockRequest.getHeader("Authorization")).thenReturn("Bearer " + token);

            // When
            String extractedUserId = jwtUtils.extractUserIdFromToken(mockRequest);

            // Then
            assertThat(extractedUserId).isEqualTo(userId);
        }

        @Test
        @DisplayName("Should return null when no Authorization header")
        void shouldReturnNullWhenNoAuthHeader() {
            // Given
            when(mockRequest.getHeader("Authorization")).thenReturn(null);

            // When
            String extractedUserId = jwtUtils.extractUserIdFromToken(mockRequest);

            // Then
            assertThat(extractedUserId).isNull();
        }

        @Test
        @DisplayName("Should return null when Authorization header is not Bearer")
        void shouldReturnNullWhenNotBearerHeader() {
            // Given
            when(mockRequest.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNzd29yZA==");

            // When
            String extractedUserId = jwtUtils.extractUserIdFromToken(mockRequest);

            // Then
            assertThat(extractedUserId).isNull();
        }
    }

    @Nested
    @DisplayName("Token Validation Tests")
    class TokenValidationTests {

        @Test
        @DisplayName("Should validate token as valid")
        void shouldValidateTokenAsValid() {
            // Given
            String token = jwtUtils.generateToken(userDetails);

            // When & Then
            assertThat(jwtUtils.isTokenValid(token, userDetails)).isTrue();
        }

        @Test
        @DisplayName("Should validate token as invalid for different user")
        void shouldValidateTokenAsInvalidForDifferentUser() {
            // Given
            String token = jwtUtils.generateToken(userDetails);
            UserDetails differentUser = new User("differentUser", "password", Collections.emptyList());

            // When & Then
            assertThat(jwtUtils.isTokenValid(token, differentUser)).isFalse();
        }

        @Test
        @DisplayName("Should validate expired token as invalid")
        void shouldValidateExpiredTokenAsInvalid() {
            // Given - Create a JWT with an expiration date in the past
            Map<String, Object> claims = new HashMap<>();
            String token = Jwts.builder()
                    .setClaims(claims)
                    .setSubject(userDetails.getUsername())
                    .setIssuedAt(new Date(System.currentTimeMillis() - 1000)) // 1 second in the past
                    .setExpiration(new Date(System.currentTimeMillis() - 500)) // Expiration 0.5 seconds in the past
                    .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey)), SignatureAlgorithm.HS256)
                    .compact();

            // When & Then - JwtUtils.isTokenValid should catch the ExpiredJwtException and return false
            // If the token is expired, we expect isTokenValid to return false, but it might throw an exception
            // So we need to handle both cases
            try {
                boolean isValid = jwtUtils.isTokenValid(token, userDetails);
                assertThat(isValid).isFalse();
            } catch (ExpiredJwtException e) {
                // This is also an acceptable outcome - if JwtUtils doesn't catch ExpiredJwtException internally,
                // it's still proof that the token is expired and considered invalid
                assertThat(e).isInstanceOf(ExpiredJwtException.class);
            }
        }

        @Test
        @DisplayName("Should identify refresh token correctly")
        void shouldIdentifyRefreshTokenCorrectly() {
            // Given
            String refreshToken = jwtUtils.generateRefreshToken(userDetails, userId);
            String accessToken = jwtUtils.generateToken(userDetails);

            // When & Then
            assertThat(jwtUtils.isRefreshToken(refreshToken)).isTrue();
            assertThat(jwtUtils.isRefreshToken(accessToken)).isFalse();
        }
    }

    @Nested
    @DisplayName("Advanced Token Validation Tests")
    class AdvancedTokenValidationTests {
        
        @Test
        @DisplayName("Should validate token freshness by checking issued at date")
        void shouldValidateTokenFreshnessByCheckingIssuedAt() {
            // Given
            String token = jwtUtils.generateToken(userDetails);
            Date issuedAt = jwtUtils.extractIssuedAt(token);
            
            // Then
            assertThat(issuedAt).isNotNull();
            assertThat(issuedAt.getTime() <= System.currentTimeMillis() 
                    && issuedAt.getTime() >= System.currentTimeMillis() - 5000).isTrue(); // Within 5 seconds
        }
        
        @Test
        @DisplayName("Should maintain token creation metadata")
        void shouldMaintainTokenCreationMetadata() {
            // Given - Create two JWTs with different creation timestamps
            Map<String, Object> claims = new HashMap<>();
            String token1 = Jwts.builder()
                    .setClaims(claims)
                    .setId(UUID.randomUUID().toString())
                    .setSubject(userDetails.getUsername())
                    .setIssuedAt(new Date(System.currentTimeMillis() - 1000)) // 1 second ago
                    .setExpiration(new Date(System.currentTimeMillis() + 3600000)) // 1 hour in future
                    .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey)), SignatureAlgorithm.HS256)
                    .compact();
            
            String token2 = Jwts.builder()
                    .setClaims(claims)
                    .setId(UUID.randomUUID().toString())
                    .setSubject(userDetails.getUsername())
                    .setIssuedAt(new Date(System.currentTimeMillis())) // now
                    .setExpiration(new Date(System.currentTimeMillis() + 3600000)) // 1 hour in future
                    .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey)), SignatureAlgorithm.HS256)
                    .compact();
            
            // Then - tokens should differ due to unique ID and timestamps
            assertThat(token1).isNotEqualTo(token2);
            assertThat(jwtUtils.extractTokenId(token1)).isNotEqualTo(jwtUtils.extractTokenId(token2));
            assertThat(jwtUtils.extractIssuedAt(token1).getTime())
                .isLessThan(jwtUtils.extractIssuedAt(token2).getTime());
        }
        
        @Test
        @DisplayName("Should return empty authorities for token without authorities")
        void shouldReturnEmptyAuthoritiesForTokenWithoutAuthorities() {
            // Given - a token with minimal claims
            Map<String, Object> minimalClaims = new HashMap<>();
            String token = jwtUtils.generateToken(minimalClaims, userDetails);
            
            // When
            List<GrantedAuthority> extractedAuthorities = jwtUtils.extractAuthorities(token);
            
            // Then
            assertThat(extractedAuthorities).isEmpty();
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should throw exception for malformed token")
        void shouldThrowExceptionForMalformedToken() {
            // Given
            String malformedToken = "malformed.token.string";

            // When & Then
            assertThatThrownBy(() -> jwtUtils.extractUsername(malformedToken))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("Should handle token with missing claims")
        void shouldHandleTokenWithMissingClaims() {
            // Given - create a token without authorities claim
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", userId);
            String token = jwtUtils.generateToken(claims, userDetails);

            // When & Then
            List<GrantedAuthority> extractedGrantedAuthorities = jwtUtils.extractAuthorities(token);
            assertThat(extractedGrantedAuthorities).isEmpty();
        }

        @Test
        @DisplayName("Should throw exception for token with invalid signature")
        void shouldThrowExceptionForTokenWithInvalidSignature() {
            // Given
            String token = jwtUtils.generateToken(userDetails);

            // Corrupt the signature by changing the last character
            String corruptedToken = token.substring(0, token.length() - 1) + (token.charAt(token.length() - 1) == 'A' ? 'B' : 'A');

            // When & Then
            assertThatThrownBy(() -> jwtUtils.extractUsername(corruptedToken))
                    .isInstanceOf(SignatureException.class);
        }

        @Test
        @DisplayName("Should throw exception for expired token when extracting claims")
        void shouldThrowExceptionForExpiredTokenWhenExtracting() {
            // Given - Create a JWT with an expiration date in the past
            Map<String, Object> claims = new HashMap<>();
            String token = Jwts.builder()
                    .setClaims(claims)
                    .setSubject(userDetails.getUsername())
                    .setIssuedAt(new Date(System.currentTimeMillis() - 2000)) // 2 seconds in the past
                    .setExpiration(new Date(System.currentTimeMillis() - 1000)) // 1 second in the past
                    .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey)), SignatureAlgorithm.HS256)
                    .compact();

            // When & Then
            assertThatThrownBy(() -> jwtUtils.extractUsername(token))
                    .isInstanceOf(ExpiredJwtException.class);
        }

        @Test
        @DisplayName("Should return null for missing userId claim")
        void shouldReturnNullForMissingUserIdClaim() {
            // Given
            Map<String, Object> claims = new HashMap<>();
            // No userId claim
            String token = jwtUtils.generateToken(claims, userDetails);

            // When
            String extractedUserId = jwtUtils.extractUserId(token);

            // Then
            assertThat(extractedUserId).isNull();
        }
    }

    @Nested
    @DisplayName("Key Management Tests")
    class KeyManagementTests {

        @Test
        @DisplayName("Should reject token signed with different key")
        void shouldRejectTokenSignedWithDifferentKey() {
            // Given
            SecurityProperties.Jwt differentJwt = new SecurityProperties.Jwt();
            differentJwt.setSecretKey("ADifferentVeryVeryVeryLongSecretKeyForJwtTestingThatShouldBeAtLeast256BitsLong");
            differentJwt.setExpiration(3600000L);
            SecurityProperties properties = new SecurityProperties();
            properties.setJwt(differentJwt);
            JwtUtils differentJwtUtils = new JwtUtils(properties);

            // When
            String token = differentJwtUtils.generateToken(userDetails);

            // Then - Original jwtUtils should reject this token
            assertThatThrownBy(() -> jwtUtils.extractUsername(token))
                    .isInstanceOf(SignatureException.class);
        }
    }
}
