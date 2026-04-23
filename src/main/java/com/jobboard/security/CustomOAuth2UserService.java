package com.jobboard.security;

import com.jobboard.entity.User.Provider;
import com.jobboard.entity.User;
import com.jobboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {

        // 1. Let Spring fetch the user info from Google/GitHub
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 2. Which provider is this? ("google" or "github")
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // 3. Pull raw attributes from the OAuth2 response
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email      = extractEmail(registrationId, attributes);
        String name       = extractName(registrationId, attributes);
        String providerId = extractProviderId(registrationId, attributes);
        Provider provider = registrationId.equalsIgnoreCase("google")
                ? Provider.GOOGLE : Provider.GITHUB;

        // 4. Find or create the user in our DB
        User user = userRepository.findByEmail(email)
                .map(existing -> updateExistingUser(existing, provider, providerId))
                .orElseGet(() -> createNewUser(email, name, provider, providerId));

        // 5. Wrap our User entity inside an OAuth2User Spring can work with
        return new CustomOAuth2User(oAuth2User, user);
    }

    // ── existing user: just refresh their provider info ──────────────────────
    private User updateExistingUser(User user, Provider provider, String providerId) {
        user.setProvider(provider);
        user.setProviderId(providerId);
        return userRepository.save(user);
    }

    // ── brand-new user: no role yet → needs profile completion ───────────────
    private User createNewUser(String email, String name,
                               Provider provider, String providerId) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(null);          // OAuth users have no local password
        user.setRole(null);              // role is UNKNOWN until they tell us
        user.setProvider(provider);
        user.setProviderId(providerId);
        user.setProfileComplete(false);  // flag → triggers redirect
        return userRepository.save(user);
    }

    // ── attribute extraction helpers ─────────────────────────────────────────
    private String extractEmail(String registrationId, Map<String, Object> attrs) {
        return (String) attrs.get("email");
    }

    private String extractName(String registrationId, Map<String, Object> attrs) {
        if (registrationId.equalsIgnoreCase("github")) {
            return (String) attrs.getOrDefault("login", "");
        }
        return (String) attrs.getOrDefault("name", "");
    }

    private String extractProviderId(String registrationId, Map<String, Object> attrs) {
        Object id = attrs.get("sub");              // Google uses "sub"
        if (id == null) id = attrs.get("id");      // GitHub uses "id"
        return String.valueOf(id);
    }
}