package az.demo.NexoraAcademy.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Hər request-ə bir correlation id təyin edir (client "X-Correlation-Id" göndərsə onu
 * saxlayır, göndərməsə yeni UUID yaradır), MDC-yə qoyur ki, eyni request-in bütün log
 * sətirləri (CrudLoggingAspect daxil olmaqla) həmin id ilə görünsün — mərkəzləşdirilmiş
 * log toplama zamanı (ELK/Loki s.) tək request-in bütün sətirlərini birləşdirmək üçün.
 *
 * Qəsdən @Component DEYİL — servlet konteynerinə qoşulması
 * {@link az.demo.NexoraAcademy.config.WebFilterConfig}-dəki
 * FilterRegistrationBean üzərindən, HIGHEST_PRECEDENCE sırası ilə edilir ki, Spring
 * Security filter zəncirindən (o da öz növbəsində containerə qoşulmuş bir filter-dir)
 * əvvəl işə düşsün — beləliklə rate-limit/auth rədd cavabları da correlation id ilə
 * loglanır. @Component kimi işarələsəydik, Spring Boot onu HƏM bu bean qeydiyyatı
 * ilə, HƏM DƏ avtomatik filter aşkarlanması ilə iki dəfə qeydiyyatdan keçirər, nəticədə
 * hər request üçün filter iki dəfə işləyərdi.
 */
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String MDC_KEY = "correlationId";
    public static final String HEADER_NAME = "X-Correlation-Id";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {

        String incoming = request.getHeader(HEADER_NAME);
        String correlationId = (incoming != null && !incoming.isBlank())
                ? incoming.trim()
                : UUID.randomUUID().toString();

        MDC.put(MDC_KEY, correlationId);
        response.setHeader(HEADER_NAME, correlationId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }
}
