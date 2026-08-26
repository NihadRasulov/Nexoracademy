package az.demo.NexoraAcademy.dto.ai;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Bizim backend-in KƏNAR bota göndərdiyi JSON gövdəsi.
 *
 * <p>DİQQƏT — buradakı sahə adları (message/conversation_id/user_id) botun
 * gözlədiyi JSON açarları ilə EYNİ olmalıdır. Qrup yoldaşın botun hansı
 * açarları qəbul etdiyini deyəndə burada uyğunlaşdır (məs. bot "text" gözləyirsə
 * {@code message} → {@code text} et). Null sahələr JSON-a daxil edilmir.
 *
 * @param message        istifadəçi mesajı
 * @param conversationId söhbət konteksti üçün id (bot dəstəkləyirsə)
 * @param userId         çağıran istifadəçinin id-si (bot loglama/kontekst üçün istəyirsə)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ChatbotApiRequest(
        String message,
        String conversationId,
        String userId
) {
}
