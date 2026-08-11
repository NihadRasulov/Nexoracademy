package az.demo.NexoraAcademy.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Qrup yoldaşının öz yazdığı (kənar, self-hosted) chat-bot xidmətinə qoşulma
 * konfiqurasiyası. Bütün dəyərlər application.yml → env dəyişənlərindən oxunur
 * (bax .env / application.yml-dəki "app.chatbot" bloku).
 *
 * <p>{@code enabled=false} olanda ChatbotService heç bir HTTP çağırışı etmir və
 * 503 qaytarır — yəni bot URL-i hələ verilməyibsə tətbiq problemsiz işə düşür
 * (eyni EmailService-in "best-effort" məntiqi kimi).
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.chatbot")
public class ChatbotProperties {

    /** Bot inteqrasiyası ümumiyyətlə aktivdirmi. URL verilməmişsə false saxla. */
    private boolean enabled = false;

    /** Botun kök ünvanı, məs. http://192.168.1.50:5000 və ya https://bot.mysite.com */
    private String baseUrl = "";

    /** baseUrl-dən sonrakı mesaj göndərmə yolu — botun endpoint-inə görə dəyiş. */
    private String chatPath = "/chat";

    /** Bot API açarı (varsa). Boşdursa header əlavə olunmur. */
    private String apiKey = "";

    /** API açarının hansı HTTP başlığında göndərilməsi (botun tələbinə görə). */
    private String apiKeyHeader = "X-API-Key";

    /** Bağlantı timeout-u (ms). */
    private int connectTimeoutMs = 3000;

    /** Cavab gözləmə timeout-u (ms) — bot yavaş cavab verə bilər, ona görə uzun. */
    private int readTimeoutMs = 20000;
}
