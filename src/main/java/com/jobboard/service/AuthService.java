package com.jobboard.service;

import com.jobboard.entity.User.Role;
import com.jobboard.entity.User;
import com.jobboard.exceptions.EmailAlreadyExistsException;
import com.jobboard.exceptions.InvalidCredentialsException;
import com.jobboard.exceptions.UserNotFoundException;
import com.jobboard.repository.UserRepository;
import com.jobboard.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // ─────────────────────────────────────────
    //  REGISTER
    // ─────────────────────────────────────────
    public void register(String email, String password, Role role) {

        // 1. Guard: email must be unique
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email already in use: " + email);
        }

        // 2. Build the new user
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));   // BCrypt hash
        user.setRole(role);
        user.setProvider(User.Provider.LOCAL);

        // 3. Persist
        userRepository.save(user);

    }

    // ─────────────────────────────────────────
    //  LOGIN
    // ─────────────────────────────────────────
    public String login(String email, String password) {

        // 1. Look up the user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("No account found for: " + email));
        // 2. Verify the raw password against the stored BCrypt hash
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException("Incorrect password");
        }

        // 3. Issue a JWT and return it
        return jwtService.generateToken(user);
    }
}