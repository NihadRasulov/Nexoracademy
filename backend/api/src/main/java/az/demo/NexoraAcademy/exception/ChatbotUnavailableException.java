package az.demo.NexoraAcademy.exception;

import org.springframework.http.HttpStatus;

/**
 * Kənar chat-bot xidmətinə çatmaq mümkün olmayanda atılır: bot söndürülüb
 * (app.chatbot.enabled=false), URL yanlışdır, timeout olub və ya bot 5xx
 * qaytarıb. GlobalExceptionHandler bunu 503 SERVICE_UNAVAILABLE-ə çevirir.
 */
public class ChatbotUnavailableException extends ApiException {

    public ChatbotUnavailableException(String message) {
        super(HttpStatus.SERVICE_UNAVAILABLE, message);
    }
}
