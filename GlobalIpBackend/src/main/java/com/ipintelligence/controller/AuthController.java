
package com.ipintelligence.controller;

import com.ipintelligence.dto.*;
import com.ipintelligence.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ipintelligence.dto.*;
import com.ipintelligence.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
@CrossOrigin(origins = {"http://localhost:3000"})
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        auth.forgotPassword(request.getEmail());
        return ResponseEntity.status(HttpStatus.OK).body("Password reset instructions sent if email exists.");
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(auth.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(auth.login(request));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        boolean success = false;
        String errorDetail = null;
        try {
            success = auth.resetPassword(request.getToken(), request.getNewPassword());
        } catch (Exception e) {
            errorDetail = e.getMessage();
        }
        if (success) {
            return ResponseEntity.ok("Password reset successful");
        } else {
            String msg = "Invalid or expired token";
            if (errorDetail != null) {
                msg += ": " + errorDetail;
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(msg);
        }
    }
}
