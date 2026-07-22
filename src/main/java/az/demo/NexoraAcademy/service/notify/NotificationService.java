package az.demo.NexoraAcademy.service.notify;

import az.demo.NexoraAcademy.dto.notify.NotificationRequest;
import az.demo.NexoraAcademy.dto.notify.NotificationResponse;
import az.demo.NexoraAcademy.entity.enums.NotificationStatus;
import az.demo.NexoraAcademy.entity.identity.User;
import az.demo.NexoraAcademy.entity.notify.Notification;
import az.demo.NexoraAcademy.exception.InvalidStateException;
import az.demo.NexoraAcademy.exception.ResourceNotFoundException;
import az.demo.NexoraAcademy.repository.identity.UserRepository;
import az.demo.NexoraAcademy.repository.notify.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<NotificationResponse> findAll() {
        return notificationRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public NotificationResponse findById(UUID id) {
        return toResponse(getOrThrow(id));
    }

    public NotificationResponse create(NotificationRequest request) {
        Notification notification = new Notification();
        notification.setUser(resolveUser(request.userId()));
        notification.setType(request.type());
        notification.setChannel(request.channel());
        notification.setPayload(request.payload() != null ? request.payload() : new HashMap<>());
        notification.setStatus(NotificationStatus.QUEUED);

        return toResponse(notificationRepository.saveAndFlush(notification));
    }

    public NotificationResponse update(UUID id, NotificationRequest request) {
        Notification notification = getOrThrow(id);

        notification.setUser(resolveUser(request.userId()));
        notification.setType(request.type());
        notification.setChannel(request.channel());
        notification.setPayload(request.payload() != null ? request.payload() : notification.getPayload());

        return toResponse(notificationRepository.saveAndFlush(notification));
    }

    public NotificationResponse patch(UUID id, NotificationRequest request) {
        Notification notification = getOrThrow(id);

        if (request.userId() != null) notification.setUser(resolveUser(request.userId()));
        if (request.type() != null) notification.setType(request.type());
        if (request.channel() != null) notification.setChannel(request.channel());
        if (request.payload() != null) notification.setPayload(request.payload());

        return toResponse(notificationRepository.saveAndFlush(notification));
    }

    /** Marks the notification as sent. */
    public NotificationResponse markSent(UUID id) {
        Notification notification = getOrThrow(id);
        if (notification.getStatus() != NotificationStatus.QUEUED) {
            throw new InvalidStateException("Notification " + id + " cannot be marked sent from status " + notification.getStatus());
        }
        notification.setStatus(NotificationStatus.SENT);
        notification.setSentAt(java.time.Instant.now());
        return toResponse(notificationRepository.saveAndFlush(notification));
    }

    /** Marks the notification as read by the recipient. */
    public NotificationResponse markRead(UUID id) {
        Notification notification = getOrThrow(id);
        notification.setStatus(NotificationStatus.READ);
        notification.setReadAt(java.time.Instant.now());
        return toResponse(notificationRepository.saveAndFlush(notification));
    }

    public void delete(UUID id) {
        if (!notificationRepository.existsById(id)) {
            throw ResourceNotFoundException.of("Notification", id);
        }
        notificationRepository.deleteById(id);
    }

    private User resolveUser(UUID userId) {
        return userRepository.findById(userId).orElseThrow(() -> ResourceNotFoundException.of("User", userId));
    }

    private Notification getOrThrow(UUID id) {
        return notificationRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Notification", id));
    }

    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getUser().getId(),
                notification.getType(),
                notification.getChannel(),
                notification.getPayload(),
                notification.getStatus(),
                notification.getSentAt(),
                notification.getReadAt(),
                notification.getCreatedAt()
        );
    }
}
