# NexoraAcademy — Production Readiness Audit

**Tarix:** 2026-07-22
**Əhatə dairəsi:** Spring Boot 4.1 / PostgreSQL / Flyway backend (`az.demo.NexoraAcademy`)
**Metod:** Bütün `application*.yml`, `.env`/`.env.example`, `pom.xml`, `docker-compose.yml`, `CorsConfig`, `SecurityConfig`, `GlobalExceptionHandler`, `JwtService`/`JwtProperties`, `AuthService`/`AuthController`, `EmailService`, bütün 23 controller sinfi, `logback-spring.xml`, `CrudLoggingAspect`, git tarixçəsi bilavasitə oxunub.

> Qeyd: `application-dev.yml`/`application-prod.yml`, `CorsConfig`, `@RestControllerAdvice`, Flyway strukturunun **mövcud olması** artıq qəbul edilib — aşağıda yalnız bunların **içindəki konfiqurasiyanın düzgünlüyü** qiymətləndirilir.

---

## Yenilənmə qeydi (2026-07-22, ilkin audit-dən sonra)

İlkin audit-də tapılan bir sıra maddə artıq kodda düzəldilib. Aşağıdakı "Bölmə 1-10" hissəsi **orijinal tapıntıları** əks etdirir (tarixi referans üçün saxlanılıb); faktiki hazırkı vəziyyət üçün ən aşağıdakı **"Prioritet Sıralı Checklist"** və **"Deploy Etməzdən Əvvəl"** bölmələrinə bax — onlar yenilənib.

**Bu sessiyada görülən işlər:**
- Production Dockerfile (+ `.dockerignore`) yaradıldı, non-root user permission bug-ı (log qovluğu yazma icazəsi) tapılıb düzəldildi, real `docker compose build/up` ilə test olundu — `/actuator/health` → `UP`.
- `docker-compose.yml`-a `app` servisi əlavə olundu (build, `DB_HOST=postgres`/`MAIL_HOST=mailhog` override, log-lar üçün named volume).
- `.gitignore` korlanmış vəziyyətdən təmizləndi; `.env` və `.env.example` hər ikisi artıq ignore olunur; `.env.example` git tracking-dən çıxarıldı (commit gözləyir).
- `app.cors.allowed-origins` `CORS_ALLOWED_ORIGINS` env-dəyişəninə bağlandı (dev-də localhost default-u, prod-da default yoxdur — fail-fast).
- `/api/v1/auth/register|login|login/verify-otp|forgot-password|resend-verification` üçün IP-əsaslı rate limiting əlavə olundu (`AuthRateLimitingFilter`).
- `TestMailController` silindi.
- Hikari `leak-detection-threshold: 30000` əlavə olundu.
- Ödəniş gateway-i üçün HMAC-SHA256 imza doğrulama mexanizmi yazıldı (`PaymentGatewayProperties`/`PaymentGatewaySignatureVerifier`/`PaymentCallbackSignatureFilter`) — `PAYMENT_GATEWAY_WEBHOOK_SECRET` boş olduğu müddətcə bypass olunur (WARN logu ilə), dəyər veriləndə məcburiləşir.
- `DB_PASSWORD` istifadəçi tərəfindən rotasiya edildi (`docker compose down -v` → `.env` yeniləndi → `up -d`) və doğrulandı.

---

## 1. Environment / Config

### ✅ HAZIR — `ddl-auto=validate`, heç bir `update`/`create` yoxdur
`src/main/resources/application.yml:26` və `src/main/resources/application-prod.yml:19` — hər ikisi `hibernate.ddl-auto: validate`. Schema idarəçiliyi tam Flyway-ə həvalə olunub (yorum sətri: "Hibernate heç vaxt schema yaratmamalı/dəyişməməlidir"). Düzgün konfiqurasiya, əlavə iş lazım deyil.

### ✅ HAZIR — `show-sql` yalnız dev profilində aktivdir
`src/main/resources/application-dev.yml:12` (`spring.jpa.show-sql: true`) — bu açar `application.yml` və `application-prod.yml`-da yoxdur, yəni default `false` qalır. Prod-da SQL konsola/log-a yazılmır.

### ✅ HAZIR — Flyway `validate-on-migrate=true`, `out-of-order=false`
`src/main/resources/application.yml:42-43`. `application-prod.yml:21-22` yalnız `enabled: true` təkrarlayır, digər Flyway ayarları base `application.yml`-dan miras qalır (`baseline-on-migrate`, `validate-on-migrate`, `schemas` s.) — düzgündür, təkrar tərif lazım deyil.

### ✅ HAZIR — Prod profili üçün default dəyər yoxdur (fail-fast)
`application-prod.yml:12-14` — `${DB_HOST}`, `${DB_PORT}`, `${DB_NAME}`, `${DB_USER}`, `${DB_PASSWORD}` üçün heç bir `:default` placeholder yoxdur. Bu dəyişənlər prod-da təyin olunmasa Spring Boot açılış zamanı xəta verib dayanacaq — dev default-ları (`DB_PASSWORD:1234`, bax `application.yml:15`) təsadüfən prod-a sızmayacaq. Fayl başındakı şərh bunu izah edir və düzgündür.

### ❌ ÇATIŞMIR — `app.cors.allowed-origins` heç bir yml faylında təyin olunmayıb
`CorsProperties` (`src/main/java/az/demo/NexoraAcademy/config/CorsProperties.java:14-17`) `app.cors.allowed-origins`-i `ConfigurationProperties` kimi oxuyur və default olaraq **boş `ArrayList`** ilə başlayır. Amma bu açar nə `application.yml`-da, nə `application-prod.yml`-da, nə `.env`/`.env.example`-də mövcud deyil (yoxlanıldı — heç bir faylda `cors` sözü keçmir).

**Niyə problemdir:** `CorsConfig.corsConfigurationSource()` (`CorsConfig.java:24`) `corsProperties.getAllowedOrigins()`-i birbaşa `setAllowedOrigins()`-ə ötürür. Boş siyahı ilə brauzer heç bir origin-ə `Access-Control-Allow-Origin` başlığı qaytarmayacaq — yəni frontend prod-da **hər origin-dən** bloklanacaq (bu, wildcard-dan fərqli, əks istiqamətli bir problemdir: təhlükəsiz, amma tətbiq işləməyəcək).

**Düzəliş** — `application-prod.yml`-a əlavə et:
```yaml
app:
  cors:
    allowed-origins: ${CORS_ALLOWED_ORIGINS}
```
və `.env.example` / prod `.env`-ə:
```
CORS_ALLOWED_ORIGINS=https://app.nexora-academy.com,https://admin.nexora-academy.com
```
(Spring `List<String>` üçün vergüllə ayrılmış env-dəyəri avtomatik siyahıya çevirir.)

---

## 2. Security

### ✅ HAZIR — JWT secret prod-da hardcode deyil, default-suzdur
`application-prod.yml:29` — `app.jwt.secret: ${JWT_SECRET}` (placeholder-siz). `JwtProperties.java:15`-dəki `"change-me-change-me-..."` yalnız **dev fallback** kimi `application.yml:74`-də istifadə olunur (`${JWT_SECRET:change-me-...}`), prod profilində bu default keçərsiz olur. Kod səviyyəsində düzgün dizayn edilib.

### ❌ ÇATIŞMIR (KRİTİK) — hazırkı `.env`-dəki JWT_SECRET və DB_PASSWORD, git-ə commit olunmuş `.env.example` ilə **eynidir**
`.env:12` və `.env.example:12`-dəki `JWT_SECRET` dəyəri (`asdghagkjasddjkasdfkasdkjadjkakjadsfjkadfbkvdaskbadskbhad...`) hərfi-hərfinə eynidir; eyni şəkildə `DB_PASSWORD=1234` da hər iki faylda eyni. `.env.example` `git log --all -p -- .env.example` ilə təsdiqləndi: bu fayl 3 commit-də (`e466ec7`, `a52c18f`, `c8cfc98`) dəyişdirilib və **hazırda repoda committed vəziyyətdədir**.

**Niyə kritikdir:** `.env` özü `.gitignore`-dadır (`(.gitignore:37-39)`) və heç vaxt commit olunmayıb (git tarixçəsində yoxdur) — bu hissə düzgündür. Amma developer `.env.example`-i `.env`-ə köçürüb **heç vaxt real dəyərləri əvəz etməyib**. Nəticədə hazırkı "gizli" JWT imza açarı və DB şifrəsi əslində repo-nu klonlayan hər kəsə açıqdır. Bu dəyərlər production-a bu şəkildə aparılarsa, istənilən kəs `.env.example`-dəki JWT_SECRET-lə etibarlı access/refresh token saxtalaşdıra bilər (bax `JwtService.signingKey()`, sətir 97-98).

**Düzəliş:**
1. Yeni JWT secret generasiya et: `openssl rand -base64 48` və nəticəni yalnız real, commit olunmayan prod `.env`/secret manager-ə yaz.
2. `DB_PASSWORD`-u da real, güclü şifrə ilə dəyiş (prod Postgres instansında).
3. `.env.example`-dəki nümunə dəyərləri açıq şəkildə "əvəz edilməli" placeholder-lərə çevir, məsələn:
   ```
   JWT_SECRET=REPLACE_WITH_OUTPUT_OF__openssl_rand_base64_48
   DB_PASSWORD=REPLACE_ME
   ```
4. Bu commit-dən sonra, əgər bu JWT_SECRET hər hansı canlı/staging mühitdə istifadə olunubsa, o mühitdəki bütün açıq sessiyaları/refresh token-ləri etibarsız hesab et.

### ✅ HAZIR — Şifrələr BCrypt ilə hash-lənir
`SecurityConfig.java:162-164` — `BCryptPasswordEncoder` bean-i. `AuthService.register()` (`AuthService.java:76`) `passwordEncoder.encode(request.password())` çağırır, plain-text saxlanma yoxdur.

### ✅ HAZIR — Admin/həssas endpoint-lər `SecurityConfig`-də rol əsaslı qorunur
`SecurityConfig.java:93-147` — `/api/v1/admin/**`, `/api/v1/users/**`, `/api/v1/payments/**`, `/api/v1/scholarships/**`, `/api/v1/sessions/**`, `/api/v1/oauth-accounts/**`, `/api/v1/notifications/**` `hasAnyRole("ADMIN","SYSTEM_ADMIN")`-ə bağlanıb; `content/courses/categories/instructors` yazma əməliyyatları `CONTENT_MANAGER`-ə açılır. `anyRequest().authenticated()` (sətir 152-153) qalanları default-olaraq bağlayır. Struktur məntiqli və sızma yoxdur.

### ⚠️ QİSMƏN HAZIR — `PaymentController.callback()` imza doğrulaması olmadan public-dir
`SecurityConfig.java:62-64` `/api/v1/payments/callback`-i `permitAll()` edir, `PaymentController.java:65-70`-dəki şərh bunu özü etiraf edir: *"real inteqrasiyada bura gateway-in imza yoxlanışı əlavə edilməlidir"*. Hazırda real gateway bağlanmadığı üçün funksional olaraq problemsizdir, amma **real ödəniş provayderi qoşulmadan bu endpoint production-a çıxarılmamalıdır** — hər kəs saxta callback göndərib `PaymentService.handleCallback()`-i işə sala bilər.

**Düzəliş (gateway seçildikdən sonra):** `PaymentController.callback()`-ə HMAC/imza başlığı yoxlaması əlavə et, məsələn:
```java
@PostMapping("/callback")
public ResponseEntity<PaymentResponse> callback(
        @RequestHeader("X-Gateway-Signature") String signature,
        @Valid @RequestBody PaymentCallbackRequest request) {
    paymentGatewaySignatureVerifier.verify(signature, request);
    return ResponseEntity.ok(paymentService.handleCallback(request));
}
```

### ⚠️ QİSMƏN HAZIR — Dev-only test controller production build-ə daxildir
`src/main/java/az/demo/NexoraAcademy/controller/test/TestMailController.java` — `/test/mail` endpoint-i `EmailService.send()`-i istənilən vaxt işə salır. `SecurityConfig`-də `/test/**` üçün ayrıca qayda yoxdur, ona görə `anyRequest().authenticated()` onu tutur (tam açıq deyil), amma bu, istehsalat kod bazasında qalmamalı debug artefaktıdır.

**Düzəliş:** Faylı sil, ya da `@Profile("dev")` ilə işarələ:
```java
@Profile("dev")
@RestController
@RequestMapping("/test")
public class TestMailController { ... }
```

### ⚠️ QİSMƏN HAZIR — Swagger UI / OpenAPI docs prod-da da açıqdır
`SecurityConfig.java:75-78` `/swagger-ui/**` və `/v3/api-docs/**`-i profil fərqi qoymadan `permitAll()` edir; `application-prod.yml`-da bunları söndürən heç bir `springdoc.*` ayarı yoxdur. `OpenApiConfig.java` boş bir stub sinifdir (heç bir `@Bean` yoxdur), yəni springdoc auto-config default rejimdə işləyir və bütün endpoint sxemini/DTO strukturunu ictimaiyyətə açır.

**Düzəliş** — `application-prod.yml`-a əlavə et:
```yaml
springdoc:
  api-docs:
    enabled: false
  swagger-ui:
    enabled: false
```

---

## 3. CORS konfigurasiyası

`CorsConfig.java` tam oxundu:

- ✅ **HAZIR** — `allowedOrigins` hardcode `localhost` və ya `"*"` DEYİL, `CorsProperties` üzərindən xarici konfiqurasiyaya bağlıdır (`CorsConfig.java:24`).
- ✅ **HAZIR** — `allowCredentials(true)` (sətir 42) ilə birgə wildcard origin istifadə olunmayıb — Spring bu kombinasiyanı zatən qadağan edir, amma kod da onu tətbiq etmir.
- ❌ **ÇATIŞMIR** — bax yuxarı, Bölmə 1: `app.cors.allowed-origins` heç yerdə dəyər almayıb, ona görə hazırkı vəziyyətdə origin siyahısı **boşdur**, yəni bütün cross-origin sorğular rədd olunacaq. Kodun özündə "localhost qalıb" problemi YOXDUR — problem, dəyərin heç təyin olunmamasıdır.

---

## 4. Exception Handling

`GlobalExceptionHandler.java` (112 sətir) tam oxundu:

### ✅ HAZIR — Stack trace və ya daxili sinif adları response-da sızmır
- `handleUnexpected()` (sətir 105-110): istənilən gözlənilməz `Exception` tutulur, `log.error(...)` ilə **serverdə** loglanır, amma client-ə yalnız generic `"An unexpected error occurred"` (500) qaytarılır — stack trace, exception sinif adı və ya mesajı response-a düşmür.
- `handleDataIntegrityViolation()` (sətir 77-83): Postgres constraint xətasının daxili mesajı yalnız `log.warn`-a yazılır, client-ə generic `"The request conflicts with existing data"` gedir.
- `handleBadCredentials()` (sətir 85-89): "hansı sahə səhvdir" deyil, generic `"Invalid email or password"` — user enumeration-a qarşı düzgün.
- `ErrorResponse` record-u (`exception/ErrorResponse.java`) yalnız `timestamp/status/error/message/path/errors` saxlayır — `cause`, `stackTrace` və ya exception sinifi field-ları yoxdur.

Bu bölmə tam production-ready-dir, əlavə iş tələb olunmur.

### ✅ HAZIR — Auth/AccessDenied JSON forması eynidir və sızıntısızdır
`JwtAuthenticationEntryPoint.java` və `CustomAccessDeniedHandler.java` da eyni minimal JSON strukturunu (`timestamp/status/error/message/path`) qaytarır, `GlobalExceptionHandler`-in şərhi (sətir 26-28) bunu qəsdən izah edir. Uyğunluq var, əlavə iş lazım deyil.

---

## 5. Rate Limiting / Brute-force qorunması

### ❌ ÇATIŞMIR — Heç bir endpoint-də IP/istifadəçi əsaslı rate limit yoxdur
`pom.xml`-də `bucket4j`, `resilience4j`, `spring-cloud-gateway` və ya bənzər heç bir rate-limit kitabxanası yoxdur (bütün 22 asılılıq yoxlanıldı). `AuthController.java`-dakı `/register`, `/login`, `/forgot-password`, `/resend-verification` endpoint-lərinin heç birində throttle/interceptor yoxdur.

**Niyə problemdir:** `AuthService.login()` (`AuthService.java:96-111`) hər sorğuda `authenticationManager.authenticate()` çağırır — bu, sonsuz sayda şifrə təxmini cəhdinə imkan verir (OTP addımına çatmadan). `register()` və `resendVerification()` sonsuz spam email göndərilməsinə açıqdır.

**Düzəliş** — minimal IP-əsaslı limit üçün Bucket4j əlavə et:
```xml
<dependency>
    <groupId>com.bucket4j</groupId>
    <artifactId>bucket4j_jdk17-core</artifactId>
    <version>8.10.1</version>
</dependency>
```
və `/api/v1/auth/**` üçün `OncePerRequestFilter` (məs. `RateLimitingFilter`) yazıb `SecurityConfig.addFilterBefore(jwtAuthenticationFilter, ...)` sətirindən əvvəl zəncirə əlavə et — məsələn login/register/resend-verification üçün IP başına dəqiqədə 5 sorğu.

### ✅ HAZIR — OTP-nin cəhd limiti mövcuddur
`AuthProperties.java:23` (`otpMaxAttempts = 5`, `.env.example:33`-də `OTP_MAX_ATTEMPTS=5` kimi konfiqurasiya olunur) və `AuthService.verifyOtp()` (`AuthService.java:275-297`) hər səhv təxmində `session.attempts`-i artırır, limitə çatanda `session.setRevokedAt()` ilə kodu ləğv edir. Doğru tətbiq olunub.

### ✅ HAZIR — OTP-nin etibarlılıq müddəti təyin olunub
`AuthProperties.java:17,20` — email-verify 10 dəqiqə, login-otp 10 dəqiqə (`application.yml:86-87`). `verifyOtp()` (sətir 280-284) `expiresAt`-i keçmiş OTP-ni ayrıca rədd edir və sessiyanı ləğv edir. Doğru tətbiq olunub.

---

## 6. Email / OTP

### ✅ HAZIR — OTP kodu log-a yazılmır
`EmailService.send()` (`EmailService.java:27-46`) yalnız `log.info("Email sent successfully to {}", to)` yazır — email **body**-si (OTP kodunun özü) heç bir log sətrinə düşmür. `AuthService.sendVerificationOtp()`/`sendLoginOtp()` (sətir 226-243) OTP-ni yalnız e-poçt body-sinə qoyur, `log.*` çağırışına ötürmür. Bütün `src/main/java` daxilində `log.*(...)` çağırışları arasında `password|otp|token|secret` sözlərini ehtiva edən heç bir sətir tapılmadı (grep ilə yoxlanıldı).

### ✅ HAZIR — "From" ünvanı kodda düzgün konfiqurasiya olunub
`MailProperties.java` → `app.mail.from: ${MAIL_FROM:no-reply@nexora-academy.local}` (`application.yml:80`) — `EmailService.send()` sətir 31-də `mailProperties.getFrom()` istifadə edir, hardcode ünvan yoxdur.

### ⚠️ QİSMƏN HAZIR — Production SMTP-yə keçid üçün .env dəyərləri hazırkı struktur üzərində dəyişdirilməlidir
Hazırkı `.env`/`.env.example`: `MAIL_HOST=localhost`, `MAIL_PORT=1025`, `MAIL_SMTP_AUTH=false`, `MAIL_SMTP_STARTTLS=false`, boş `MAIL_USERNAME`/`MAIL_PASSWORD` — bunlar yalnız MailHog üçün işləyir, real SMTP provayderi (SendGrid/SES/Postmark s.) `auth=true` və TLS tələb edir. Kodda dəyişiklik lazım deyil, çünki `application.yml:47-57` bu dəyərləri artıq env-dən oxuyur — sadəcə prod `.env`-də real dəyərlər lazımdır:
```
MAIL_HOST=smtp.sendgrid.net
MAIL_PORT=587
MAIL_USERNAME=apikey
MAIL_PASSWORD=<SendGrid API açarı>
MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS=true
MAIL_FROM=no-reply@nexora-academy.com
```
Bunu "çatışmır" yox, "əməliyyat addımı" kimi qeyd edirəm, çünki kod strukturu artıq bunu dəstəkləyir.

---

## 7. Logging

### ✅ HAZIR — Prod-da root log səviyyəsi WARN-dır
`application-prod.yml:32-33` — `logging.level.root: WARN`. Dev-də isə `application-dev.yml:20-22` `org.hibernate.SQL: DEBUG` və `org.hibernate.orm.jdbc.bind: TRACE` aktivdir (yalnız `dev` profilində) — düzgün ayrım.

### ✅ HAZIR — Həssas data (şifrə/token/OTP) log-a yazılmır
Bax Bölmə 6. Əlavə olaraq `CrudLoggingAspect.java` (`logging/CrudLoggingAspect.java:101-112`) sözlə qeyd edir ki, request DTO-larının **field dəyərləri heç vaxt** loglanmır — yalnız DTO-nun sinif adı (`summarizeArg()`), UUID/String/Number/enum kimi "təhlükəsiz" tiplər istisnadır. Bu, `RegisterRequest`/`LoginRequest` kimi şifrə saxlayan DTO-ların `toString()`-inin log-a düşməsinin qarşısını düzgün şəkildə alır.

### ⚠️ QİSMƏN HAZIR — CRUD audit logger-ləri prod-da da INFO səviyyəsində qalır (qəsdəndir, amma sənədləşdirilməli)
`logback-spring.xml:97-108` — `AUDIT_CREATE/READ/UPDATE/DELETE` logger-ləri açıq şəkildə `level="INFO"` təyin edilib. Logback-də adlandırılmış logger-in açıq səviyyəsi `root`-dan miras almır, ona görə `application-prod.yml:32`-dəki `root: WARN` bunlara **təsir etmir** — hər CRUD çağırışı prod-da da fayla yazılmağa davam edəcək. Bu, dizayn baxımından düzgündür (audit trail məqsədilə), sadəcə "prod-da log səviyyəsi INFO deyil WARN-dır" tələbini hərfi mənada yoxlayanda qeyd olunmalıdır — root DEBUG/INFO axını WARN-a düşür, amma xüsusi audit axını qəsdən INFO saxlanılır. Əlavə iş tələb olunmur, sadəcə DevOps komandası bunun fərqinə varmalıdır ki, `logs/create|read|update|delete/` qovluqları prod-da da böyüyəcək (30 günlük saxlama artıq `LOG_RETENTION_HOURS=720` ilə tənzimlənib — `logback-spring.xml:15`).

---

## 8. Database

### ✅ HAZIR — HikariCP ayarları default deyil, açıq təyin olunub
`application.yml:17-20`:
```yaml
hikari:
  maximum-pool-size: 10
  minimum-idle: 2
  connection-timeout: 30000
```
`application-prod.yml` bunları override etmir (baza dəyərlər prod-da da tətbiq olunur). Kiçik/orta yük üçün məqbuldur.

### ⚠️ QİSMƏN HAZIR — `leak-detection-threshold` təyin olunmayıb
Hazırkı Hikari konfiqurasiyasında connection-leak aşkarlanması yoxdur. Kritik deyil, amma prod-a çıxmazdan əvvəl əlavə etmək tövsiyə olunur:
```yaml
spring:
  datasource:
    hikari:
      leak-detection-threshold: 30000
```

### ✅ HAZIR — Flyway migration-lar ardıcıl və düzgün nömrələnib
`src/main/resources/db/migration/` — `V1` – `V13`, boşluq/təkrar versiya nömrəsi yoxdur (`V1__init_extensions_and_schemas.sql` → `V13__add_login_otp_and_session_attempts.sql`). `out-of-order: false`, `validate-on-migrate: true`, `baseline-on-migrate: true` (`application.yml:40-43`) checksum uyğunsuzluğunun sakitcə keçilməsinin qarşısını alır. Migration adları modul-sxemlərlə (`identity`, `catalog`, `academics`...) uyğundur.

**Diqqət (pom.xml şərhi, sətir 93-98):** `flyway-core`/`flyway-database-postgresql` təkbaşına Spring Boot 4.x-də migration-ları avtomatik işə salmır — ayrıca `spring-boot-starter-flyway` asılılığı tələb olunur. Bu asılılıq artıq `pom.xml:99-102`-də mövcuddur, ona görə bu bənd `✅ HAZIR`, sadəcə gələcəkdə bu asılılığı təsadüfən silməmək üçün qeyd edirəm.

---

## 9. Docker / Deploy

### ❌ ÇATIŞMIR — Spring Boot tətbiqi üçün Dockerfile mövcud deyil
Repo kökündə (və heç bir alt-qovluqda) `Dockerfile` tapılmadı. `docker-compose.yml` yalnız `postgres`, `flyway` (bir dəfəlik migration runner) və `mailhog` xidmətlərini ehtiva edir — tətbiqin özünü build/run edən heç bir servis yoxdur.

**Tövsiyə olunan multi-stage Dockerfile** (`Dockerfile`, repo kökündə):
```dockerfile
# --- Build stage ---
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -B
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# --- Runtime stage ---
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
WORKDIR /app
COPY --from=build /app/target/NexoraAcademy-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8185
ENTRYPOINT ["java", "-jar", "app.jar"]
```
və `docker-compose.yml`-a əlavə et:
```yaml
  app:
    build: .
    container_name: nexora-app
    restart: unless-stopped
    env_file: .env
    environment:
      SPRING_PROFILES_ACTIVE: prod
    ports:
      - "8185:8185"
    depends_on:
      flyway:
        condition: service_completed_successfully
```

### ❌ ÇATIŞMIR — docker-compose.yml-də prod üçün ayrıca `.env` strukturu yoxdur (app servisi olmadığı üçün)
Hazırkı `docker-compose.yml` yalnız Postgres/Flyway/MailHog-un `.env`-dən oxuduğu dəyişənləri istifadə edir (`${DB_NAME:-...}` s.). Yuxarıdakı `app` servisi əlavə olunduqdan sonra, prod üçün ayrıca `docker-compose.prod.yml` (MailHog-suz, `SPRING_PROFILES_ACTIVE=prod` ilə) hazırlanması tövsiyə olunur, çünki MailHog production-da istifadə edilməməlidir (bax Bölmə 6).

### ✅ HAZIR — Health check endpoint aktivdir və düzgün açıqdır
`pom.xml:44-47` — `spring-boot-starter-actuator` mövcuddur (şərh qeyd edir ki, əvvəllər `SecurityConfig` bunu `permitAll` edib, amma asılılıq olmadığı üçün faktiki 404 verirdi — indi asılılıq var). `SecurityConfig.java:81-83` `/actuator/health`-i `permitAll()` edir. `application.yml:66-69` `management.health.mail.enabled: false` ilə SMTP olmayanda health-in "DOWN" görünməsinin qarşısını alır. `management.endpoints.web.exposure.include` heç yerdə genişləndirilməyib, yəni Spring Boot default-una görə yalnız `health` (və `info`) web üzərindən açıqdır — `/actuator/env`, `/actuator/beans` kimi həssas endpoint-lər ifşa olunmayıb. Bu bölmə tam hazırdır.

---

## 10. Secrets İdarəetməsi

### ✅ HAZIR — `.env` `.gitignore`-dadır və heç vaxt commit olunmayıb
`.gitignore:37-39`:
```
.env
.env.*
!.env.example
```
`git log --all -- .env` **boş nəticə** qaytardı — `.env` faylı git tarixçəsində heç vaxt olmayıb. Bu bölmənin mexanizmi düzgündür.

### ❌ ÇATIŞMIR (KRİTİK, Bölmə 2 ilə eyni tapıntı) — `.env.example`-də real görünüşlü secret-lar commit olunub və hazırkı `.env` bunları hərfi-hərfinə təkrarlayır
Bax Bölmə 2-dəki tam izahat. Bu, "git history-də təsadüfən commit olunmuş şifrə" sualının cavabıdır: birbaşa `.env` heç vaxt commit olunmayıb, amma funksional olaraq eyni nəticəyə gəlinib — çünki nümunə faylındakı "placeholder" əslində işlək bir sirdir və developer onu olduğu kimi köçürüb istifadə edib.

---

## Prioritet Sıralı Checklist (2026-07-22 yeniləndi)

### 🔴 KRİTİK (deploy-dan əvvəl mütləq)
| # | Maddə | Status | Fayl / hara | Təxmini vaxt |
|---|-------|--------|------|---------------|
| 1 | JWT_SECRET-i yenidən generasiya et (`openssl rand -base64 48`) | ⬜ Sən edəcəksən | `.env` → `JWT_SECRET=` | ~10 dəqiqə |
| 2 | DB_PASSWORD-u real, unikal şifrə ilə dəyiş | ✅ Edildi (`nexoracademy-1234`, doğrulandı) | `.env` | — |
| 3 | `.env.example`-dəki köhnə `JWT_SECRET`/`DB_PASSWORD` nümunə dəyərlərini placeholder-ə çevir | ⬜ Sən edəcəksən (JWT_SECRET-i dəyişəndə eyni anda et) | `.env.example` | ~5 dəqiqə |
| 4 | `app.cors.allowed-origins` prod domenlərinə təyin et | ⬜ Sən edəcəksən (dəyər lazımdır) | `.env` → `CORS_ALLOWED_ORIGINS=` (hazırda `http://localhost:3000` — real domenlə əvəz et, məs. `https://app.nexora-academy.com`) | ~5 dəqiqə |
| 5 | Real production SMTP provayderinə keç (MailHog-u əvəz et) | ⬜ Sən edəcəksən (dəyərlər lazımdır) | `.env` → `MAIL_HOST`/`MAIL_PORT`/`MAIL_USERNAME`/`MAIL_PASSWORD`/`MAIL_SMTP_AUTH=true`/`MAIL_SMTP_STARTTLS=true`/`MAIL_FROM` | ~30 dəqiqə |
| 6 | `/api/v1/auth/**` üçün rate limiting | ✅ Edildi (`AuthRateLimitingFilter`) | — | — |
| 7 | Spring Boot tətbiqi üçün Dockerfile + `docker-compose.yml`-a `app` servisi | ✅ Edildi (test olundu, `/actuator/health` → UP) | `Dockerfile`, `docker-compose.yml` | — |

### 🟠 VACİB
| # | Maddə | Status | Fayl / hara | Təxmini vaxt |
|---|-------|--------|------|---------------|
| 8 | Swagger UI / OpenAPI docs-u prod-da söndür | ⬜ Sən edəcəksən | `application-prod.yml`-a əlavə et: `springdoc.api-docs.enabled=false`, `springdoc.swagger-ui.enabled=false` | ~15 dəqiqə |
| 9 | `TestMailController`-i sil | ✅ Edildi (fayl silindi) | — | — |
| 10 | Real ödəniş gateway-i qoşulmazdan əvvəl `/payments/callback`-ə imza doğrulaması | ✅ Mexanizm yazıldı, ⬜ real dəyər sən əlavə edəcəksən | `.env` → `PAYMENT_GATEWAY_WEBHOOK_SECRET=` (gateway-in webhook signing secret-i), lazım olsa `PAYMENT_GATEWAY_SIGNATURE_HEADER=` (default `X-Gateway-Signature`, gateway fərqli header adı istifadə edirsə dəyiş) | Gateway seçimindən asılı |
| 11 | Hikari `leak-detection-threshold` təyin et | ✅ Edildi (`30000` ms) | `application.yml` | — |
| 12 | Prod üçün ayrıca `docker-compose.prod.yml` (MailHog-suz, `prod` profilli) hazırla | ⬜ Sən edəcəksən (hazırkı fayl dev-yönümlüdür) | yeni fayl | ~30 dəqiqə |
| 13 | `.env.example`-in git tracking-dən çıxarılması commit/push edilsin | ⬜ Sən edəcəksən (kodlar aşağıda) | — | ~5 dəqiqə |

### 🟢 TÖVSİYƏ OLUNUR
| # | Maddə | Fayl | Təxmini vaxt |
|---|-------|------|---------------|
| 14 | Struktur/JSON logging və ya correlation-id əlavə et (mərkəzləşdirilmiş log toplama üçün) | `logback-spring.xml` | ~1-2 saat |
| 15 | URL-pattern təhlükəsizliyinə əlavə olaraq `@PreAuthorize` metod-səviyyəli qorunma | müxtəlif service-lər | ~2 saat |
| 16 | Login/register/OTP rate-limit üçün inteqrasiya testi yaz | `src/test/.../integration` | ~1 saat |
| 17 | k8s/orkestrasiya üçün ayrıca readiness/liveness prob strategiyası (əgər tətbiq olunacaqsa) | `application-prod.yml` | ~30 dəqiqə |
| 18 | GitHub tarixçəsindən köhnə `.env.example` commit-lərini təmizləmək (BFG/`git filter-repo` + force-push) — JWT_SECRET rotasiya edildikdən sonra prioritet aşağıdır | — | ~30-60 dəqiqə |

---

## "Deploy Etməzdən Əvvəl Mütləq Et" — Checklist

- [x] ~~DB_PASSWORD-u real, unikal şifrə ilə dəyiş~~ — edildi, doğrulandı (`docker compose down -v` → `.env` → `up -d`, `/actuator/health` → UP)
- [x] ~~`/api/v1/auth/**` üçün rate limiting~~ — `AuthRateLimitingFilter` ilə edildi
- [x] ~~`TestMailController`-i sil~~ — edildi
- [x] ~~Production Dockerfile yaz və test et~~ — edildi (permission bug tapılıb düzəldildi)
- [x] ~~`docker-compose.yml`-a `app` servisi əlavə et~~ — edildi
- [x] ~~Hikari `leak-detection-threshold` əlavə et~~ — edildi (`30000` ms)
- [x] ~~Real ödəniş gateway-i üçün imza doğrulama mexanizmi~~ — kod hazır, dəyər lazımdır (aşağı bax)
- [ ] JWT_SECRET-i `openssl rand -base64 48` ilə yenidən generasiya et, `.env`-ə yaz *(~10 dəqiqə)*
- [ ] `.env.example`-dəki `JWT_SECRET`/`DB_PASSWORD` nümunə dəyərlərini placeholder mətninə çevir (məs. `REPLACE_ME`) *(~5 dəqiqə)*
- [ ] `.env`-də `CORS_ALLOWED_ORIGINS`-i real prod frontend domenləri ilə doldur *(~5 dəqiqə)*
- [ ] Real SMTP provayderi (SendGrid/SES/Postmark) məlumatlarını `.env`-ə yaz, `MAIL_SMTP_AUTH=true`, `MAIL_SMTP_STARTTLS=true` təyin et *(~30 dəqiqə)*
- [ ] Real ödəniş gateway-i seçildikdən sonra `.env`-də `PAYMENT_GATEWAY_WEBHOOK_SECRET` (və lazım olsa `PAYMENT_GATEWAY_SIGNATURE_HEADER`) doldur *(gateway-dən asılı)*
- [ ] `springdoc.api-docs.enabled=false` və `springdoc.swagger-ui.enabled=false`-u `application-prod.yml`-a əlavə et *(~15 dəqiqə)*
- [ ] Prod üçün ayrıca `docker-compose.prod.yml` (MailHog-suz) hazırla *(~30 dəqiqə)*
- [ ] `.env.example`-in git tracking-dən çıxarılmasını commit/push et:
  ```bash
  git add .gitignore Dockerfile .dockerignore PRODUCTION_READINESS.md docker-compose.yml \
    src/main/resources/application.yml src/main/resources/application-prod.yml \
    src/main/java/az/demo/NexoraAcademy/config/SecurityConfig.java \
    src/main/java/az/demo/NexoraAcademy/config/PaymentGatewayProperties.java \
    src/main/java/az/demo/NexoraAcademy/security/AuthRateLimitingFilter.java \
    src/main/java/az/demo/NexoraAcademy/security/PaymentCallbackSignatureFilter.java \
    src/main/java/az/demo/NexoraAcademy/security/PaymentGatewaySignatureVerifier.java \
    src/main/java/az/demo/NexoraAcademy/security/CachedBodyHttpServletRequest.java \
    src/main/java/az/demo/NexoraAcademy/controller/billing/PaymentController.java

  git commit -m "Add production Dockerfile, CORS/rate-limit/payment-signature config; stop tracking .env.example"
  git push
  ```
  *(~5 dəqiqə)*
- [ ] (İstəyə bağlı, aşağı prioritet) GitHub tarixçəsindən köhnə `.env.example` commit-lərini BFG/`git filter-repo` ilə təmizlə *(~30-60 dəqiqə)*
- [ ] Prod deploy-dan sonra `/actuator/health`-in real DB/Mail vəziyyətini düzgün göstərdiyini yoxla *(~10 dəqiqə)*
