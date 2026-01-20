package com.ipintelligence.service;

import org.springframework.transaction.annotation.Transactional;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import com.ipintelligence.model.PasswordResetToken;
import com.ipintelligence.repo.PasswordResetTokenRepository;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtTokenProvider jwt;
    private final PasswordResetTokenRepository tokenRepo;
    private final JavaMailSender mailSender;

    @Autowired
    public AuthServiceImpl(UserRepository users,
                           PasswordEncoder encoder,
                           AuthenticationManager authManager,
                           JwtTokenProvider jwt,
                           PasswordResetTokenRepository tokenRepo,
                           JavaMailSender mailSender) {
        this.users = users;
        this.encoder = encoder;
        this.authManager = authManager;
        this.jwt = jwt;
        this.tokenRepo = tokenRepo;
        this.mailSender = mailSender;
    }

    @Override
    @Transactional
    public void forgotPassword(String email) {
        if (email != null) email = email.trim().toLowerCase();
        User user = users.findByEmail(email).orElse(null);
        if (user == null) {
            // For security, do not reveal if user exists
            return;
        }
        // Remove any previous tokens for this user
        tokenRepo.deleteByUser(user);
        // Generate token
        String token = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusHours(1);
        PasswordResetToken resetToken = new PasswordResetToken(token, user, expiry);
        tokenRepo.save(resetToken);
        // Build reset link
        String resetLink = "http://localhost:3000/reset-password?token=" + token;
        // Send email
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(user.getEmail());
            helper.setSubject("Password Reset Request");
            helper.setText("<p>To reset your password, click the link below:</p>"
                + "<p><a href='" + resetLink + "'>Reset Password</a></p>"
                + "<p>This link will expire in 1 hour.</p>", true);
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send reset email");
        }
    }

        @Override
        @Transactional
        public boolean resetPassword(String token, String newPassword) {
            PasswordResetToken resetToken = tokenRepo.findByToken(token).orElse(null);
            if (resetToken == null || resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
                System.out.println("[RESET-PASSWORD-DEBUG] Invalid or expired token: " + token);
                return false;
            }
            User user = resetToken.getUser();
            // Defensive: ensure email is normalized
            if (user.getEmail() != null) user.setEmail(user.getEmail().trim().toLowerCase());
            System.out.println("[RESET-PASSWORD-DEBUG] Before reset: email=" + user.getEmail() + ", hash=" + user.getPassword());
            String newHash = encoder.encode(newPassword);
            user.setPassword(newHash);
            users.save(user);
            System.out.println("[RESET-PASSWORD-DEBUG] After reset: email=" + user.getEmail() + ", new hash=" + user.getPassword());
            tokenRepo.delete(resetToken);
            return true;
        }

    @Override
    public AuthResponse register(RegisterRequest r) {
        if (users.existsByEmail(r.getEmail())) {
            throw new RuntimeException("User with this email already exists");
        }

        User u = new User();
        u.setUsername(r.getUsername());
        u.setEmail(r.getEmail() != null ? r.getEmail().trim().toLowerCase() : null);
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
        String email = r.getEmail();
        if (email != null) email = email.trim().toLowerCase();
        if (email == null || !email.contains("@")) {
            throw new RuntimeException("Please login using your email address.");
        }
        User u = users.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        System.out.println("[LOGIN-DEBUG] Attempt login: email=" + u.getEmail() + ", hash=" + u.getPassword());
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
