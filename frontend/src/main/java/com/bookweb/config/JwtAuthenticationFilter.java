package com.bookweb.config;

import com.bookweb.util.TokenUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Gson gson = new Gson();
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        logger.debug("JwtAuthenticationFilter: Processing request: {}", request.getRequestURI());
        
        try {
            // Get token from request (checks Authorization header, session, and cookies)
            String token = TokenUtil.getTokenFromRequest();
            
            logger.debug("Token: {}", token != null ? "FOUND" : "NOT FOUND");
            
            if (token != null && !token.isEmpty()) {
                try {
                    // Decode JWT payload
                    String[] parts = token.split("\\.");
                    if (parts.length == 3) {
                        // Decode payload
                        String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
                        JsonObject payloadJson = gson.fromJson(payload, JsonObject.class);
                        
                        String userId = payloadJson.get("id").getAsString();
                        // Read role directly from JWT payload (most reliable source)
                        String jwtRole = payloadJson.has("role") ? payloadJson.get("role").getAsString() : null;
                        logger.debug("Decoded userId: {}, role: {} from JWT", userId, jwtRole);
                        
                        // Fallback to session/cookie role if not in JWT
                        if (jwtRole == null) {
                            jwtRole = TokenUtil.getUserRoleFromRequest();
                        }
                        
                        // Create authorities list
                        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                        
                        // Add admin role if user is admin
                        if (jwtRole != null && jwtRole.equalsIgnoreCase("admin")) {
                            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                            logger.info("Added ROLE_ADMIN for user {}", userId);
                        }
                        
                        // Create and set authentication
                        UsernamePasswordAuthenticationToken auth = 
                            new UsernamePasswordAuthenticationToken(userId, token, authorities);
                        
                        SecurityContextHolder.getContext().setAuthentication(auth);
                        logger.info("Authentication set for user {} in SecurityContext", userId);
                    }
                } catch (Exception e) {
                    logger.error("Failed to process JWT token: {}", e.getMessage());
                    SecurityContextHolder.clearContext();
                }
            } else {
                logger.debug("No valid token found in request/session/cookies");
                SecurityContextHolder.clearContext();
            }
        } catch (Exception e) {
            logger.error("Error in JwtAuthenticationFilter: {}", e.getMessage(), e);
            SecurityContextHolder.clearContext();
        }
        
        filterChain.doFilter(request, response);
    }
}
