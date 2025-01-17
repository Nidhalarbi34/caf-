package com.inn.cafe.JWT;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

 @Autowired
 CustomerUserDetailsService customerUserDetailsService;

 @Autowired
 JwtFilter jwtFilter;
 @Bean
 public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
  http.cors(corsCustomizer -> corsCustomizer.configurationSource(corsConfigurationSource()))
          .csrf(csrfCustomizer -> csrfCustomizer.disable()) // Disable CSRF protection for non-browser clients
          .authorizeHttpRequests(authz -> authz
                  .anyRequest().permitAll()) // Permit all requests without authentication
          .sessionManagement(session -> session
                  .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

  http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

  return http.build();
 }

 @Bean
 public CorsConfigurationSource corsConfigurationSource() {
  CorsConfiguration configuration = new CorsConfiguration();
  configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200", "https://yourdomain.com")); // Specify allowed origins here
  configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS")); // Specify allowed methods
  configuration.setAllowedHeaders(Collections.singletonList("*")); // Replace with specific headers if needed
  configuration.setAllowCredentials(true); // Allow credentials if necessary
  UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
  source.registerCorsConfiguration("/**", configuration);
  return source;
 }

 @Bean
 public AuthenticationManager authManager(HttpSecurity http) throws Exception {
  AuthenticationManagerBuilder authenticationManagerBuilder =
          http.getSharedObject(AuthenticationManagerBuilder.class);
  authenticationManagerBuilder.userDetailsService(customerUserDetailsService);
  return authenticationManagerBuilder.build();
 }

 @Bean
 public PasswordEncoder passwordEncoder() {
  return NoOpPasswordEncoder.getInstance();
 }
}
