package az.demo.NexoraAcademy.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Kənar chat-bot xidmətinə sinxron HTTP çağırışları üçün ayrıca konfiqurasiya
 * olunmuş {@link RestClient}. spring-boot-starter-web ilə gələn RestClient
 * istifadə olunur — əlavə asılılıq (WebClient/webflux) lazım deyil.
 *
 * <p>baseUrl və timeout-lar {@link ChatbotProperties}-dən götürülür. API açarı
 * hər sorğuda ChatbotService tərəfindən əlavə olunur (bax ChatbotService).
 */
@Configuration
@RequiredArgsConstructor
public class ChatbotClientConfig {

    private final ChatbotProperties properties;

    @Bean
    public RestClient chatbotRestClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(properties.getConnectTimeoutMs()));
        factory.setReadTimeout(Duration.ofMillis(properties.getReadTimeoutMs()));

        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .requestFactory(factory)
                .build();
    }
}
