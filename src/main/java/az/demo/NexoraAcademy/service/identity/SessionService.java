package az.demo.NexoraAcademy.service.identity;

import az.demo.NexoraAcademy.dto.identity.SessionRequest;
import az.demo.NexoraAcademy.dto.identity.SessionResponse;
import az.demo.NexoraAcademy.entity.enums.SessionType;
import az.demo.NexoraAcademy.entity.identity.Session;
import az.demo.NexoraAcademy.entity.identity.User;
import az.demo.NexoraAcademy.exception.ResourceNotFoundException;
import az.demo.NexoraAcademy.repository.identity.SessionRepository;
import az.demo.NexoraAcademy.repository.identity.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class SessionService {

    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<SessionResponse> findAll() {
        return sessionRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public SessionResponse findById(UUID id) {
        return toResponse(getOrThrow(id));
    }

    public SessionResponse create(SessionRequest request) {
        Session session = new Session();
        session.setUser(resolveUser(request.userId()));
        session.setType(request.type() != null ? request.type() : SessionType.SESSION);
        session.setTokenHash(request.tokenHash());
        session.setIpAddress(request.ipAddress());
        session.setUserAgent(request.userAgent());
        session.setExpiresAt(request.expiresAt());

        return toResponse(sessionRepository.saveAndFlush(session));
    }

    public SessionResponse update(UUID id, SessionRequest request) {
        Session session = getOrThrow(id);

        session.setUser(resolveUser(request.userId()));
        session.setType(request.type() != null ? request.type() : session.getType());
        session.setTokenHash(request.tokenHash());
        session.setIpAddress(request.ipAddress());
        session.setUserAgent(request.userAgent());
        session.setExpiresAt(request.expiresAt());

        return toResponse(sessionRepository.saveAndFlush(session));
    }

    public SessionResponse patch(UUID id, SessionRequest request) {
        Session session = getOrThrow(id);

        if (request.userId() != null) session.setUser(resolveUser(request.userId()));
        if (request.type() != null) session.setType(request.type());
        if (request.tokenHash() != null) session.setTokenHash(request.tokenHash());
        if (request.ipAddress() != null) session.setIpAddress(request.ipAddress());
        if (request.userAgent() != null) session.setUserAgent(request.userAgent());
        if (request.expiresAt() != null) session.setExpiresAt(request.expiresAt());

        return toResponse(sessionRepository.saveAndFlush(session));
    }

    /** Revokes a session (logout / token invalidation) without deleting the audit trail. */
    public SessionResponse revoke(UUID id) {
        Session session = getOrThrow(id);
        session.setRevokedAt(java.time.Instant.now());
        return toResponse(sessionRepository.saveAndFlush(session));
    }

    public void delete(UUID id) {
        if (!sessionRepository.existsById(id)) {
            throw ResourceNotFoundException.of("Session", id);
        }
        sessionRepository.deleteById(id);
    }

    private User resolveUser(UUID userId) {
        return userRepository.findById(userId).orElseThrow(() -> ResourceNotFoundException.of("User", userId));
    }

    private Session getOrThrow(UUID id) {
        return sessionRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Session", id));
    }

    private SessionResponse toResponse(Session session) {
        return new SessionResponse(
                session.getId(),
                session.getUser().getId(),
                session.getType(),
                session.getIpAddress(),
                session.getUserAgent(),
                session.getIssuedAt(),
                session.getExpiresAt(),
                session.getUsedAt(),
                session.getRevokedAt()
        );
    }
}
