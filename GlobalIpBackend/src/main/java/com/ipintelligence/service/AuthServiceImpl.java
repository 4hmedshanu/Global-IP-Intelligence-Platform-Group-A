package com.ipintelligence.service;

import com.ipintelligence.dto.AuthRequest;
import com.ipintelligence.dto.AuthResponse;
import com.ipintelligence.dto.ProfileRequest;
import com.ipintelligence.dto.ProfileResponse;
import com.ipintelligence.dto.RegisterRequest;
import com.ipintelligence.model.User;
import com.ipintelligence.repo.UserRepository;
import com.ipintelligence.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtTokenProvider jwt;

    public AuthServiceImpl(UserRepository users,
                           PasswordEncoder encoder,
                           AuthenticationManager authManager,
                           JwtTokenProvider jwt) {
        this.users = users;
        this.encoder = encoder;
        this.authManager = authManager;
        this.jwt = jwt;
    }

    @Override
    public AuthResponse register(RegisterRequest r) {
        if (users.existsByEmail(r.getEmail())) {
            throw new RuntimeException("User with this email already exists");
        }

        User u = new User();
        u.setUsername(r.getUsername());
        u.setEmail(r.getEmail());
        u.setPassword(encoder.encode(r.getPassword()));

        String role = r.getRole();
        if (role == null || role.isBlank()) {
            role = "USER";
        } else {
            role = role.toUpperCase();
            if (!role.equals("ADMIN") && !role.equals("ANALYST") && !role.equals("USER")) {
                throw new RuntimeException("Invalid role: " + role);
            }
        }
        u.setRole(role);

        users.save(u);

        String token = jwt.generateToken(u.getEmail(), u.getRole());

        return new AuthResponse(token, u.getId(), u.getUsername(), u.getEmail(), u.getRole());
    }

    @Override
    public AuthResponse login(AuthRequest r) {
        String identifier = r.getEmail();
        User u;

        if (identifier.contains("@")) {
            u = users.findByEmail(identifier)
                    .orElseThrow(() -> new RuntimeException("Invalid credentials"));
        } else {
            u = users.findByUsername(identifier)
                    .orElseThrow(() -> new RuntimeException("Invalid credentials"));
        }

        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(u.getEmail(), r.getPassword())
        );

        String token = jwt.generateToken(u.getEmail(), u.getRole());

        return new AuthResponse(token, u.getId(), u.getUsername(), u.getEmail(), u.getRole());
    }

    @Override
    public ProfileResponse getProfile(String email) {
        User user = users.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return new ProfileResponse(user.getId(), user.getUsername(), user.getEmail(), 
                user.getFirstName(), user.getLastName(), user.getRole());
    }

    @Override
    public ProfileResponse updateProfile(String email, ProfileRequest request) {
        User user = users.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            user.setUsername(request.getUsername());
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            if (users.existsByEmail(request.getEmail()) && !request.getEmail().equals(user.getEmail())) {
                throw new RuntimeException("Email already exists");
            }
            user.setEmail(request.getEmail());
        }
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }

        users.save(user);
        return getProfile(user.getEmail());
    }

    @Override
    public void changePassword(String email, String newPassword) {
        User user = users.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setPassword(encoder.encode(newPassword));
        users.save(user);
    }
}
