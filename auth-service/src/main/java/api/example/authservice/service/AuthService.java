package api.example.authservice.service;

import api.example.authservice.dto.*;
import api.example.authservice.entity.User;
import api.example.authservice.repository.UserRepository;
import api.example.authservice.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private static final int OTP_EXPIRY_MINUTES = 10;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        // Prevent HR registration through normal flow
        if ("HR".equalsIgnoreCase(request.getRole())) {
            throw new RuntimeException("HR accounts cannot be created through registration");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.valueOf(request.getRole().toUpperCase()))
                .verified(false)
                .enabled(true)
                .accountNonLocked(true)
                .build();

        // Generate and send OTP
        String otp = generateOtp();
        user.setOtp(otp);
        user.setOtpGeneratedTime(LocalDateTime.now());

        userRepository.save(user);

        try {
            emailService.sendOtpEmail(user.getEmail(), otp);
        } catch (Exception e) {
            log.error("Failed to send OTP email: {}", e.getMessage());
            throw new RuntimeException("Failed to send verification email");
        }

        return AuthResponse.builder()
                .message("Registration successful. Please verify your email.")
                .email(user.getEmail())
                .role(user.getRole().name())
                .userId(user.getId())
                .verified(false)
                .build();
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for user: {}", request.getEmail());

        String email = request.getEmail().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!user.isVerified()) {
            throw new RuntimeException("Please verify your email before logging in");
        }

        if (!user.isEnabled()) {
            throw new RuntimeException("Account is disabled");
        }

        if (!user.isAccountNonLocked()) {
            throw new RuntimeException("Account is locked");
        }

        // Authenticate
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword())
        );

        String accessToken = jwtUtil.generateAccessToken(
                user.getEmail(), user.getRole().name(), user.getId()
        );

        String refreshToken = jwtUtil.generateRefreshToken(
                user.getEmail(), user.getRole().name(), user.getId()
        );

        log.info("Login successful for user: {}", email);

        return AuthResponse.builder()
                .message("Login successful")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .email(user.getEmail())
                .role(user.getRole().name())
                .userId(user.getId())
                .verified(true)
                .build();
    }

    @Transactional
    public AuthResponse verifyOtp(VerifyOtpRequest request) {
        String email = request.getEmail().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getOtp() == null) {
            throw new RuntimeException("No OTP found. Please request a new one.");
        }

        if (!user.getOtp().equals(request.getOtp())) {
            throw new RuntimeException("Invalid OTP");
        }

        if (user.getOtpGeneratedTime().plusMinutes(OTP_EXPIRY_MINUTES)
                .isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP has expired");
        }

        // Mark as verified
        user.setVerified(true);
        user.setOtp(null);
        user.setOtpGeneratedTime(null);
        userRepository.save(user);

        // Generate tokens
        String accessToken = jwtUtil.generateAccessToken(
                user.getEmail(), user.getRole().name(), user.getId()
        );

        String refreshToken = jwtUtil.generateRefreshToken(
                user.getEmail(), user.getRole().name(), user.getId()
        );

        return AuthResponse.builder()
                .message("Email verified successfully")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .email(user.getEmail())
                .role(user.getRole().name())
                .userId(user.getId())
                .verified(true)
                .build();
    }

    @Transactional
    public MessageResponse resendOtp(String email) {
        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isVerified()) {
            throw new RuntimeException("User is already verified");
        }

        String otp = generateOtp();
        user.setOtp(otp);
        user.setOtpGeneratedTime(LocalDateTime.now());
        userRepository.save(user);

        emailService.sendOtpEmail(user.getEmail(), otp);

        return new MessageResponse("OTP sent successfully");
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtUtil.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid or expired refresh token");
        }

        String email = jwtUtil.extractEmail(refreshToken);
        String role = jwtUtil.extractRole(refreshToken);
        Long userId = jwtUtil.extractUserId(refreshToken);

        String newAccessToken = jwtUtil.generateAccessToken(email, role, userId);
        String newRefreshToken = jwtUtil.generateRefreshToken(email, role, userId);

        return AuthResponse.builder()
                .message("Token refreshed successfully")
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .email(email)
                .role(role)
                .userId(userId)
                .verified(true)
                .build();
    }

    @Transactional
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        String email = request.getEmail().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String otp = generateOtp();
        user.setOtp(otp);
        user.setOtpGeneratedTime(LocalDateTime.now());
        userRepository.save(user);

        emailService.sendPasswordResetEmail(user.getEmail(), otp);

        return new MessageResponse("Password reset OTP sent to your email");
    }

    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        String email = request.getEmail().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getOtp() == null || !user.getOtp().equals(request.getOtp())) {
            throw new RuntimeException("Invalid OTP");
        }

        if (user.getOtpGeneratedTime().plusMinutes(OTP_EXPIRY_MINUTES)
                .isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP has expired");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setOtp(null);
        user.setOtpGeneratedTime(null);
        userRepository.save(user);

        return new MessageResponse("Password reset successfully");
    }

    public TokenValidationResponse validateToken(String token) {
        try {
            if (jwtUtil.validateToken(token)) {
                String email = jwtUtil.extractEmail(token);
                String role = jwtUtil.extractRole(token);
                Long userId = jwtUtil.extractUserId(token);

                return TokenValidationResponse.builder()
                        .valid(true)
                        .email(email)
                        .role(role)
                        .userId(userId)
                        .message("Token is valid")
                        .build();
            }
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
        }

        return TokenValidationResponse.builder()
                .valid(false)
                .message("Token is invalid or expired")
                .build();
    }

    private String generateOtp() {
        return String.format("%06d", new Random().nextInt(1000000));
    }
}