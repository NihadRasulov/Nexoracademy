package az.demo.NexoraAcademy.dto.outcomes;

import jakarta.validation.constraints.NotNull;

public record ReviewPublicationRequest(@NotNull Boolean published) {
}
