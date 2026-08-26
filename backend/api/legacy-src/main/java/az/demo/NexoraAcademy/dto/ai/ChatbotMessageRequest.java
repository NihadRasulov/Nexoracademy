package az.demo.NexoraAcademy.dto.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Frontend-in bizim backend-ə göndərdiyi sorğu (POST /api/v1/chatbot/message).
 *
 * @param message        istifadəçinin yazdığı mesaj
 * @param conversationId söhbəti davam etdirmək üçün əvvəlki cavabda qayıdan id
 *                       (ilk mesajda boş ola bilər)
 */
public record ChatbotMessageRequest(
        @NotBlank @Size(max = 4000) String message,

        @Size(max = 100) String conversationId
) {
}
