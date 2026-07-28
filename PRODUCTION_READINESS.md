# NexoraAcademy — Production Readiness Audit

**Tarix:** 2026-07-22 · **Son yenilənmə:** 2026-07-24
**Əhatə dairəsi:** Spring Boot 4.1 / PostgreSQL / Flyway backend (`az.demo.NexoraAcademy`)
**Metod:** Bütün `application*.yml`, `.env`/`.env.example`, `pom.xml`, `docker-compose*.yml`, `CorsConfig`, `SecurityConfig`, `GlobalExceptionHandler`, `JwtService`/`JwtProperties`, `AuthService`/`AuthController`, `EmailService`, bütün controller sinifləri, `logback-spring.xml`, `CrudLoggingAspect`, git tarixçəsi bilavasitə oxunub.

> Bu sənəd iki hissədən ibarətdir: əvvəlcə **qalan işlər** (yalnız sən edə bilərsən), sonra **ətraflı texniki audit** (Bölmə 1-10, hər maddənin fayl/sətir referansı ilə), ən altda isə **tamamlanmış işlərin tarixçəsi**. Qarışıqlıq yaranmasın deyə, aşağıdakı "Qalan İşlər" siyahısı yeganə "nə qalıb" mənbəyidir — Bölmə 1-10-dakı ✅ işarələr artıq kod səviyyəsində tamamlanmış deməkdir, təkrar iş tələb etmir.

---

## ⬜ Qalan İşlər — Yalnız Sən Edə Bilərsən

Bunların hamısı ya **real sirr/kimlik məlumatı** (mənim bilmədiyim/bilə bilmədiyim dəyərlər), ya **biznes qərarı**, ya da **geri dönməz/paylaşılan əməliyyatdır**. Kod tərəfi bunların hamısı üçün artıq hazırdır — yalnız dəyəri doldurmaq/qərar vermək qalır.

### 🔴 Kritik (deploy-dan əvvəl mütləq)
| # | Maddə | Hara | Niyə mən edə bilmirəm | Təxmini vaxt |
|---|-------|------|------------------------|---------------|
| 1 | ~~`JWT_SECRET`-i yenidən generasiya et~~ **✅ 2026-07-28: rotasiya edildi** — 48 baytlıq təsadüfi dəyər (base64url, 64 simvol) `.env`-ə yazıldı. Köhnə (git tarixçəsinə sızmış) dəyər artıq istifadə olunmur. Qeyd: rotasiya bütün mövcud token-ləri etibarsız edir | `.env` → `JWT_SECRET=` | — | tamamlandı |
| 2 | ~~`CORS_ALLOWED_ORIGINS`-i real domenlə doldur~~ **✅ 2026-07-28: lokal/müvəqqəti prod üçün dolduruldu** — Live Server/Vite/CRA portları (5500/5501/5173/3000 × localhost/127.0.0.1). **Qalan:** frontend real domenə deploy olunanda həmin domen əlavə edilməlidir | `.env` → `CORS_ALLOWED_ORIGINS=` | Real prod domeni hələ yoxdur | qismən |
| 3 | ~~Real production SMTP provayderinə keç~~ **✅ 2026-07-25: Gmail SMTP qoşuldu** (`smtp.gmail.com:587`, App Password, STARTTLS). Yalnız gələcək qeyd: pulsuz Gmail gündə ~500 məktubla məhduddur — real yüklə SES/SendGrid/Postmark-a keçmək lazım gələcək | `.env` → `MAIL_*` | — | tamamlandı |
| 4 | Real ödəniş gateway-i seçildikdən sonra webhook secret-i doldur | `.env` → `PAYMENT_GATEWAY_WEBHOOK_SECRET=` (hazırda **boşdur** → callback imzasız qəbul olunur) | Gateway seçimi biznes qərarıdır, secret həmin gateway-in paneldindən gəlir | Gateway seçimindən asılı |
| 9 | ~~`FRONTEND_BASE_URL`-i real domenlə əvəz et~~ **✅ 2026-07-28: müvəqqəti prod üçün `http://localhost:5500`** (frontend dostun öz maşınında Live Server ilə işləyir). **Qalan:** frontend deploy olunanda real domenlə əvəz et | `.env` → `FRONTEND_BASE_URL=` | Real prod domeni hələ yoxdur | qismən |
| 10 | ~~Prod-da admin seed qərarı~~ **✅ 2026-07-28: `ADMIN_SEED_ENABLED=true` + 4 güclü təsadüfi şifrə (24 simvol)** `.env`-ə yazıldı və DB-dəki mövcud 4 hesabın `password_hash`-ı da yeniləndi (seeder mövcud hesaba toxunmadığı üçün ayrıca UPDATE lazım idi). Köhnə `admin1234` tipli şifrələr yoxlanılıb — **401** verir | `.env` → `ADMIN_SEED_*` | — | tamamlandı |

### 🟠 Vacib / İstəyə bağlı
| # | Maddə | Hara | Niyə mən edə bilmirəm | Təxmini vaxt |
|---|-------|------|------------------------|---------------|
| 5 | GitHub tarixçəsindən köhnə `.env.example` commit-lərini (`e466ec7`, `a52c18f`, `c8cfc98`) BFG/`git filter-repo` ilə təmizlə, force-push et | — | Tarixçəni yenidən yazan, komanda ilə koordinasiya və force-push tələb edən geri dönməz əməliyyatdır. JWT_SECRET rotasiya edildikdən sonra prioritet aşağıdır | ~30-60 dəqiqə |
| 6 | (İstəyə bağlı) `@PreAuthorize` metod-səviyyəli qorunma əlavə et | müxtəlif service-lər | Funksional boşluq deyil (URL-pattern qaydaları artıq hər şeyi əhatə edir, bax Bölmə 2) — hansı metodun hansı rola bağlanacağı məhsul qərarıdır | ~2 saat |
| 7 | Login/register/OTP rate-limit üçün inteqrasiya testi yaz və canlı DB-yə qarşı doğrula | `src/test/.../integration` | Bu sessiyada canlı Postgres/Testcontainers mühiti yox idi — sınanmamış test commit etmək istəmədim | ~1 saat |
| 8 | Prod deploy-dan sonra `/actuator/health`-in real DB/Mail vəziyyətini düzgün göstərdiyini yoxla | — | Canlı deploy mühitinə çıxışım yoxdur | ~10 dəqiqə |

---

## Ətraflı Texniki Audit

### 1. Environment / Config

**✅ HAZIR — `ddl-auto=validate`, heç bir `update`/`create` yoxdur.** `application.yml:26`, `application-prod.yml:19` — schema idarəçiliyi tam Flyway-ə həvalə olunub.

**✅ HAZIR — `show-sql` yalnız dev profilində aktivdir.** `application-dev.yml:12`.

**✅ HAZIR — Flyway `validate-on-migrate=true`, `out-of-order=false`.** `application.yml:42-43`.

**✅ HAZIR — Prod profili üçün default dəyər yoxdur (fail-fast).** `application-prod.yml:12-14` — `DB_*` dəyişənləri prod-da təyin olunmasa açılış zamanı xəta verib dayanır.

**✅ HAZIR — `app.cors.allowed-origins` `CORS_ALLOWED_ORIGINS` env-dəyişəninə bağlıdır.** `application.yml:83` (dev fallback: `http://localhost:3000`), `application-prod.yml:34-35` (placeholder-siz — dəyər verilməzsə app açılmır). `CorsProperties`/`CorsConfig` bunu birbaşa `setAllowedOrigins()`-ə ötürür. Kod tərəfi tamamdır — qalan yalnız **real dəyər** (bax "Qalan İşlər" #2).

---

### 2. Security

**✅ HAZIR — JWT secret prod-da hardcode deyil, default-suzdur.** `application-prod.yml:29` — `app.jwt.secret: ${JWT_SECRET}` (placeholder-siz). `JwtProperties.java:15`-dəki dev fallback yalnız `application.yml`-da aktivdir.

**✅ HAZIR (kod) / ⬜ real dəyər qalır — `.env.example`-dəki nümunə `JWT_SECRET`/`DB_PASSWORD` artıq işlək sirr deyil.** Əvvəllər `.env.example`-dəki `JWT_SECRET`/`DB_PASSWORD` hazırkı `.env` ilə hərfi-hərfinə eyni idi (developer nümunəni köçürüb heç vaxt əvəz etməmişdi) — bu, repo-nu klonlayan hər kəsə etibarlı JWT token saxtalaşdırmaq imkanı verirdi (bax `JwtService.signingKey()`). Bu, 2026-07-24-də düzəldildi: `.env.example`-dəki hər iki dəyər açıq placeholder mətninə çevrildi (`REPLACE_WITH_OUTPUT_OF__openssl_rand_base64_48`, `REPLACE_ME_STRONG_UNIQUE_PASSWORD`). `DB_PASSWORD` real Postgres instansında artıq rotasiya edilib və doğrulanıb (2026-07-22). **Qalan:** `JWT_SECRET`-in real dəyəri hələ köhnə (sızmış) dəyərdir — bax "Qalan İşlər" #1, bu, yalnız sənin edə biləcəyin son addımdır.

**✅ HAZIR — Şifrələr BCrypt ilə hash-lənir.** `SecurityConfig.java:162-164`, `AuthService.register()`.

**✅ HAZIR — Admin/həssas endpoint-lər `SecurityConfig`-də rol əsaslı qorunur.** `SecurityConfig.java:93-147` — `anyRequest().authenticated()` qalanları default bağlayır, struktur məntiqli, sızma yoxdur.

**✅ HAZIR — `PaymentController.callback()` üçün imza doğrulama mexanizmi mövcuddur.** `PaymentCallbackSignatureFilter` (bax `security/PaymentCallbackSignatureFilter.java`) `PaymentGatewaySignatureVerifier` vasitəsilə HMAC-SHA256 imzanı controller-ə çatmazdan **əvvəl** yoxlayır (controller metodunun özündə deyil — filter-based dizayn). `PAYMENT_GATEWAY_WEBHOOK_SECRET` boş olduğu müddətcə yoxlama bypass olunur (hər callback-də WARN logu ilə), dəyər veriləndə məcburiləşir. **Qalan:** real gateway seçilib webhook secret-i doldurulmayınca bu endpoint funksional olaraq açıqdır — bax "Qalan İşlər" #4.

**✅ HAZIR (2026-07-25) — Default admin hesablarının şifrələri artıq hardcode deyil.** `AdminSeeder` əvvəllər hər profildə, o cümlədən prod-da, dörd admin hesabını koda yazılmış zəif şifrələrlə (`admin1234`, `system-admin1234` və s.) yaradırdı — kodu görən hər kəs canlı sistemə SYSTEM_ADMIN kimi girə bilərdi. İndi dəyərlər `AdminSeederProperties` (`app.admin-seed.*`) üzərindən gəlir: `application.yml`-da yalnız dev default-ları, `application-prod.yml`-da isə `enabled: ${ADMIN_SEED_ENABLED:false}` + boş şifrə default-ları var. Seed prod-da açılarsa və şifrə verilməyibsə tətbiq açılışda `IllegalStateException` ilə dayanır (fail-fast); 12 simvoldan qısa şifrə WARN loglayır. **Qalan:** prod üçün qərar/şifrələr — bax "Qalan İşlər" #10.

**✅ HAZIR — Dev-only test controller silinib.** `TestMailController` (`/test/mail`) 2026-07-22-də tam silindi, kod bazasında qalıq yoxdur.

**✅ HAZIR — Swagger UI / OpenAPI docs prod-da söndürülüb.** `SecurityConfig.java:75-78` `/swagger-ui/**`/`/v3/api-docs/**`-i profil fərqi qoymadan `permitAll()` edir (dev üçün doğrudur), amma `application-prod.yml`-a 2026-07-24-də `springdoc.api-docs.enabled=false` və `springdoc.swagger-ui.enabled=false` əlavə olundu — springdoc auto-config prod-da bu endpoint-ləri özü qeydiyyatdan çıxarır.

---

### 3. CORS konfigurasiyası

`CorsConfig.java` tam oxundu:
- **✅ HAZIR** — `allowedOrigins` hardcode `localhost`/`"*"` deyil, `CorsProperties` üzərindən xarici konfiqurasiyaya bağlıdır.
- **✅ HAZIR** — `allowCredentials(true)` wildcard origin ilə birgə istifadə olunmayıb.
- **✅ HAZIR (kod) / ⬜ real dəyər qalır** — bax Bölmə 1: mexanizm tamamdır, `.env`-də hələ dev dəyəri (`http://localhost:3000`) var — real domenlə əvəz olunmalıdır (bax "Qalan İşlər" #2).

---

### 4. Exception Handling

`GlobalExceptionHandler.java` (112 sətir) tam oxundu — **bu bölmə tam production-ready-dir, əlavə iş tələb olunmur.**

- Stack trace/daxili sinif adları response-da sızmır (`handleUnexpected`, `handleDataIntegrityViolation`).
- `handleBadCredentials()` generic mesaj qaytarır — user enumeration-a qarşı düzgün.
- `JwtAuthenticationEntryPoint`/`CustomAccessDeniedHandler` eyni minimal JSON formasını istifadə edir.

---

### 5. Rate Limiting / Brute-force qorunması

**✅ HAZIR — `/api/v1/auth/**` üçün IP-əsaslı rate limiting mövcuddur.** `AuthRateLimitingFilter` (in-memory, per-IP, fixed-window) `/register`, `/login`, `/login/verify-otp`, `/forgot-password`, `/resend-verification` üçün dəqiqəlik limit tətbiq edir, `SecurityConfig`-də JWT filter-dən əvvəl zəncirə əlavə olunub. Tək instans üçün kifayətdir; horizontal scale olunarsa paylaşılan store (Redis) ilə əvəz edilməlidir (kodda qeyd olunub).

**✅ HAZIR — OTP-nin cəhd limiti mövcuddur.** `AuthProperties.java:23` (`otpMaxAttempts = 5`), `AuthService.verifyOtp()` limitə çatanda sessiyanı ləğv edir.

**✅ HAZIR — OTP-nin etibarlılıq müddəti təyin olunub.** `AuthProperties.java:17,20` — 10 dəqiqə, `verifyOtp()` keçmiş OTP-ni rədd edir.

---

### 6. Email / OTP

**✅ HAZIR — OTP kodu log-a yazılmır.** `EmailService.send()` yalnız göndərmə statusunu loglayır, body-ni yox.

**✅ HAZIR — "From" ünvanı kodda düzgün konfiqurasiya olunub.** `MailProperties`/`app.mail.from`, hardcode yoxdur.

**✅ HAZIR (2026-07-25) — Production SMTP qoşulub (Gmail).** `.env` → `smtp.gmail.com:587`, `MAIL_SMTP_AUTH=true`, `MAIL_SMTP_STARTTLS=true`, `MAIL_PASSWORD` = Google App Password, `MAIL_FROM` = autentifikasiya olunan hesab.

**✅ HAZIR (2026-07-25) — SMTP timeout-ları real provayder üçün uyğunlaşdırıldı.** `application.yml`-dəki `connectiontimeout/timeout/writetimeout` MailHog üçün 3000 ms idi; Gmail-in STARTTLS handshake-i bunu tez-tez keçir və `EmailService` xətanı uddugu üçün məktub səssizcə itirdi. İndi default 10000 ms (`MAIL_CONNECTION_TIMEOUT_MS`/`MAIL_READ_TIMEOUT_MS`/`MAIL_WRITE_TIMEOUT_MS` ilə tənzimlənir).

**✅ HAZIR (2026-07-25) — Email göndərmə artıq sorğunu bloklamır.** `EmailService.send()` `@Async`-dir (`config/AsyncConfig`, `@EnableAsync`) — əvvəllər Gmail-in 1-3 saniyəlik cavabı birbaşa `register`/`login`/`forgot-password` sorğusunun müddətinə əlavə olunurdu. `TaskDecorator` MDC-ni (correlation-id) arxa-fon thread-inə köçürür ki, async loglar da korrelyasiya olunsun.

**⚠️ Bilinən məhdudiyyət — Gmail kvotası.** Pulsuz Gmail hesabı gündə ~500 məktub göndərə bilir və `From` autentifikasiya olunan ünvandan fərqli ola bilmir. Test/erkən istifadə üçün kifayətdir; real yükdə SES/SendGrid/Postmark-a keçid lazım olacaq (yalnız `.env` dəyişikliyi — kodda dəyişiklik tələb olunmur).

---

### 7. Logging

**✅ HAZIR — Prod-da root log səviyyəsi WARN-dır.** `application-prod.yml:32-33`.

**✅ HAZIR — Həssas data (şifrə/token/OTP) log-a yazılmır.** `CrudLoggingAspect.java` DTO field dəyərlərini heç vaxt loglamır, yalnız sinif adı/UUID/enum kimi "təhlükəsiz" tipləri.

**⚠️ Qəsdəndir, əlavə iş lazım deyil — CRUD audit logger-ləri prod-da da INFO səviyyəsində qalır.** `logback-spring.xml:97-108` — audit trail məqsədilə qəsdən belədir, `root: WARN` bunlara təsir etmir. DevOps komandası `logs/create|read|update|delete/` qovluqlarının prod-da da böyüyəcəyinin fərqinə varmalıdır (30 günlük saxlama `LOG_RETENTION_HOURS=720` ilə tənzimlənib).

**✅ HAZIR — Correlation-id ilə mərkəzləşdirilmiş log korrelyasiyası.** `logging/CorrelationIdFilter.java` + `config/WebFilterConfig.java` (2026-07-24) — hər HTTP request-ə MDC-based correlation-id təyin edilir (`X-Correlation-Id` — client göndərsə saxlanılır, göndərməsə UUID yaradılır), response header-ə qaytarılır, `HIGHEST_PRECEDENCE`-də Spring Security-dən əvvəl işə düşür (rədd olunan 401/429 sorğular da loglanır). `logback-spring.xml`/`application.yml` bu id-ni fayl və konsol log-larında göstərir.

---

### 8. Database

**✅ HAZIR — HikariCP ayarları default deyil, açıq təyin olunub.** `application.yml:17-20` — `maximum-pool-size: 10`, `minimum-idle: 2`, `connection-timeout: 30000`.

**✅ HAZIR — `leak-detection-threshold` təyin olunub.** `application.yml:24` — `30000` ms (2026-07-22 əlavə olundu).

**✅ HAZIR — Flyway migration-lar ardıcıl və düzgün nömrələnib.** `V1`–`V13`, `out-of-order: false`, `validate-on-migrate: true`, `baseline-on-migrate: true`. `spring-boot-starter-flyway` asılılığı `pom.xml`-də mövcuddur (Spring Boot 4.x-də migration-ları avtomatik işə salmaq üçün tələb olunur).

---

### 9. Docker / Deploy

**✅ HAZIR — Production Dockerfile mövcuddur və test olunub.** Repo kökündəki `Dockerfile` — multi-stage build (`eclipse-temurin:21-jdk-alpine` → `-jre-alpine`), non-root `spring` istifadəçisi, `/app/logs` icazəsi əvvəlcədən düzəldilib, `HEALTHCHECK` `/actuator/health`-ə qarşı. Real `docker compose build/up` ilə test olunub — `/actuator/health` → `UP`.

**✅ HAZIR — `docker-compose.prod.yml` mövcuddur (2026-07-24).** MailHog **yoxdur**, `app` servisi `SPRING_PROFILES_ACTIVE=prod` ilə aktivdir, Postgres portu host-a çıxarılmır. İşə salmaq: `docker compose -f docker-compose.prod.yml up -d --build`. Əsas `docker-compose.yml` dəyişməz qalıb (dev/local, MailHog ilə; `app` servisi orada şərh xanasında saxlanılıb ki, dev üçün compose-suz işə salına bilsin).

**✅ HAZIR — Health check endpoint aktivdir və düzgün açıqdır.** `spring-boot-starter-actuator`, `/actuator/health` `permitAll()`, `management.health.mail.enabled: false` (SMTP olmayanda DOWN göstərməsin deyə), `management.endpoints.web.exposure.include` genişləndirilməyib (yalnız `health`/`info` açıqdır — `/actuator/env`, `/actuator/beans` ifşa olunmayıb).

**✅ HAZIR (2026-07-25) — Reverse proxy arxasında düzgün sxem/host.** `application-prod.yml` → `server.forward-headers-strategy: framework`. Bu ayar olmadan nginx/ingress arxasında Spring bağlantını `http://<container-ip>:8081` kimi görürdü: redirect-lər və generasiya olunan mütləq URL-lər `http://` olurdu.

**✅ HAZIR — k8s liveness/readiness probe dəstəyi (2026-07-24).** `application-prod.yml` → `management.endpoint.health.probes.enabled=true` + `livenessstate`/`readinessstate` — `/actuator/health/liveness` və `/actuator/health/readiness` ayrıca sorğulana bilər (mövcud `permitAll()` qaydası bunları da əhatə edir).

---

### 10. Secrets İdarəetməsi

**✅ HAZIR — `.env` `.gitignore`-dadır və heç vaxt commit olunmayıb.** `.gitignore:37-39`, `git log --all -- .env` boş nəticə.

**✅ HAZIR (kod/repo) / ⬜ tarixçə qalır — `.env.example` artıq işlək sirr saxlamır, git tracking-dən çıxarılıb.** Əvvəllər `.env.example`-də real görünüşlü secret-lar committed idi (`e466ec7`, `a52c18f`, `c8cfc98`) və hazırkı `.env` bunları hərfi-hərfinə təkrarlayırdı. Hər ikisi düzəldildi: dəyərlər placeholder-ə çevrildi (2026-07-24), fayl `git ls-tree HEAD`-də artıq görünmür (tracking-dən çıxarılıb, əvvəlki commit-də tamamlanıb). **Qalan:** köhnə sızmış dəyərlər hələ GitHub-un köhnə commit tarixçəsində qalır — bax "Qalan İşlər" #5 (BFG/`git filter-repo`, aşağı prioritet, JWT_SECRET rotasiya edildikdən sonra).

---

## ✅ Edilmiş İşlərin Tam Siyahısı (Dəyişiklik Tarixçəsi)

### 2026-07-22
- Production Dockerfile (+ `.dockerignore`) yaradıldı, non-root user permission bug-ı (log qovluğu yazma icazəsi) tapılıb düzəldildi, real `docker compose build/up` ilə test olundu — `/actuator/health` → `UP`.
- `docker-compose.yml`-a `app` servisi əlavə olundu (dev üçün, MailHog ilə).
- `.gitignore` korlanmış vəziyyətdən təmizləndi; `.env` və `.env.example` hər ikisi ignore olundu; `.env.example` git tracking-dən çıxarıldı.
- `app.cors.allowed-origins` `CORS_ALLOWED_ORIGINS` env-dəyişəninə bağlandı (dev-də localhost default-u, prod-da default yoxdur — fail-fast).
- `/api/v1/auth/register|login|login/verify-otp|forgot-password|resend-verification` üçün IP-əsaslı rate limiting əlavə olundu (`AuthRateLimitingFilter`).
- `TestMailController` silindi.
- Hikari `leak-detection-threshold: 30000` əlavə olundu.
- Ödəniş gateway-i üçün HMAC-SHA256 imza doğrulama mexanizmi yazıldı (`PaymentGatewayProperties`/`PaymentGatewaySignatureVerifier`/`PaymentCallbackSignatureFilter`).
- `DB_PASSWORD` real Postgres instansında rotasiya edildi və doğrulandı.

### 2026-07-24
- `.env.example`-dəki `JWT_SECRET`/`DB_PASSWORD` nümunə dəyərləri (əvvəlki sızmış dəyərlərlə eyni idi) açıq placeholder mətninə çevrildi.
- `application-prod.yml`-a `springdoc.api-docs.enabled=false`/`springdoc.swagger-ui.enabled=false` əlavə olundu.
- `application-prod.yml`-a k8s liveness/readiness probe dəstəyi əlavə olundu (`management.endpoint.health.probes`, `livenessstate`/`readinessstate`).
- `docker-compose.prod.yml` yaradıldı (MailHog-suz, `app` servisi aktiv, `SPRING_PROFILES_ACTIVE=prod`).
- Correlation-id filter (`logging/CorrelationIdFilter.java` + `config/WebFilterConfig.java`) əlavə olundu, `logback-spring.xml`/`application.yml` log pattern-ləri buna uyğun yeniləndi.
- Bütün dəyişikliklər `./mvnw compile` ilə yoxlanıldı — xəta yoxdur (canlı Postgres olmadığı üçün tam inteqrasiya testi bu mühitdə işə salınmadı).
- Sənəd yenidən strukturlaşdırıldı (2026-07-24): köhnəlmiş (artıq kodda həll olunmuş) tapıntılar ✅-ə yeniləndi, təkrarlanan checklist cədvəlləri birləşdirildi, yalnız "Qalan İşlər" bölməsi yeganə "nə qalıb" mənbəyi olaraq ən yuxarıya çıxarıldı.

### 2026-07-25
- **Gmail SMTP qoşuldu** (`.env` → `smtp.gmail.com:587`, App Password, AUTH+STARTTLS).
- **`AdminSeeder` təhlükəsizlik düzəlişi:** hardcode admin şifrələri (`admin1234` və s.) `AdminSeederProperties`-ə (`app.admin-seed.*`) köçürüldü; prod profilində seed default olaraq söndürüldü (`ADMIN_SEED_ENABLED:false`) və şifrə default-ları boşdur; seed açıq, şifrə boş olarsa açılışda fail-fast; zəif (12 simvoldan qısa) şifrə üçün WARN.
- **SMTP timeout-ları** 3000 → 10000 ms (env ilə tənzimlənən: `MAIL_CONNECTION_TIMEOUT_MS`/`MAIL_READ_TIMEOUT_MS`/`MAIL_WRITE_TIMEOUT_MS`) — Gmail handshake-i əvvəlki limitə sığmırdı və məktub səssizcə itirdi.
- **`EmailService.send()` `@Async` edildi** (`config/AsyncConfig` — `@EnableAsync` + MDC-köçürən `TaskDecorator`), beləliklə SMTP gecikməsi qeydiyyat/login cavab müddətinə əlavə olunmur və correlation-id async loglarda saxlanılır.
- `application-prod.yml`-a `server.forward-headers-strategy: framework` əlavə olundu (reverse proxy arxasında düzgün sxem/host).
- `.env` və `.env.example` yeni dəyişənlərlə (`ADMIN_SEED_*`, `MAIL_*_TIMEOUT_MS`) və Gmail qeydləri ilə yeniləndi; `.env`-dəki köhnə "MailHog" şərhi düzəldildi.
- Dəyişikliklər `./mvnw compile` ilə yoxlanıldı — xəta yoxdur (canlı Postgres/Docker bu mühitdə işləmirdi, ona görə boot/inteqrasiya testi hələ icra olunmayıb).
