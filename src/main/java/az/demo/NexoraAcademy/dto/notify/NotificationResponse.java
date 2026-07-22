package az.demo.NexoraAcademy.dto.notify;

import az.demo.NexoraAcademy.entity.enums.NotificationChannel;
import az.demo.NexoraAcademy.entity.enums.NotificationStatus;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        UUID userId,
        String type,
        NotificationChannel channel,
        Map<String, Object> payload,
        NotificationStatus status,
        Instant sentAt,
        Instant readAt,
        Instant createdAt
) {
}
