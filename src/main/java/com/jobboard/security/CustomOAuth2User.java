package com.jobboard.security;

import com.jobboard.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;
@Getter
// Wraps Spring's OAuth2User AND our own User entity together
public class CustomOAuth2User implements OAuth2User {

    private final OAuth2User oAuth2User;
    private final User user;

    public CustomOAuth2User(OAuth2User oAuth2User, User user) {
        this.oAuth2User = oAuth2User;
        this.user = user;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oAuth2User.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (user.getRole() == null) {
            return List.of(new SimpleGrantedAuthority("ROLE_INCOMPLETE"));
        }
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getName() {
        return user.getEmail();
    }

    public String getUsername() {
        return user.getEmail();
    }
}