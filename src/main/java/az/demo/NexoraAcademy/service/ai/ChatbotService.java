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

    private final RestClient chatbotRestClient;
    private final ChatbotProperties properties;

    public ChatbotService(RestClient chatbotRestClient, ChatbotProperties properties) {
        this.chatbotRestClient = chatbotRestClient;
        this.properties = properties;
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

        try {
            ChatbotApiResponse apiResponse = chatbotRestClient.post()
                    .uri(properties.getChatPath())
                    .headers(headers -> {
                        headers.setContentType(MediaType.APPLICATION_JSON);
                        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
                        // API açarı yalnız təyin olunubsa göndərilir.
                        if (StringUtils.hasText(properties.getApiKey())) {
                            headers.set(properties.getApiKeyHeader(), properties.getApiKey());
                        }
                    })
                    .body(apiRequest)
                    .retrieve()
                    .body(ChatbotApiResponse.class);

            if (apiResponse == null || !StringUtils.hasText(apiResponse.reply())) {
                log.warn("Chat-bot boş cavab qaytardı (baseUrl={}, path={})",
                        properties.getBaseUrl(), properties.getChatPath());
                throw new ChatbotUnavailableException("Chat-bot düzgün cavab qaytarmadı");
            }

            // Söhbət id-si botdan gəlməyibsə, frontend-in göndərdiyini geri qaytarırıq
            // ki, kontekst zənciri qırılmasın.
            String conversationId = StringUtils.hasText(apiResponse.conversationId())
                    ? apiResponse.conversationId()
                    : request.conversationId();

            return new ChatbotMessageResponse(apiResponse.reply(), conversationId);

        } catch (RestClientException ex) {
            // Timeout, connection refused, 4xx/5xx və s. — hamısı buraya düşür.
            log.error("Chat-bota çatmaq mümkün olmadı (baseUrl={}, path={}): {}",
                    properties.getBaseUrl(), properties.getChatPath(), ex.getMessage());
            throw new ChatbotUnavailableException("Chat-bot xidmətinə hazırda çatmaq mümkün deyil");
        }
    }
}
