package com.jobboard.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2SuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. Disable CSRF — we use JWT (stateless), not session cookies
                .csrf(csrf -> csrf.disable())

                // 2. Stateless session — Spring must NOT create or use HttpSession
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 3. Route-level authorization rules
                .authorizeHttpRequests(auth -> auth

                        // ── Public endpoints ──────────────────────────────────────
                        .requestMatchers(HttpMethod.POST, "/auth/register", "/auth/login")
                        .permitAll()
                        .requestMatchers("/oauth2/**", "/login/oauth2/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/jobs")
                        .permitAll()
                        .requestMatchers("/complete-profile").permitAll()
                        .requestMatchers("/oauth2/success").permitAll()

                        // ── Employer-only endpoints ───────────────────────────────
                        .requestMatchers(HttpMethod.POST,   "/api/jobs")
                        .hasRole("EMPLOYER")
                        .requestMatchers(HttpMethod.PUT,    "/api/jobs/{id}")
                        .hasRole("EMPLOYER")
                        .requestMatchers(HttpMethod.DELETE, "/api/jobs/{id}")
                        .hasRole("EMPLOYER")
                        .requestMatchers(HttpMethod.GET,    "/api/jobs/{id}/applications")
                        .hasRole("EMPLOYER")


                        // ── Candidate-only endpoints ──────────────────────────────
                        .requestMatchers(HttpMethod.POST,   "/api/jobs/{id}/apply")
                        .hasRole("CANDIDATE")
                        .requestMatchers(HttpMethod.GET,    "/api/applications/mine")
                        .hasRole("CANDIDATE")
                        .requestMatchers(HttpMethod.DELETE, "/api/applications/{id}")
                        .hasRole("CANDIDATE")

                        // ── Any authenticated user ────────────────────────────────
                        .requestMatchers(HttpMethod.GET, "/profile")
                        .authenticated()

                        // ── Catch-all: everything else requires authentication ─────
                        .anyRequest().authenticated()
                )

                // 4. OAuth2 login configuration
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(info -> info
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2SuccessHandler)
                )

                // 5. Plug in our JWT filter BEFORE Spring's default login filter
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Needed by AuthService to authenticate during login
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

}