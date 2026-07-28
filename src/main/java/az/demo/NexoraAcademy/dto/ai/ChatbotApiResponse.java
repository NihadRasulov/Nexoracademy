package az.demo.NexoraAcademy.dto.ai;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * KƏNAR botdan gələn JSON cavabın backend tərəfindəki qarşılığı.
 *
 * <p>{@code ignoreUnknown=true} sayəsində botun cavabında əlavə/naməlum
 * sahələr olsa da xəta vermir. {@link JsonAlias} ilə ən çox rast gəlinən
 * açar adları eyni sahəyə xəritələnir — beləcə bot "reply", "response",
 * "answer", "message" və ya "text"-dən hansını qaytarsa da tuturuq.
 *
 * <p>Botun cavab formatı fərqlidirsə (məs. iç-içə {@code data.reply}), bu
 * record-u ona uyğun dəyiş.
 *
 * @param reply          botun mətn cavabı
 * @param conversationId botun qaytardığı söhbət id-si (varsa)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ChatbotApiResponse(
        @JsonAlias({"response", "answer", "message", "text", "output"})
        String reply,

        @JsonAlias({"conversation_id", "session_id", "sessionId", "chatId", "chat_id"})
        String conversationId
) {
}
