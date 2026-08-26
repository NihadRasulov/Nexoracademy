package az.demo.NexoraAcademy.service;

import az.demo.NexoraAcademy.dto.auth.LoginRequest;
import az.demo.NexoraAcademy.dto.auth.RefreshTokenRequest;
import az.demo.NexoraAcademy.dto.auth.TokenResponse;
import az.demo.NexoraAcademy.entity.enums.AccountStatus;
import az.demo.NexoraAcademy.entity.enums.SessionType;
import az.demo.NexoraAcademy.entity.enums.UserRole;
import az.demo.NexoraAcademy.entity.identity.Session;
import az.demo.NexoraAcademy.entity.identity.User;
import az.demo.NexoraAcademy.exception.InvalidCredentialsException;
import az.demo.NexoraAcademy.exception.InvalidTokenException;
import az.demo.NexoraAcademy.repository.identity.SessionRepository;
import az.demo.NexoraAcademy.repository.identity.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;

/** Authentication for the private CMS admin only. Public account registration does not exist. */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final JwtService jwtService;

    @Transactional
    public TokenResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (user.getRole() != UserRole.ADMIN || user.getStatus() != AccountStatus.ACTIVE) {
            throw new InvalidCredentialsException("This account cannot access the admin panel");
        }

        user.setLastLoginAt(Instant.now());
        userRepository.save(user);
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
        sessionRepository.save(session);

        User user = session.getUser();
        if (user.getRole() != UserRole.ADMIN || user.getStatus() != AccountStatus.ACTIVE) {
            throw new InvalidTokenException("Admin access is no longer active");
        }
        return issueTokens(user);
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {
        sessionRepository.findByTokenHash(hash(request.refreshToken())).ifPresent(session -> {
            if (session.getRevokedAt() == null) {
                session.setRevokedAt(Instant.now());
                sessionRepository.save(session);
            }
        });
    }

    private TokenResponse issueTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        Session session = new Session();
        session.setUser(user);
        session.setType(SessionType.SESSION);
        session.setTokenHash(hash(refreshToken));
        session.setExpiresAt(Instant.now().plusMillis(jwtService.getRefreshTokenExpirationMs()));
        sessionRepository.save(session);

        return TokenResponse.bearer(accessToken, refreshToken, jwtService.getAccessTokenExpirationMs());
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return Base64.getEncoder().encodeToString(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
