package az.demo.NexoraAcademy.dto.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Botun cavabında gələn təklif düyməsi.
 *
 * <p>Botun UX-i düymələr üzərində qurulub — istifadəçi mətn yazmaq əvəzinə
 * hazır variantlardan birini seçir. Əvvəllər bu massiv backend-də səssizcə
 * atılırdı (bax CHATBOT_INTEQRASIYASI.md §3.1-B), ona görə widget-də yalnız
 * düz mətn görünürdü.
 *
 * @param type  düymənin tipi (məs. "button")
 * @param label istifadəçiyə göstərilən mətn (məs. "Proqramlaşdırma")
 * @param value seçildikdə bota geri göndəriləcək dəyər (məs. "proqramlashdirma")
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ChatbotAction(
        String type,
        String label,
        String value
) {
}
