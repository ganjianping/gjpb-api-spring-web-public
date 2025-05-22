package org.ganjp.blog.am.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filter that intercepts each request to validate JWT tokens and authenticate users.
 * This filter extracts JWT tokens from Authorization headers, validates them,
 * and sets the authenticated user in the Spring Security context.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;
    
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    // Modified constructor to accept UserDetailsService directly to avoid circular dependency
    public JwtAuthenticationFilter(JwtUtils jwtUtils, 
                                 @Autowired(required = false) UserDetailsService userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        // Skip JWT processing for OPTIONS requests (CORS preflight)
        if (request.getMethod().equals("OPTIONS")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        final String jwt;
        final String username;

        // Check if Authorization header exists and has Bearer token
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract token (remove "Bearer " prefix)
        jwt = authHeader.substring(BEARER_PREFIX.length());
        
        try {
            username = jwtUtils.extractUsername(jwt);
            
            // Authenticate user if token has username and no authentication exists yet 
            // and userDetailsService is not null
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null 
                && userDetailsService != null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                
                // Validate token and set authentication if valid
                if (jwtUtils.isTokenValid(jwt, userDetails)) {
                    List<GrantedAuthority> authorities = jwtUtils.extractAuthorities(jwt);

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            authorities
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Log error but do not block the request
            logger.error("JWT authentication failed: " + e.getMessage(), e);
        }
        
        filterChain.doFilter(request, response);
    }
}