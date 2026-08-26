package az.demo.NexoraAcademy.dto.ai;

import java.util.List;
import java.util.Map;

/**
 * Bizim backend-in frontend-ə qaytardığı cavab.
 *
 * <p>{@code state}/{@code actions}/{@code courses}/{@code capture} sahələri
 * botdan olduğu kimi ötürülür — əvvəllər backend bunları atırdı və nəticədə
 * chat widget-ində yalnız düz mətn görünürdü (bax CHATBOT_INTEQRASIYASI.md §3.1-B).
 *
 * @param reply          botun mətn cavabı
 * @param conversationId növbəti mesajda geri göndəriləcək söhbət id-si
 * @param state          söhbətin mərhələsi — frontend UI vəziyyətini buna görə qura bilər
 * @param actions        istifadəçiyə göstəriləcək düymələr (boş ola bilər)
 * @param courses        botun tövsiyə etdiyi kurslar (boş ola bilər)
 * @param capture        lid tutma siqnalı — frontend forma göstərmək üçün istifadə edə bilər
 */
public record ChatbotMessageResponse(
        String reply,
        String conversationId,
        String state,
        List<ChatbotAction> actions,
        List<Map<String, Object>> courses,
        String capture
) {
}
