package az.demo.NexoraAcademy.config;

import az.demo.NexoraAcademy.security.AuthRateLimitingFilter;
import az.demo.NexoraAcademy.security.CustomAccessDeniedHandler;
import az.demo.NexoraAcademy.security.JwtAuthenticationEntryPoint;
import az.demo.NexoraAcademy.security.JwtAuthenticationFilter;
import az.demo.NexoraAcademy.security.PaymentCallbackSignatureFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthRateLimitingFilter authRateLimitingFilter;
    private final PaymentCallbackSignatureFilter paymentCallbackSignatureFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http

                // REST API olduğu üçün CSRF istifadə etmirik
                .csrf(csrf -> csrf.disable())

                // CORS ayrıca CorsConfig classında idarə olunacaq
                .cors(Customizer.withDefaults())

                // Session saxlanılmayacaq (JWT üçün lazımdır)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Exception Handling
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )

                // Authorization Rules
                .authorizeHttpRequests(auth -> auth

                        // Authentication
                        .requestMatchers(
                                "/api/v1/auth/**"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/applications"
                        ).permitAll()

                        // Payment gateway webhook — bax PaymentController.callback() qeydi:
                        // imza yoxlanışı PaymentCallbackSignatureFilter-də edilir (permitAll
                        // burada yalnız rol/JWT tələbini aradan qaldırır, filter-i yox).
                        .requestMatchers(
                                "/api/v1/payments/callback"
                        ).permitAll()

                        // Public APIs — yalnız oxuma (GET). Yazma əməliyyatları aşağıdakı
                        // catalog qaydası ilə CONTENT_MANAGER/ADMIN-ə məhdudlaşdırılır.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/courses/**",
                                "/api/v1/categories/**"
                        ).permitAll()

                        // Public marketing data is exposed through deliberately small,
                        // published-only projections; admin CRUD endpoints stay protected.
                        .requestMatchers(HttpMethod.GET, "/api/v1/public/**").permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/public/newsletter/subscriptions",
                                "/api/v1/public/contact-submissions").permitAll()

                        // Self-service notification endpoints must precede the admin-only
                        // notifications wildcard below.
                        .requestMatchers(
                                "/api/v1/notifications/me",
                                "/api/v1/notifications/*/read"
                        ).authenticated()

                        // Swagger
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // Health Check — "/**" vacibdir: application-prod.yml
                        // liveness/readiness probe-larını aktivləşdirir, onların path-i isə
                        // /actuator/health/liveness və /actuator/health/readiness-dir.
                        // Yalnız dəqiq "/actuator/health" yazılanda k8s probe-ları 401 alırdı.
                        // Prod-da actuator ayrıca management portuna (9091) köçür və bu
                        // zəncir ora ümumiyyətlə tətbiq olunmur — bax application-prod.yml.
                        .requestMatchers(
                                "/actuator/health",
                                "/actuator/health/**"
                        ).permitAll()

                        // Prometheus scrape endpoint-i. Ayrıca management portu (9091)
                        // öz-özlüyündə KİFAYƏT ETMİR — bu filter zənciri həmin porta da
                        // tətbiq olunur (sınaqla təsdiqləndi: 9091/actuator/prometheus → 401),
                        // Prometheus isə JWT göndərə bilmir.
                        // Təhlükəsizlik: bu endpoint yalnız prod-da mövcuddur
                        // (management.endpoints.web.exposure.include), yalnız 9091-dədir və
                        // 9091 nə Service-də, nə ingress-də xaricə verilir — yəni yalnız
                        // klaster daxilindən oxuna bilir (bax k8s/06-network-policy.yaml).
                        // Dev profilində bu endpoint ümumiyyətlə açılmır (404).
                        .requestMatchers(
                                "/actuator/prometheus"
                        ).permitAll()

                        // Self-service — istənilən autentifikasiya olunmuş istifadəçi öz
                        // profilinə çata bilər. Aşağıdakı "/api/v1/users/**" (ADMIN-only)
                        // qaydasından ƏVVƏL olmalıdır ki, ilk uyğunluq bunu tutsun.
                        .requestMatchers(
                                "/api/v1/users/me",
                                "/api/v1/users/me/**"
                        ).authenticated()

                        // Admin APIs — SYSTEM_ADMIN da ADMIN-in bütün səlahiyyətlərinə malik olmalıdır.
                        .requestMatchers(
                                "/api/v1/admin/**",
                                "/api/v1/users/**"
                        ).hasAnyRole("ADMIN", "SYSTEM_ADMIN")

                        // Main-site public access: published CMS content for anonymous visitors.
                        // Bu qayda admin content qaydasından ƏVVƏL olmalıdır ki,
                        // ilk uyğunluq bunu tutsun.
                        .requestMatchers(HttpMethod.GET, "/api/v1/content/cms-content")
                                .permitAll()

                        // Content Manager — courses/categories-in GET-dən sonrakı bu qayda yalnız
                        // digər HTTP metodlarına (POST/PUT/PATCH/DELETE) və digər catalog
                        // endpointlərinin (instructors, course-groups, course-instructors) HAMISINA aiddir,
                        // çünki Spring Security ilk uyğun gələn qaydanı tətbiq edir.
                        .requestMatchers(
                                "/api/v1/content/**",
                                "/api/v1/courses/**",
                                "/api/v1/categories/**",
                                "/api/v1/instructors/**",
                                "/api/v1/course-groups/**",
                                "/api/v1/course-instructors/**"
                        ).hasAnyRole("ADMIN", "SYSTEM_ADMIN", "CONTENT_MANAGER")

                        // Digər content-tipli resurslar (bilgi bazası məqalələri, məzun uğur
                        // hekayələri) da yalnız CONTENT_MANAGER/ADMIN tərəfindən idarə olunur —
                        // əvvəllər bu path-lər üçün heç bir qayda yox idi, "anyRequest().authenticated()"-ə
                        // düşürdü, yəni istənilən STUDENT tam CRUD edə bilirdi.
                        .requestMatchers(
                                "/api/v1/kb-articles/**",
                                "/api/v1/graduate-outcomes/**"
                        ).hasAnyRole("ADMIN", "SYSTEM_ADMIN", "CONTENT_MANAGER")

                        // Sales / CRM
                        .requestMatchers(
                                "/api/v1/sales/**"
                        ).hasAnyRole("ADMIN", "SYSTEM_ADMIN", "SALES_CRM")

                        // Maliyyə/hesab idarəçiliyi — heç bir gateway inteqrasiyası olmadığı üçün
                        // (bax PaymentController.callback) amount/status client tərəfindən tam
                        // idarə olunur; bunu STUDENT-ə açıq saxlamaq saxta ödəniş/capture riski
                        // yaradırdı. Eyni səbəbdən scholarship (endirim faizi) da admin-only.
                        .requestMatchers(
                                "/api/v1/payments/**",
                                "/api/v1/scholarships/**"
                        ).hasAnyRole("ADMIN", "SYSTEM_ADMIN")

                        // Sessiyalar və OAuth hesab bağlantıları — xalis daxili/identity idarəçiliyi.
                        // Öz-özünə xidmət artıq /api/v1/auth/** (login/refresh/logout) ilə təmin
                        // olunur, bu CRUD-lar heç vaxt son istifadəçiyə açıq olmamalıdır.
                        .requestMatchers(
                                "/api/v1/sessions/**",
                                "/api/v1/oauth-accounts/**"
                        ).hasAnyRole("ADMIN", "SYSTEM_ADMIN")

                        // Bildirişlər — daxili audit/log xarakterli resurs, istifadəçiyə görə
                        // filtrlənən "/me" endpoint-i olmadığından tam CRUD admin-only qalır.
                        .requestMatchers(
                                "/api/v1/notifications/**"
                        ).hasAnyRole("ADMIN", "SYSTEM_ADMIN")

                        // Chat-bot — daxil olmuş istənilən istifadəçi mesaj göndərə bilər.
                        // Botu saytda GİRİŞSİZ (public) widget kimi açmaq istəsən, bu sətri
                        // .permitAll() ilə əvəz et — onda ChatbotController-də principal null
                        // olacaq (userId göndərilməyəcək), qalan hər şey işləyəcək.
                        .requestMatchers(
                                "/api/v1/chatbot/**"
                        ).authenticated()

                        // Digər bütün endpointlər authentication tələb edir (course-reviews,
                        // enrollments — bunlar sahiblik yoxlaması ilə servis səviyyəsində
                        // qorunur, bax CourseReviewService / EnrollmentService)
                        .anyRequest()
                        .authenticated()
                )

                // Rate limiter runs before authentication so throttled requests never even
                // reach JwtAuthenticationFilter / the authentication manager.
                .addFilterBefore(authRateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(paymentCallbackSignatureFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {

        return configuration.getAuthenticationManager();
    }

}
