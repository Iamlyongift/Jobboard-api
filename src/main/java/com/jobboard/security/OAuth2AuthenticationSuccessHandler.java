package com.jobboard.security;

import com.jobboard.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        User user = oAuth2User.getUser();

        // If role is still null → profile is incomplete → redirect them
        if (user.getRole() == null || !user.isProfileComplete()) {
            response.sendRedirect("/complete-profile?email=" + user.getEmail());
            return;
        }

        // Role exists → generate JWT and return it
        String token = jwtService.generateToken(user);

        // Option A: redirect with token as query param (for SPAs)
        response.sendRedirect("/oauth2/success?token=" + token);

    }
}