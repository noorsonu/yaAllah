package com.main.config;

import org.springframework.context.annotation.*;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.main.security.JwtAuthenticationEntryPoint;
import com.main.security.JwtFilter;
import com.main.security.RestAccessDeniedHandler;
import com.main.services.UserService;

import java.util.*;
import java.util.stream.Collectors;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

        @Autowired
        private JwtAuthenticationEntryPoint entryPoint;

        @Autowired
        private RestAccessDeniedHandler accessDeniedHandler;

        @Lazy
        @Autowired
        private JwtFilter jwtFilter;

        @Autowired
        private UserService userService;

        @Autowired
        private PasswordEncoder passwordEncoder;

        // Comma-separated list of allowed origins. Configurable via env
        // (CORS_ALLOWED_ORIGINS) or properties.
        @Value("${cors.allowed-origins:*}")
        private String corsAllowedOrigins;

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
                return config.getAuthenticationManager();
        }

        @Bean
        public DaoAuthenticationProvider authProvider() {
                DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
                provider.setUserDetailsService(email -> userService.loadUserByEmail(email));
                provider.setPasswordEncoder(passwordEncoder);
                return provider;
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(csrf -> csrf.disable())
                                .cors(Customizer.withDefaults())
                                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .exceptionHandling(
                                                ex -> ex.authenticationEntryPoint(entryPoint)
                                                                .accessDeniedHandler(accessDeniedHandler))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(
                                                                "/favicon.ico",
                                                                "/",
                                                                "/error",
                                                                "/public/**",
                                                                "/uploads/**",
                                                                "/v3/api-docs/**",
                                                                "/swagger-ui.html",
                                                                "/swagger-ui/**",
                                                                "/swagger-resources/**",
                                                                "/webjars/**",
                                                                "/api/auth/**",
                                                                "/api/account/**")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                                .anyRequest().authenticated())
                                .authenticationProvider(authProvider())
                                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();

                // Support wildcard and multiple origins via patterns, works with
                // allowCredentials(true)
                List<String> origins = Arrays.stream(corsAllowedOrigins.split(","))
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .collect(Collectors.toList());
                configuration.setAllowedOriginPatterns(origins);

                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(
                                Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin"));
                configuration.setExposedHeaders(Arrays.asList("Authorization"));
                configuration.setAllowCredentials(true);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }
}
