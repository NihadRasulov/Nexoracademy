package az.demo.NexoraAcademy.service.ai;

import az.demo.NexoraAcademy.config.ChatbotProperties;
import az.demo.NexoraAcademy.dto.ai.ChatbotApiRequest;
import az.demo.NexoraAcademy.dto.ai.ChatbotApiResponse;
import az.demo.NexoraAcademy.dto.ai.ChatbotMessageRequest;
import az.demo.NexoraAcademy.dto.ai.ChatbotMessageResponse;
import az.demo.NexoraAcademy.exception.ChatbotUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * Qrup yoldaşının yazdığı kənar chat-bota mesaj ötürən servis.
 *
 * <p>Bu servis botun MƏNTİQİNİ saxlamır — sadəcə istifadəçi mesajını bota
 * ötürüb cavabı geri qaytaran "körpü"dür. Bot söndürülübsə və ya əlçatan
 * deyilsə {@link ChatbotUnavailableException} (503) atılır.
 */
@Service
public class ChatbotService {

    private static final Logger log = LoggerFactory.getLogger(ChatbotService.class);

    /** Xəta log-una cavab gövdəsindən yazılacaq maksimum simvol sayı. */
    private static final int ERROR_BODY_LOG_LIMIT = 500;

    private final RestClient chatbotRestClient;
    private final ChatbotProperties properties;

    /**
     * Spring Boot 4 Jackson 3-ə keçib, ona görə bean-in tipi
     * {@code tools.jackson.databind.ObjectMapper}-dir (Jackson 2-dəki
     * {@code com.fasterxml.jackson.databind.ObjectMapper} deyil — o hələ də
     * classpath-dədir, çünki Hibernate jsonb sütunları üçün ondan istifadə edir,
     * amma Spring bean kimi qeydiyyatdan keçmir).
     */
    private final ObjectMapper objectMapper;

    public ChatbotService(RestClient chatbotRestClient,
                          ChatbotProperties properties,
                          ObjectMapper objectMapper) {
        this.chatbotRestClient = chatbotRestClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    /**
     * İstifadəçi mesajını bota göndərir və cavabı qaytarır.
     *
     * @param request frontend-dən gələn mesaj
     * @param userId  çağıran istifadəçinin id-si (bot kontekst üçün istəyə bilər); null ola bilər
     */
    public ChatbotMessageResponse sendMessage(ChatbotMessageRequest request, String userId) {
        if (!properties.isEnabled() || !StringUtils.hasText(properties.getBaseUrl())) {
            throw new ChatbotUnavailableException("Chat-bot inteqrasiyası konfiqurasiya olunmayıb");
        }

        ChatbotApiRequest apiRequest = new ChatbotApiRequest(
                request.message(),
                request.conversationId(),
                userId
        );

        String rawBody = callBot(apiRequest);
        ChatbotApiResponse apiResponse = parseBody(rawBody);

        if (apiResponse == null || !StringUtils.hasText(apiResponse.reply())) {
            log.warn("Chat-bot boş cavab qaytardı (baseUrl={}, path={})",
                    properties.getBaseUrl(), properties.getChatPath());
            throw new ChatbotUnavailableException("Chat-bot düzgün cavab qaytarmadı");
        }

        // Söhbət id-si botdan gəlməyibsə, frontend-in göndərdiyini geri qaytarırıq
        // ki, kontekst zənciri qırılmasın. (Bot hazırda bu sahəni qaytarmır —
        // bax CHATBOT_INTEQRASIYASI.md §7.1.)
        String conversationId = StringUtils.hasText(apiResponse.conversationId())
                ? apiResponse.conversationId()
                : request.conversationId();

        return new ChatbotMessageResponse(
                apiResponse.reply(),
                conversationId,
                apiResponse.state(),
                apiResponse.actions() != null ? apiResponse.actions() : List.of(),
                apiResponse.courses() != null ? apiResponse.courses() : List.of(),
                apiResponse.capture()
        );
    }

    /**
     * Bota HTTP sorğusu atır və cavabı XAM MƏTN kimi qaytarır.
     *
     * <p>Cavab qəsdən {@code String} kimi alınır, {@code ChatbotApiResponse} kimi yox:
     * bot ara-sıra cavabı {@code Content-Type} başlığı olmadan qaytarır, Spring belə
     * cavabı {@code application/octet-stream} sayır və Jackson konverteri onu emal
     * etməkdən imtina edirdi — nəticədə eyni sorğu bəzən 503, bəzən 200 verirdi
     * (bax CHATBOT_INTEQRASIYASI.md §6.5). {@code String} konverteri isə istənilən
     * media tipini qəbul edir, ona görə parse-i özümüz edirik.
     */
    private String callBot(ChatbotApiRequest apiRequest) {
        try {
            return chatbotRestClient.post()
                    .uri(properties.getChatPath())
                    .headers(headers -> {
                        headers.setContentType(MediaType.APPLICATION_JSON);
                        headers.setAccept(List.of(MediaType.APPLICATION_JSON, MediaType.ALL));
                        // ngrok pulsuz tuneli brauzer-xəbərdarlıq səhifəsi qaytara bilər;
                        // bu başlıq onu keçir. ngrok istifadə olunmursa təsirsizdir.
                        headers.set("ngrok-skip-browser-warning", "true");
                        // API açarı yalnız təyin olunubsa göndərilir.
                        if (StringUtils.hasText(properties.getApiKey())) {
                            headers.set(properties.getApiKeyHeader(), properties.getApiKey());
                        }
                    })
                    .body(apiRequest)
                    .retrieve()
                    .body(String.class);

        } catch (RestClientException ex) {
            // Timeout, connection refused, 4xx/5xx və s. — hamısı buraya düşür.
            log.error("Chat-bota çatmaq mümkün olmadı (baseUrl={}, path={}): {}",
                    properties.getBaseUrl(), properties.getChatPath(), ex.getMessage());
            throw new ChatbotUnavailableException("Chat-bot xidmətinə hazırda çatmaq mümkün deyil");
        }
    }

    /**
     * Botun xam cavabını {@link ChatbotApiResponse}-a çevirir. Parse alınmasa,
     * səbəbi tapmaq mümkün olsun deyə gövdənin başlanğıcı da log-a yazılır —
     * belə hallarda adətən JSON yerinə HTML (ngrok/proxy səhifəsi) gəlir.
     */
    private ChatbotApiResponse parseBody(String rawBody) {
        if (!StringUtils.hasText(rawBody)) {
            log.warn("Chat-bot boş gövdə qaytardı (baseUrl={}, path={})",
                    properties.getBaseUrl(), properties.getChatPath());
            throw new ChatbotUnavailableException("Chat-bot düzgün cavab qaytarmadı");
        }

        try {
            return objectMapper.readValue(rawBody, ChatbotApiResponse.class);
        } catch (Exception ex) {
            log.error("Chat-botun cavabı JSON kimi oxuna bilmədi (baseUrl={}, path={}): {} | gövdə: {}",
                    properties.getBaseUrl(), properties.getChatPath(), ex.getMessage(), truncate(rawBody));
            throw new ChatbotUnavailableException("Chat-bot gözlənilməyən formatda cavab qaytardı");
        }
    }

    private String truncate(String body) {
        String singleLine = body.replaceAll("\\s+", " ");
        return singleLine.length() <= ERROR_BODY_LOG_LIMIT
                ? singleLine
                : singleLine.substring(0, ERROR_BODY_LOG_LIMIT) + "…";
    }
}
