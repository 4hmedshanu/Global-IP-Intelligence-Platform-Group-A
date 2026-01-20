package com.ipintelligence.service;

import com.ipintelligence.dto.*;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(AuthRequest request);
    ProfileResponse getProfile(String email);
    ProfileResponse updateProfile(String email, ProfileRequest request);
    void changePassword(String email, String newPassword);

    void forgotPassword(String email);
    boolean resetPassword(String token, String newPassword);
}
