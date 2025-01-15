package com.inn.cafe.JWT;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import jakarta.servlet.FilterChain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomerUserDetailsService service;

    private Claims claims = null;
    private String username = null;
    private String email = null;

    private static final String ROLE_ADMIN = "admin";
    private static final String ROLE_USER = "user";

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain)
            throws ServletException, IOException {

        // Bypass filtering for login, signup, and forgot password requests
        if (httpServletRequest.getServletPath().matches("/user/login|/user/forgetPassword|/user/signup")) {
            filterChain.doFilter(httpServletRequest, httpServletResponse);
            return;
        }

        // Extract Authorization header
        String authorizationHeader = httpServletRequest.getHeader("Authorization");
        String token = null;
       // String username = null;

        // Check if the Authorization header contains a Bearer token
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);  // Extract the token (skip "Bearer ")

            // Extract username and claims from the token
            this.username = jwtUtil.extractUsername(token);
            this.claims = jwtUtil.extractAllClaims(token);  // Store claims for later use
            this.email = jwtUtil.extractEmail(token);
        }

        // Proceed with authentication if the username is extracted and the security context is not yet authenticated
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Load user details from the service
            UserDetails userDetails = service.loadUserByUsername(email);//byemail

            // Validate the token
            if (jwtUtil.validatetoken(token, userDetails)) {
                // Set authentication in security context
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpServletRequest));

                // Set the security context's authentication
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }

        // Continue with the filter chain
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }

    // Generic role checking method
    public boolean hasRole(String role) {
        return role.equalsIgnoreCase((String) claims.get("role"));
    }

    public boolean isAdmin() {
        return hasRole(ROLE_ADMIN);
    }

    public boolean isUser() {
        return hasRole(ROLE_USER);
    }

    public String getCurrentUsername() {
        return username;
    }

    public String getCurrentEmail() {
        return email;
    }
}
