package az.demo.NexoraAcademy.service.platform;

import az.demo.NexoraAcademy.dto.platform.AuditLogRequest;
import az.demo.NexoraAcademy.dto.platform.AuditLogResponse;
import az.demo.NexoraAcademy.entity.identity.User;
import az.demo.NexoraAcademy.entity.platform.AuditLog;
import az.demo.NexoraAcademy.exception.ResourceNotFoundException;
import az.demo.NexoraAcademy.repository.identity.UserRepository;
import az.demo.NexoraAcademy.repository.platform.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Audit logs are conventionally append-only. update/patch/delete are still
 * exposed here for API symmetry with the other entities, but a controller
 * wiring this up should gate them behind an admin-only role — routine
 * traffic should only ever call create()/findAll()/findById().
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<AuditLogResponse> findAll() {
        return auditLogRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public AuditLogResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    public AuditLogResponse create(AuditLogRequest request) {
        AuditLog log = new AuditLog();
        log.setActor(resolveUser(request.actorId()));
        log.setAction(request.action());
        log.setEntityType(request.entityType());
        log.setEntityId(request.entityId());
        log.setBeforeState(request.beforeState());
        log.setAfterState(request.afterState());
        log.setTraceId(UUID.randomUUID());
        log.setIpAddress(request.ipAddress());

        return toResponse(auditLogRepository.saveAndFlush(log));
    }

    public AuditLogResponse update(Long id, AuditLogRequest request) {
        AuditLog log = getOrThrow(id);

        log.setActor(resolveUser(request.actorId()));
        log.setAction(request.action());
        log.setEntityType(request.entityType());
        log.setEntityId(request.entityId());
        log.setBeforeState(request.beforeState());
        log.setAfterState(request.afterState());
        log.setIpAddress(request.ipAddress());

        return toResponse(auditLogRepository.saveAndFlush(log));
    }

    public AuditLogResponse patch(Long id, AuditLogRequest request) {
        AuditLog log = getOrThrow(id);

        if (request.actorId() != null) log.setActor(resolveUser(request.actorId()));
        if (request.action() != null) log.setAction(request.action());
        if (request.entityType() != null) log.setEntityType(request.entityType());
        if (request.entityId() != null) log.setEntityId(request.entityId());
        if (request.beforeState() != null) log.setBeforeState(request.beforeState());
        if (request.afterState() != null) log.setAfterState(request.afterState());
        if (request.ipAddress() != null) log.setIpAddress(request.ipAddress());

        return toResponse(auditLogRepository.saveAndFlush(log));
    }

    public void delete(Long id) {
        if (!auditLogRepository.existsById(id)) {
            throw ResourceNotFoundException.of("AuditLog", id);
        }
        auditLogRepository.deleteById(id);
    }

    private User resolveUser(UUID userId) {
        if (userId == null) {
            return null;
        }
        return userRepository.findById(userId).orElseThrow(() -> ResourceNotFoundException.of("User", userId));
    }

    private AuditLog getOrThrow(Long id) {
        return auditLogRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("AuditLog", id));
    }

    private AuditLogResponse toResponse(AuditLog log) {
        return new AuditLogResponse(
                log.getId(),
                log.getActor() != null ? log.getActor().getId() : null,
                log.getAction(),
                log.getEntityType(),
                log.getEntityId(),
                log.getBeforeState(),
                log.getAfterState(),
                log.getTraceId(),
                log.getIpAddress(),
                log.getCreatedAt()
        );
    }
}
