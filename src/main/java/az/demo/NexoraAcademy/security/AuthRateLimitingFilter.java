package az.demo.NexoraAcademy.security;

import jakarta.annotation.PreDestroy;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory, per-IP fixed-window rate limiter for the auth endpoints most exposed to
 * brute-force / credential-stuffing / spam: password login, OTP verification,
 * registration, and the two "always succeeds, sends an email" endpoints
 * (forgot-password, resend-verification) which would otherwise let anyone mail-bomb
 * an arbitrary address for free (see AuthService#forgotPassword / #resendVerification).
 *
 * Deliberately in-memory (ConcurrentHashMap), not Redis/Bucket4j-backed — this app
 * runs as a single instance. If it's ever scaled horizontally behind a load balancer,
 * swap the backing store for a shared one (e.g. Redis) so the limit is enforced across
 * all instances instead of per-instance.
 */
@Component
public class AuthRateLimitingFilter extends OncePerRequestFilter {

    private static final Map<String, Integer> LIMITS_PER_MINUTE = Map.of(
            "/api/v1/auth/register", 5,
            "/api/v1/auth/login", 10,
            "/api/v1/auth/login/verify-otp", 10,
            "/api/v1/auth/forgot-password", 5,
            "/api/v1/auth/resend-verification", 5
    );

    private static final long WINDOW_MILLIS = 60_000L;
    private static final long STALE_AFTER_MILLIS = WINDOW_MILLIS * 5;

    private final Map<String, Window> windows = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor =
            Executors.newSingleThreadScheduledExecutor(runnable -> {
                Thread thread = new Thread(runnable, "auth-rate-limit-cleanup");
                thread.setDaemon(true);
                return thread;
            });

    public AuthRateLimitingFilter() {
        cleanupExecutor.scheduleAtFixedRate(this::evictStaleWindows, 5, 5, TimeUnit.MINUTES);
    }

    @PreDestroy
    void shutdown() {
        cleanupExecutor.shutdownNow();
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {

        Integer limit = LIMITS_PER_MINUTE.get(request.getRequestURI());
        if (limit == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = clientIp(request) + "|" + request.getRequestURI();
        if (!tryConsume(key, limit)) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"status\":429,\"error\":\"Too Many Requests\","
                            + "\"message\":\"Too many attempts, please try again later.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean tryConsume(String key, int limit) {
        long now = System.currentTimeMillis();
        Window window = windows.computeIfAbsent(key, k -> new Window(now));

        long start = window.windowStart.get();
        if (now - start >= WINDOW_MILLIS && window.windowStart.compareAndSet(start, now)) {
            window.count.set(0);
        }

        return window.count.incrementAndGet() <= limit;
    }

    private void evictStaleWindows() {
        long now = System.currentTimeMillis();
        windows.entrySet().removeIf(entry -> now - entry.getValue().windowStart.get() > STALE_AFTER_MILLIS);
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static final class Window {
        final AtomicInteger count = new AtomicInteger(0);
        final AtomicLong windowStart;

        Window(long start) {
            this.windowStart = new AtomicLong(start);
        }
    }
}
