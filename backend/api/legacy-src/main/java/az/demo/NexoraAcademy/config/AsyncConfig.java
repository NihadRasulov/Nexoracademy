package az.demo.NexoraAcademy.config;

import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.Map;

/**
 * {@code @Async} dəstəyini aktivləşdirir — hazırda yalnız
 * {@link az.demo.NexoraAcademy.service.notify.EmailService#send} istifadə edir ki,
 * SMTP gecikməsi (Gmail-də tipik olaraq 1-3 saniyə) qeydiyyat/login sorğusunun
 * cavab müddətinə əlavə olunmasın.
 *
 * <p>Spring Boot-un auto-config etdiyi {@code applicationTaskExecutor} istifadə olunur;
 * aşağıdakı {@link TaskDecorator} bean-i Boot tərəfindən həmin executor-a avtomatik
 * tətbiq edilir və MDC-ni (bax {@link az.demo.NexoraAcademy.logging.CorrelationIdFilter})
 * arxa-fon thread-inə köçürür — əks halda async loglarda correlation-id itərdi.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean
    public TaskDecorator mdcTaskDecorator() {
        return runnable -> {
            Map<String, String> callerContext = MDC.getCopyOfContextMap();

            return () -> {
                Map<String, String> previousContext = MDC.getCopyOfContextMap();

                if (callerContext != null) {
                    MDC.setContextMap(callerContext);
                } else {
                    MDC.clear();
                }

                try {
                    runnable.run();
                } finally {
                    // Thread pool thread-ləri təkrar istifadə olunur — əvvəlki
                    // vəziyyəti bərpa etməsək, MDC növbəti tapşırığa sızar.
                    if (previousContext != null) {
                        MDC.setContextMap(previousContext);
                    } else {
                        MDC.clear();
                    }
                }
            };
        };
    }
}
