package az.demo.NexoraAcademy.service;

import az.demo.NexoraAcademy.config.AuthProperties;
import az.demo.NexoraAcademy.config.MailProperties;
import az.demo.NexoraAcademy.dto.auth.ForgotPasswordRequest;
import az.demo.NexoraAcademy.dto.auth.LoginRequest;
import az.demo.NexoraAcademy.dto.auth.RefreshTokenRequest;
import az.demo.NexoraAcademy.dto.auth.RegisterRequest;
import az.demo.NexoraAcademy.dto.auth.RegisterResponse;
import az.demo.NexoraAcademy.dto.auth.ResendVerificationRequest;
import az.demo.NexoraAcademy.dto.auth.ResetPasswordRequest;
import az.demo.NexoraAcademy.dto.auth.TokenResponse;
import az.demo.NexoraAcademy.dto.auth.VerifyEmailRequest;
import az.demo.NexoraAcademy.entity.enums.AccountStatus;
import az.demo.NexoraAcademy.entity.enums.SessionType;
import az.demo.NexoraAcademy.entity.enums.UserRole;
import az.demo.NexoraAcademy.entity.identity.Session;
import az.demo.NexoraAcademy.entity.identity.User;
import az.demo.NexoraAcademy.event.UserLoggedInEvent;
import az.demo.NexoraAcademy.event.UserRegisteredEvent;
import az.demo.NexoraAcademy.exception.DuplicateResourceException;
import az.demo.NexoraAcademy.exception.InvalidCredentialsException;
import az.demo.NexoraAcademy.exception.InvalidTokenException;
import az.demo.NexoraAcademy.repository.identity.SessionRepository;
import az.demo.NexoraAcademy.repository.identity.UserRepository;
import az.demo.NexoraAcademy.service.notify.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final MailProperties mailProperties;
    private final AuthProperties authProperties;
    private final ApplicationEventPublisher eventPublisher;

    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw DuplicateResourceException.of("User", "email", request.email());
        }

        User user = new User();
        user.setEmail(request.email());
        user.setFullName(request.fullName());
        user.setPhone(request.phone());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(UserRole.STUDENT);
        user.setStatus(AccountStatus.PENDING_VERIFICATION);
        user.setProfile(new HashMap<>());
        user = userRepository.saveAndFlush(user);

        eventPublisher.publishEvent(new UserRegisteredEvent(user.getId(), user.getEmail()));
        sendVerificationEmail(user);

        return new RegisterResponse(user.getId(), user.getEmail(),
                "Registration successful. Please check your email to verify your account.");
    }

    @Transactional
    public TokenResponse login(LoginRequest request, String ipAddress) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        user.setLastLoginAt(Instant.now());
        userRepository.saveAndFlush(user);

        eventPublisher.publishEvent(new UserLoggedInEvent(user.getId(), user.getEmail(), ipAddress));

        return issueTokens(user);
    }

    @Transactional
    public TokenResponse refresh(RefreshTokenRequest request) {
        String refreshToken = request.refreshToken();

        if (!jwtService.isTokenValid(refreshToken) || !jwtService.isRefreshToken(refreshToken)) {
            throw new InvalidTokenException("Invalid refresh token");
        }

        Session session = sessionRepository.findByTokenHash(hash(refreshToken))
                .orElseThrow(() -> new InvalidTokenException("Refresh token not recognized"));

        if (session.getRevokedAt() != null || session.getUsedAt() != null) {
            throw new InvalidTokenException("Refresh token has already been used or revoked");
        }
        if (session.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidTokenException("Refresh token has expired");
        }

        session.setUsedAt(Instant.now());
        session.setRevokedAt(Instant.now());
        sessionRepository.saveAndFlush(session);

        User user = session.getUser();
        return issueTokens(user);
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {
        sessionRepository.findByTokenHash(hash(request.refreshToken()))
                .ifPresent(session -> {
                    if (session.getRevokedAt() == null) {
                        session.setRevokedAt(Instant.now());
                        sessionRepository.saveAndFlush(session);
                    }
                });
    }

    /** Always succeeds from the caller's point of view — never reveals whether the email exists. */
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.email()).ifPresent(user -> {
            String rawToken = generateRawToken();

            Session session = new Session();
            session.setUser(user);
            session.setType(SessionType.PASSWORD_RESET);
            session.setTokenHash(hash(rawToken));
            session.setExpiresAt(Instant.now().plusMillis(authProperties.getPasswordResetExpirationMs()));
            sessionRepository.saveAndFlush(session);

            String link = mailProperties.getFrontendBaseUrl() + "/reset-password?token=" + rawToken;
            emailService.send(user.getEmail(), "Reset your NexoraAcademy password",
                    "We received a request to reset your password. Use the link below (valid for "
                            + (authProperties.getPasswordResetExpirationMs() / 60000) + " minutes):\n\n" + link
                            + "\n\nIf you did not request this, you can safely ignore this email.");
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        Session session = sessionRepository.findByTokenHash(hash(request.token()))
                .filter(s -> s.getType() == SessionType.PASSWORD_RESET)
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired password reset token"));

        assertUsable(session);

        User user = session.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.saveAndFlush(user);

        session.setUsedAt(Instant.now());
        session.setRevokedAt(Instant.now());
        sessionRepository.saveAndFlush(session);
    }

    @Transactional
    public void verifyEmail(VerifyEmailRequest request) {
        Session session = sessionRepository.findByTokenHash(hash(request.token()))
                .filter(s -> s.getType() == SessionType.EMAIL_VERIFY)
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired email verification token"));

        assertUsable(session);

        User user = session.getUser();
        user.setEmailVerifiedAt(Instant.now());
        if (user.getStatus() == AccountStatus.PENDING_VERIFICATION) {
            user.setStatus(AccountStatus.ACTIVE);
        }
        userRepository.saveAndFlush(user);

        session.setUsedAt(Instant.now());
        session.setRevokedAt(Instant.now());
        sessionRepository.saveAndFlush(session);
    }

    /** Always succeeds from the caller's point of view — never reveals whether the email exists. */
    @Transactional
    public void resendVerification(ResendVerificationRequest request) {
        userRepository.findByEmail(request.email())
                .filter(user -> user.getEmailVerifiedAt() == null)
                .ifPresent(this::sendVerificationEmail);
    }

    private void sendVerificationEmail(User user) {
        String rawToken = generateRawToken();

        Session session = new Session();
        session.setUser(user);
        session.setType(SessionType.EMAIL_VERIFY);
        session.setTokenHash(hash(rawToken));
        session.setExpiresAt(Instant.now().plusMillis(authProperties.getEmailVerifyExpirationMs()));
        sessionRepository.saveAndFlush(session);

        String link = mailProperties.getFrontendBaseUrl() + "/verify-email?token=" + rawToken;
        emailService.send(user.getEmail(), "Verify your NexoraAcademy email",
                "Welcome to NexoraAcademy! Please verify your email using the link below (valid for "
                        + (authProperties.getEmailVerifyExpirationMs() / 3600000) + " hours):\n\n" + link);
    }

    private void assertUsable(Session session) {
        if (session.getRevokedAt() != null || session.getUsedAt() != null) {
            throw new InvalidTokenException("Token has already been used or revoked");
        }
        if (session.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidTokenException("Token has expired");
        }
    }

    private TokenResponse issueTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        Session session = new Session();
        session.setUser(user);
        session.setType(SessionType.SESSION);
        session.setTokenHash(hash(refreshToken));
        session.setExpiresAt(Instant.now().plusMillis(jwtService.getRefreshTokenExpirationMs()));
        sessionRepository.saveAndFlush(session);

        return TokenResponse.bearer(accessToken, refreshToken, jwtService.getAccessTokenExpirationMs());
    }

    private String generateRawToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
