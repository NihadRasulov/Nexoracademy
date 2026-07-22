package az.demo.NexoraAcademy.security;

import az.demo.NexoraAcademy.config.PaymentGatewayProperties;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

/**
 * Verifies the HMAC-SHA256 signature a payment gateway attaches to its webhook
 * callback ({@link az.demo.NexoraAcademy.controller.billing.PaymentController#callback}).
 * This is the near-universal minimum scheme (raw request body signed with a shared
 * secret) — if the real gateway you integrate uses a different scheme (e.g. a
 * timestamp+payload signature, or certificate-based verification like some
 * providers use), adapt {@link #verify} accordingly instead of the header comparison.
 *
 * No secret configured (PAYMENT_GATEWAY_WEBHOOK_SECRET unset) means no real gateway
 * is wired up yet — verification is bypassed (loudly, via a WARN log) so local/dev
 * use and existing tests keep working. The moment a secret is set, verification
 * becomes mandatory and unsigned/mismatched callbacks are rejected with 401.
 */
@Component
@RequiredArgsConstructor
public class PaymentGatewaySignatureVerifier {

    private static final Logger log = LoggerFactory.getLogger(PaymentGatewaySignatureVerifier.class);

    private final PaymentGatewayProperties properties;

    public boolean isConfigured() {
        return properties.getWebhookSecret() != null && !properties.getWebhookSecret().isBlank();
    }

    public boolean verify(byte[] rawBody, String signatureHeaderValue) {
        if (!isConfigured()) {
            log.warn("PAYMENT_GATEWAY_WEBHOOK_SECRET is not set - /api/v1/payments/callback signature is "
                    + "NOT being verified. Do not go live with a real payment gateway until this is configured.");
            return true;
        }

        if (signatureHeaderValue == null || signatureHeaderValue.isBlank()) {
            return false;
        }

        String expected = hmacSha256Hex(rawBody, properties.getWebhookSecret());
        byte[] expectedBytes = expected.getBytes(StandardCharsets.UTF_8);
        byte[] actualBytes = signatureHeaderValue.trim().getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(expectedBytes, actualBytes);
    }

    private String hmacSha256Hex(byte[] body, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(body));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to compute payment gateway webhook signature", e);
        }
    }
}
