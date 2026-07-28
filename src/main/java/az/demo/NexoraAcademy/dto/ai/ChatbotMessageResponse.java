package az.demo.NexoraAcademy.dto.ai;

/**
 * Bizim backend-in frontend-ə qaytardığı cavab.
 *
 * @param reply          botun mətn cavabı
 * @param conversationId növbəti mesajda geri göndəriləcək söhbət id-si
 */
public record ChatbotMessageResponse(
        String reply,
        String conversationId
) {
}
