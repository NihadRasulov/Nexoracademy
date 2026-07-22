package az.demo.NexoraAcademy.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Deliberately no real default here (bax PaymentGatewaySignatureVerifier): boş
 * webhookSecret "gateway hələ seçilməyib" vəziyyətini bildirir, imza yoxlanışı
 * bu halda bypass olunur (log xəbərdarlığı ilə). Real gateway qoşulanda
 * PAYMENT_GATEWAY_WEBHOOK_SECRET (və lazım olsa signature-header) .env-də
 * doldurulduqdan sonra yoxlama avtomatik məcburiləşir.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.payment.gateway")
public class PaymentGatewayProperties {

    private String webhookSecret = "";

    private String signatureHeader = "X-Gateway-Signature";
}
