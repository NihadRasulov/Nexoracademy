package az.demo.NexoraAcademy.security;

import az.demo.NexoraAcademy.config.PaymentGatewayProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Guards the public payment gateway webhook (bax SecurityConfig: /api/v1/payments/callback
 * is permitAll, since a gateway can't carry our JWT). Runs before the body is deserialized
 * so a forged/unsigned callback never reaches PaymentController/PaymentService.
 *
 * See PaymentGatewaySignatureVerifier for what happens when no secret is configured yet
 * (bypassed with a warning, not rejected) — that's intentional until a real gateway is chosen.
 */
@Component
@RequiredArgsConstructor
public class PaymentCallbackSignatureFilter extends OncePerRequestFilter {

    private static final String CALLBACK_PATH = "/api/v1/payments/callback";

    private final PaymentGatewayProperties properties;
    private final PaymentGatewaySignatureVerifier verifier;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {

        if (!CALLBACK_PATH.equals(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(request);
        String signature = request.getHeader(properties.getSignatureHeader());

        if (!verifier.verify(cachedRequest.getCachedBody(), signature)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Invalid gateway signature\"}");
            return;
        }

        filterChain.doFilter(cachedRequest, response);
    }
}
