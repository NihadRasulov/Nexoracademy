# NexoraAcademy Backend — API Contract

Bu sənəd `D:\NexoraAcademy\NexoraAcademy\NexoraAcademy` repo-sunun mənbə kodunun (2026-07-22 tarixli vəziyyəti) birbaşa oxunması ilə hazırlanıb. Məqsəd: ayrıca ASP.NET Core Admin Panel (BFF) yazan komandaya bu Spring Boot backend-in **faktiki** davranışını təsvir etmək. Heç bir sahə/davranış uydurulmayıb; kodda tapılmayan və ya kodda təsdiqlənə bilməyən hər şey aydın şəkildə "// TƏSDİQLƏNMƏYİB" və ya "Bu repo-da tapılmadı" ilə işarələnib.

**Stack:** Java 21, Spring Boot 4.1.0 (parent POM), Spring Security (stateless, JWT), Spring Data JPA + Hibernate ORM 7, PostgreSQL 16 (Flyway migrasiyaları), springdoc-openapi 2.7.0, jjwt 0.12.6.

**Base URL (local dev):** `http://localhost:8081` (bax `src/main/resources/application.yml:2` — `server.port: 8081`). Global path prefiksi **yoxdur** (`server.servlet.context-path` heç yerdə təyin olunmayıb) — bütün endpoint-lər birbaşa `http://localhost:8081/api/v1/...` altındadır.

---

## 1. Autentifikasiya / JWT

### 1.0 Email OTP (register + login) — 2026-07-22-dən sonra əlavə olunub
Bu bölmə yenilənib: **login artıq iki addımlıdır**, register-in email-təsdiqi də link deyil, 6-rəqəmli OTP-dir. Mənbə: `AuthService.java`, `AuthController.java`, `Session`/`SessionType` (yeni `LOGIN_OTP` dəyəri + `attempts` sütunu, bax `V13__add_login_otp_and_session_attempts.sql`).

- **Register:** `POST /api/v1/auth/register` əvvəlki kimi hesab yaradır (`PENDING_VERIFICATION`), amma indi email-ə **link əvəzinə 6-rəqəmli OTP kod** göndərir. Təsdiq: `POST /api/v1/auth/verify-email` artıq `{token}` yox, **`{email, otp}`** qəbul edir.
- **Login iki addımlıdır:**
  1. `POST /api/v1/auth/login` `{email, password}` — uğurlu olsa, **tokens QAYTARMIR**, əvəzinə email-ə 6-rəqəmli OTP göndərir və `LoginOtpResponse{message, email, expiresInSeconds}` qaytarır (200).
  2. `POST /api/v1/auth/login/verify-otp` `{email, otp}` — OTP düzgündürsə, əsl `TokenResponse` (access+refresh) qaytarır (200). Bu addımda `lastLoginAt`/`UserLoggedInEvent` işə düşür (addım-1-də yox).
- **OTP xüsusiyyətləri:**
  - 6 rəqəm, `SecureRandom` ilə, `String.format("%06d", ...)`.
  - Saxlanma: mövcud `identity.sessions` cədvəlində (`Session` entity), `tokenHash` sütununda SHA-256 hash kimi (xam OTP DB-də saxlanmır) — register-OTP `type=EMAIL_VERIFY`, login-OTP `type=LOGIN_OTP`.
  - Ömrü: `AuthProperties.emailVerifyExpirationMs` (register-OTP) və `loginOtpExpirationMs` (login-OTP) — hər ikisinin **default** dəyəri 10 dəqiqədir, env `EMAIL_VERIFY_EXPIRATION_MS`/`LOGIN_OTP_EXPIRATION_MS` ilə override olunur.
  - **Brute-force qoruması:** `Session.attempts` sütunu — hər səhv təxmin artırılır, `AuthProperties.otpMaxAttempts` (default 5) aşılanda o kod dərhal ləğv olunur (`revokedAt` doldurulur) — istifadəçi yenidən `/login` (və ya `/resend-verification`) çağırıb təzə kod istəməlidir.
  - Yeni OTP istəniəndə (təkrar `/login` və ya `/resend-verification` çağırılanda) **əvvəlki hələ istifadə olunmamış OTP avtomatik ləğv olunur** — eyni anda yalnız bir aktiv kod var.
  - Xəta mesajları (hamısı 401, `InvalidTokenException`): kod tapılmadı/vaxtı bitib/artıq istifadə olunub → `"Invalid or expired code"`; vaxtı keçib → `"Code has expired"`; səhv rəqəmlər → `"Invalid code"`.
- **`refresh`/`logout` dəyişməyib** — `verify-otp`-dan alınan tokenlərlə eyni şəkildə işləyir (bax §1.6/§1.7).
- **DİQQƏT — canlı sınaqda aşkarlanan konfiqurasiya qeydi:** real `.env` faylında (repo-dakı `.env.example` deyil, developer-in öz `.env`-i) köhnə `EMAIL_VERIFY_EXPIRATION_MS=86400000` (24 saat) dəyəri qala bilər — bu, real OTP-nin 24 saat etibarlı qalması deməkdir (6 rəqəmli kod üçün həddindən artıq uzun, brute-force pəncərəsini böyüdür). Yeni `.env`-lərdə `EMAIL_VERIFY_EXPIRATION_MS=600000`, `LOGIN_OTP_EXPIRATION_MS=600000`, `OTP_MAX_ATTEMPTS=5` istifadə edilməlidir (bax `.env.example`).

### 1.1 Alqoritm
`src/main/java/az/demo/NexoraAcademy/service/JwtService.java` — token **HS256** (HMAC-SHA256) ilə imzalanır:
```java
.signWith(signingKey(), Jwts.SIG.HS256)
```
`signingKey()` `Keys.hmacShaKeyFor(secret.getBytes(UTF_8))` istifadə edir, yəni sirr sırf simmetrik HMAC açarı kimi işlədilir (RS256/açar cütü YOXDUR).

### 1.2 Signing key mənbəyi
- `JwtProperties` (`@ConfigurationProperties(prefix = "app.jwt")`) sahəsi: `secret`.
- `application.yml`: `app.jwt.secret: ${JWT_SECRET:change-me-change-me-change-me-change-me-change-me!!}` — yəni **environment variable `JWT_SECRET`**-dən oxunur (`.env` faylı vasitəsilə, `spring-dotenv`/`DotenvEnvironmentPostProcessor` ilə yüklənir — bax `.env.example`), default (fallback) qiymət yalnız dev üçündür.
- `application-prod.yml`-də **default YOXDUR** (`app.jwt.secret: ${JWT_SECRET}`) — `prod` profili aktiv olanda `JWT_SECRET` təyin olunmayıbsa tətbiq başlamır (bilərəkdən belə edilib, bax faylın başındakı şərh).
- Faktiki sirr dəyəri bu sənəddə yazılmayıb (təhlükəsizlik). Minimum uzunluq tələbi kod şərhində qeyd olunub: "min. 256 bit / 32 byte", amma bunun runtime-da yoxlanıldığına dair kod tapılmadı (`// TƏSDİQLƏNMƏYİB` — sadəcə şərh var, məcburi assertion yoxdur).
- Key Vault/Secrets Manager inteqrasiyası bu repo-da **tapılmadı** — sadəcə `.env`/OS environment variable.

### 1.3 `iss` / `aud`
- `iss` (issuer): `JwtProperties.issuer`, default `"nexora-academy"`, env `JWT_ISSUER` ilə override olunur. Token həm yaradılanda (`issuer(...)`) həm doğrulananda (`requireIssuer(...)`) yoxlanılır — səhv `iss` olan token rədd edilir.
- `aud` (audience): **JWT-də `aud` claim-i ümumiyyətlə YOXDUR.** `JwtService.buildToken()`-da belə bir claim əlavə edilmir və `parseClaims()`-də tələb olunmur.

### 1.4 Access / Refresh token ömrü
`JwtProperties` (default dəyərlər, `application.yml`-də env ilə override olunur):
| Token | Default | Env variable |
|---|---|---|
| Access token | 15 dəqiqə (900000 ms) | `JWT_ACCESS_EXPIRATION_MS` |
| Refresh token | 30 gün (2592000000 ms) | `JWT_REFRESH_EXPIRATION_MS` |

### 1.5 JWT claim-lərinin tam siyahısı
Mənbə: `JwtService.buildToken(User user, String tokenType, long expirationMs)`.

| Claim | Tip (JWT-də) | Dəyər |
|---|---|---|
| `sub` | string | `user.getId()` — UUID-in `toString()` forması |
| `role` | string | `user.getRole().name()` — tək rol adı (bax §6), **array DEYİL** |
| `type` | string | `"access"` və ya `"refresh"` |
| `iss` | string | yuxarı bax (§1.3) |
| `iat` | number (Unix epoch, saniyə) | token yaradılma anı |
| `exp` | number (Unix epoch, saniyə) | `iat + expirationMs` |
| `jti` | string | `UUID.randomUUID().toString()` — hər tokenin unikal id-si |

Canlı nümunə (decode edilmiş payload, real sınaqdan):
```json
{"sub":"11bf8b20-370d-4cbe-91c7-89bbaead6ff2","role":"STUDENT","type":"access","iss":"nexora-academy","iat":1784701177,"exp":1784702077,"jti":"e3e69b62-6bd8-46f2-969b-062f0089fbd1"}
```
**`email`, `permissions`, `userId` (ayrıca claim kimi) claim-ləri YOXDUR** — istifadəçi email-i almaq üçün `GET /api/v1/users/me` çağırılmalıdır.

### 1.6 Refresh axını
`AuthController` (`/api/v1/auth`) — bax §2.1 üçün tam cədvəl. Xülasə:
- `POST /api/v1/auth/refresh` — real endpoint var. Request body: `{"refreshToken": "string"}`. Response body: `TokenResponse` (`accessToken`, `refreshToken`, `tokenType="Bearer"`, `expiresInSeconds`).
- **Refresh token bir dəfəlik istifadə olunur (rotation + reuse-detection):** `AuthService.refresh()` — köhnə refresh token `Session` cədvəlində tapılır (`tokenHash` = SHA-256(rawToken) ilə axtarılır), `usedAt`/`revokedAt` yoxlanılır (əgər artıq işlədilibsə/ləğv olunubsa → `401 InvalidTokenException`), sonra dərhal `usedAt`/`revokedAt` doldurulur və **yeni** access+refresh cütü verilir. Köhnə refresh token ikinci dəfə işlədilə bilməz.
- `isTokenValid()` + `isRefreshToken()` yoxlanılır — access token-i refresh üçün göndərmək `401 "Invalid refresh token"` verir.

### 1.7 Logout — real DB revocation
`AuthService.logout(RefreshTokenRequest request)`:
```java
sessionRepository.findByTokenHash(hash(request.refreshToken()))
    .ifPresent(session -> { if (revokedAt == null) session.setRevokedAt(Instant.now()); ... });
```
Yəni **stateful**-dir: `identity.sessions` cədvəlində (`Session` entity, `SessionType.SESSION`) `revokedAt` sütunu doldurulur — refresh token bir daha `POST /auth/refresh` üçün işləməyəcək.

**VACİB:** yalnız **refresh token** DB-də izlənir/ləğv olunur. **Access token stateless-dir — logout-dan sonra da öz təbii 15-dəqiqəlik ömrü bitənə qədər etibarlıdır** (heç bir blacklist/revoked-access-token mexanizmi tapılmadı). BFF tərəfi bunu nəzərə almalıdır: logout zamanı client access token-i özü silməlidir, backend onu "yandıra" bilmir.

### 1.8 Clock skew
`Jwts.parser().verifyWith(...).requireIssuer(...).build()` — heç bir `clockSkew(...)` çağırışı tapılmadı. jjwt kitabxanasının defaultu 0 saniyədir, yəni **əlavə tolerantlıq YOXDUR** (server saatları arasında fərq token-i vaxtından tez/gec etibarsız edə bilər).

### 1.9 Digər autentifikasiya detalları
- `Authorization: Bearer <token>` header formatı (`JwtAuthenticationFilter`, prefiks dəqiq `"Bearer "`).
- Access token həmişə `type` claim-i `"access"` olmalıdır — filter `isAccessToken()` yoxlayır, əks halda SecurityContext boş qalır (sonra `401` `JwtAuthenticationEntryPoint` tərəfindən verilir).
- Hesab statusu (`AccountStatus`) login-dən **sonra** deyil, `CustomUserDetailsService.build()`-da UserDetails yaradılanda yoxlanılır: `SUSPENDED`/`BANNED` → `locked=true`, `DEACTIVATED` → `disabled=true`. `PENDING_VERIFICATION` statuslu (email təsdiqlənməmiş) istifadəçi **login ola bilir** — email verification login-i bloklamır.
- Şifrə hash: `BCryptPasswordEncoder` (`SecurityConfig.passwordEncoder()`).

---

## 2. Bütün REST Endpoint-lər

**Ümumi qeyd — icazə modeli:** Bu layihədə **`@PreAuthorize`/`@Secured`/`@RolesAllowed` HEÇ YERDƏ istifadə olunmur** (bütün mənbə kodunda axtarıldı, tapılmadı). Bütün rol-əsaslı icazə **path-based**-dir — `SecurityConfig.securityFilterChain()`-də `.requestMatchers(...).hasAnyRole(...)` qaydaları ilə tənzimlənir. Aşağıdakı cədvəllərdə "Rol" sütunu bu path qaydalarından götürülüb. **İki istisna** var — burada icazə servis səviyyəsində əl ilə yoxlanılır (`SecurityUtils.hasAnyRole()` + `org.springframework.security.access.AccessDeniedException` atılması, → HTTP 403): `EnrollmentService` və `CourseReviewService` (aşağıda hər ikisi ayrıca qeyd olunub).

`hasAnyRole("X")` Spring Security-də avtomatik olaraq istifadəçinin `ROLE_X` authority-sinə malik olmasını yoxlayır (`CustomUserDetailsService` `"ROLE_" + user.getRole().name()` authority-si verir).

### 2.0 SecurityConfig-in tam, sıra ilə qayda siyahısı
(sıra vacibdir — Spring Security ilk uyğun gələn qaydanı tətbiq edir)

| # | Path pattern(lər) | Metod | Qayda |
|---|---|---|---|
| 1 | `/api/v1/auth/**` | hamısı | `permitAll()` |
| 2 | `/api/v1/payments/callback` | hamısı | `permitAll()` |
| 3 | `/api/v1/courses/**`, `/api/v1/categories/**` | yalnız `GET` | `permitAll()` |
| 4 | `/swagger-ui/**`, `/v3/api-docs/**` | hamısı | `permitAll()` |
| 5 | `/actuator/health` | hamısı | `permitAll()` |
| 6 | `/api/v1/users/me`, `/api/v1/users/me/**` | hamısı | `authenticated()` (istənilən rol) |
| 7 | `/api/v1/admin/**`, `/api/v1/users/**` | hamısı | `hasAnyRole("ADMIN","SYSTEM_ADMIN")` |
| 8 | `/api/v1/content/**`, `/api/v1/courses/**`, `/api/v1/categories/**`, `/api/v1/instructors/**`, `/api/v1/course-groups/**`, `/api/v1/course-instructors/**` | hamısı (qeyd-3-dən sonra qalan, yəni GET-dən başqa hər şey + digər HTTP metodları) | `hasAnyRole("ADMIN","SYSTEM_ADMIN","CONTENT_MANAGER")` |
| 9 | `/api/v1/kb-articles/**`, `/api/v1/graduate-outcomes/**` | hamısı | `hasAnyRole("ADMIN","SYSTEM_ADMIN","CONTENT_MANAGER")` |
| 10 | `/api/v1/sales/**` | hamısı | `hasAnyRole("ADMIN","SYSTEM_ADMIN","SALES_CRM")` |
| 11 | `/api/v1/payments/**`, `/api/v1/scholarships/**` | hamısı | `hasAnyRole("ADMIN","SYSTEM_ADMIN")` |
| 12 | `/api/v1/sessions/**`, `/api/v1/oauth-accounts/**` | hamısı | `hasAnyRole("ADMIN","SYSTEM_ADMIN")` |
| 13 | `/api/v1/notifications/**` | hamısı | `hasAnyRole("ADMIN","SYSTEM_ADMIN")` |
| 14 | (qalan hər şey) | hamısı | `authenticated()` — bura `enrollments`, `course-reviews`, `/test/**` daxildir |

Qeyd-8 üçün: `/api/v1/course-instructors` yalnız qayda-8-dədir (GET də daxil olmaqla tam CONTENT_MANAGER-only), `courses`/`categories` isə GET-i qayda-3 tutur (public), qalan metodları qayda-8.

---

### 2.1 AuthController — `/api/v1/auth` (hamısı `permitAll`)
Fayl: `controller/auth/AuthController.java`

| Metod | Path | Rol | Request Body | Response Body | Status |
|---|---|---|---|---|---|
| POST | `/api/v1/auth/register` | permitAll | `RegisterRequest{email:string(@Email,max255), fullName:string(2-150), phone:string?(pattern), password:string(8-72, ≥1 hərf+≥1 rəqəm)}` | `RegisterResponse{userId:UUID, email:string, message:string}` | 201, 400 (validation), 409 (email/phone artıq var) |
| POST | `/api/v1/auth/login` | permitAll | `LoginRequest{email:string(@Email), password:string}` | **`LoginOtpResponse{message:string, email:string, expiresInSeconds:long}`** — tokens YOXDUR, uğurlu olsa email-ə 6-rəqəmli OTP göndərilir | 200, 400, 401 (yanlış email/parol — mesaj hər iki halda eynidir, user enumeration qorunur) |
| POST | `/api/v1/auth/login/verify-otp` | permitAll | `LoginOtpVerifyRequest{email:string(@Email), otp:string(6 rəqəm)}` | `TokenResponse{accessToken:string, refreshToken:string, tokenType:"Bearer", expiresInSeconds:long}` | 200, 400, 401 (kod səhv/vaxtı bitib/tapılmadı) |
| POST | `/api/v1/auth/refresh` | permitAll | `RefreshTokenRequest{refreshToken:string}` | `TokenResponse` (yuxarı bax) | 200, 400, 401 (etibarsız/istifadə olunmuş/vaxtı bitmiş) |
| POST | `/api/v1/auth/logout` | permitAll | `RefreshTokenRequest{refreshToken:string}` | boş (`Void`) | 204 (tapılmasa belə 204 qaytarır — idempotent) |
| POST | `/api/v1/auth/forgot-password` | permitAll | `ForgotPasswordRequest{email:string(@Email)}` | boş | 204 (email mövcud olub-olmamasından asılı olmayaraq — enumeration qorunur) |
| POST | `/api/v1/auth/reset-password` | permitAll | `ResetPasswordRequest{token:string, newPassword:string(8-72, ≥1 hərf+≥1 rəqəm)}` | boş | 204, 400, 401 (etibarsız/vaxtı bitmiş token) — **dəyişməyib, hələ də link-token-based**, OTP DEYİL |
| POST | `/api/v1/auth/verify-email` | permitAll | **`VerifyEmailRequest{email:string(@Email), otp:string(6 rəqəm)}`** (əvvəllər `{token}` idi — dəyişdi, bax §1.0) | boş | 204, 400, 401 |
| POST | `/api/v1/auth/resend-verification` | permitAll | `ResendVerificationRequest{email:string(@Email)}` | boş | 204 — yeni OTP göndərir, əvvəlkini ləğv edir |

Qeyd: `login` və `login/verify-otp` endpoint-ləri `HttpServletRequest`-dən `remoteAddr`-ı oxuyur; `UserLoggedInEvent` yalnız `verify-otp` uğurlu olanda yayımlanır (bax §1.0 — login "tamamlanmış" sayılmır OTP təsdiqlənənə qədər).

---

### 2.2 UserController — `/api/v1/users`
Fayl: `controller/identity/UserController.java`

| Metod | Path | Rol | Request Body | Response Body | Status |
|---|---|---|---|---|---|
| GET | `/api/v1/users/me` | `authenticated()` (istənilən rol — özü haqqında) | — | `UserResponse` (aşağı bax) | 200, 401 |
| PATCH | `/api/v1/users/me` | `authenticated()` | `UpdateProfileRequest{email:string?, phone:string?, fullName:string?, locale:string?, profile:Map<string,object>?}` — hamısı optional, `@Valid` (format yoxlanılır, presence yox) | `UserResponse` | 200, 400, 401, 409 (email tutulub) |
| POST | `/api/v1/users/me/password` | `authenticated()` | `ChangePasswordRequest{currentPassword:string, newPassword:string(8-72,...)}` | boş | 204, 400, 401 (cari parol səhvdirsə) |
| POST | `/api/v1/users` | `ADMIN`,`SYSTEM_ADMIN` | `UserRequest` (tam, aşağı bax) | `UserResponse` | 201, 400, 403, 409 |
| GET | `/api/v1/users` | `ADMIN`,`SYSTEM_ADMIN` | Query: `q:string?`, `role:UserRole?`, `status:AccountStatus?`, `page:int?`(default 0), `size:int?`(default 20), `sort:string?`(default `createdAt`) | **`Page<UserResponse>`** (Spring Data default JSON — bax §2.pagination) | 200, 403 |
| GET | `/api/v1/users/{id}` | `ADMIN`,`SYSTEM_ADMIN` | — | `UserResponse` | 200, 403, 404 |
| PUT | `/api/v1/users/{id}` | `ADMIN`,`SYSTEM_ADMIN` | `UserRequest` (tam — bütün `@NotBlank`/`@NotNull` sahələr məcburidir) | `UserResponse` | 200, 400, 403, 404, 409 |
| PATCH | `/api/v1/users/{id}` | `ADMIN`,`SYSTEM_ADMIN` | `UserRequest` (qismən — göndərilməyən sahələr toxunulmaz qalır, göndərilənlər format yoxlanılır) | `UserResponse` | 200, 400, 403, 404, 409 |
| DELETE | `/api/v1/users/{id}` | `ADMIN`,`SYSTEM_ADMIN` | — | boş | 204, 403, 404 |

**`UserRequest`** (create/update/patch üçün ortaq DTO): `email:string(@Email,max255)`, `phone:string?(pattern)`, `fullName:string(2-150)`, `password:string(8-72,...)`, `role:UserRole?` (default `STUDENT` create-də), `status:AccountStatus?` (default `PENDING_VERIFICATION` create-də), `locale:string?(pattern "az" və ya "az-AZ")`, `profile:Map<string,object>?(max 50 açar)`.

**`UserResponse`**: `id:UUID`, `email:string`, `phone:string?`, `fullName:string`, `role:UserRole`, `status:AccountStatus`, `locale:string`, `profile:Map<string,object>`, `lastLoginAt:Instant?`, `createdAt:Instant`, `updatedAt:Instant`.

Qeyd: `patch` endpoint-i admin `role` sahəsini dəyişəndə `UserRoleChangedEvent` yayımlayır (`event/UserRoleChangedEvent.java`) — event listener-lərin nə etdiyi bu sənədin əhatəsindən kənardır.

---

### 2.3 CategoryController — `/api/v1/categories`
Fayl: `controller/catalog/CategoryController.java`. GET → `permitAll`, digər metodlar → `ADMIN`,`SYSTEM_ADMIN`,`CONTENT_MANAGER`.

| Metod | Path | Rol | Request Body | Response Body | Status |
|---|---|---|---|---|---|
| POST | `/api/v1/categories` | CM/ADMIN/SYS_ADMIN | `CategoryRequest{slug:string(max80,pattern kebab-case), name:string(2-120), parentId:short?(>0), sortOrder:int?(≥0), active:boolean?}` | `CategoryResponse` | 201, 400, 403, 409 |
| GET | `/api/v1/categories` | **permitAll** | — | `List<CategoryResponse>` (paginasiya YOXDUR — düz massiv) | 200 |
| GET | `/api/v1/categories/{id}` | **permitAll** | — (`id:short`) | `CategoryResponse` | 200, 404 |
| PUT | `/api/v1/categories/{id}` | CM/ADMIN/SYS_ADMIN | `CategoryRequest` (tam) | `CategoryResponse` | 200, 400, 403, 404, 409 |
| PATCH | `/api/v1/categories/{id}` | CM/ADMIN/SYS_ADMIN | `CategoryRequest` (qismən) | `CategoryResponse` | 200, 400, 403, 404, 409 |
| DELETE | `/api/v1/categories/{id}` | CM/ADMIN/SYS_ADMIN | — | boş | 204, 403, 404 |

`CategoryResponse`: `id:short`, `slug:string`, `name:string`, `parentId:short?`, `sortOrder:int`, `active:boolean`.

---

### 2.4 CourseController — `/api/v1/courses`
Fayl: `controller/catalog/CourseController.java`. GET → `permitAll`, digər metodlar → `ADMIN`,`SYSTEM_ADMIN`,`CONTENT_MANAGER`.

| Metod | Path | Rol | Request Body | Response Body | Status |
|---|---|---|---|---|---|
| POST | `/api/v1/courses` | CM/ADMIN/SYS_ADMIN | `CourseRequest` (aşağı bax) | `CourseResponse` | 201, 400, 403, 409 |
| GET | `/api/v1/courses` | **permitAll** | Query: `q:string?`, `categoryId:short?`, `difficulty:DifficultyLevel?`, `deliveryFormat:DeliveryFormat?`, `published:boolean?`, `active:boolean?`, `page:int?`(0), `size:int?`(20), `sort:string?`(`createdAt`) | **`Page<CourseResponse>`** | 200 |
| GET | `/api/v1/courses/{id}` | **permitAll** | — (`id:UUID`) | `CourseResponse` | 200, 404 |
| PUT | `/api/v1/courses/{id}` | CM/ADMIN/SYS_ADMIN | `CourseRequest` (tam) | `CourseResponse` | 200, 400, 403, 404 |
| PATCH | `/api/v1/courses/{id}` | CM/ADMIN/SYS_ADMIN | `CourseRequest` (qismən) | `CourseResponse` | 200, 400, 403, 404 |
| DELETE | `/api/v1/courses/{id}` | CM/ADMIN/SYS_ADMIN | — | boş | 204, 403, 404 |

`CourseRequest`: `slug:string(max160, kebab-case)`, `categoryId:short`, `title:string(3-200)`, `shortDescription:string?(max400)`, `fullDescription:string?(max20000)`, `targetAudience:string?(max5000)`, `difficulty:DifficultyLevel`, `durationWeeks:short?(>0)`, `deliveryFormat:DeliveryFormat`, `locationText:string?(max255)`, `basePrice:decimal?(≥0)`, `currency:string?(3 böyük hərf, ISO4217)`, `pricePeriod:string?(max30)`, `published:boolean?`, `active:boolean?`, `archived:boolean?`, `validFrom:Instant?`, `validUntil:Instant?`(`validFrom`-dan sonra olmalı, `@DateRange`), `content:Map<string,object>?(max100 açar)`, `relatedCourseIds:UUID[]?(max20)`.

`CourseResponse`: yuxarıdakı sahələr + `id:UUID`, `createdBy:UUID?`, `createdAt:Instant`, `updatedAt:Instant`.

**Canlı nümunə — `Page<CourseResponse>` cavabının həqiqi JSON forması** (Spring Data-nın öz default `Page` serializasiyası, custom envelope YOXDUR):
```json
{
  "content": [ { "id": "...", "slug": "...", "...": "..." } ],
  "empty": false,
  "first": true,
  "last": false,
  "number": 0,
  "numberOfElements": 2,
  "pageable": {
    "offset": 0, "pageNumber": 0, "pageSize": 2, "paged": true,
    "sort": { "empty": false, "sorted": true, "unsorted": false },
    "unpaged": false
  },
  "size": 2,
  "sort": { "empty": false, "sorted": true, "unsorted": false },
  "totalElements": 28,
  "totalPages": 14
}
```
BFF tərəfi bunu bilməlidir: `totalElements`/`totalPages` var, amma sort/pageable obyektlərinin içi Spring-ə xas formatdır (məs. `sort` daxilində konkret sahə adları yoxdur, yalnız bool-lar).

---

### 2.5 InstructorController — `/api/v1/instructors` (hamısı `ADMIN`,`SYSTEM_ADMIN`,`CONTENT_MANAGER` — GET də daxil, permitAll YOXDUR)
Fayl: `controller/catalog/InstructorController.java`

| Metod | Path | Rol | Request Body | Response Body | Status |
|---|---|---|---|---|---|
| POST | `/api/v1/instructors` | CM/ADMIN/SYS_ADMIN | `InstructorRequest{userId:UUID?, fullName:string(2-150), bio:string?(max8000), photoUrl:string?(http(s) URL), linkedinUrl:string?(http(s) URL), certifications:List<Map>?(max50), active:boolean?}` | `InstructorResponse` | 201, 400, 403, 409 |
| GET | `/api/v1/instructors` | CM/ADMIN/SYS_ADMIN | — | `List<InstructorResponse>` | 200, 403 |
| GET | `/api/v1/instructors/{id}` | CM/ADMIN/SYS_ADMIN | — (`id:UUID`) | `InstructorResponse` | 200, 403, 404 |
| PUT | `/api/v1/instructors/{id}` | CM/ADMIN/SYS_ADMIN | `InstructorRequest` (tam) | `InstructorResponse` | 200, 400, 403, 404 |
| PATCH | `/api/v1/instructors/{id}` | CM/ADMIN/SYS_ADMIN | `InstructorRequest` (qismən) | `InstructorResponse` | 200, 400, 403, 404 |
| DELETE | `/api/v1/instructors/{id}` | CM/ADMIN/SYS_ADMIN | — | boş | 204, 403, 404 |

`InstructorResponse`: `id:UUID`, `userId:UUID?`, `fullName:string`, `bio:string?`, `photoUrl:string?`, `linkedinUrl:string?`, `avgRating:decimal?`, `certifications:List<Map<string,object>>`, `active:boolean`, `createdAt:Instant`.

---

### 2.6 CourseInstructorController — `/api/v1/course-instructors` (hamısı CM/ADMIN/SYS_ADMIN)
Fayl: `controller/catalog/CourseInstructorController.java`. **Composite key** (`courseId`+`instructorId`) — `id` deyil.

| Metod | Path | Rol | Request Body | Response Body | Status |
|---|---|---|---|---|---|
| POST | `/api/v1/course-instructors` | CM/ADMIN/SYS_ADMIN | `CourseInstructorRequest{courseId:UUID, instructorId:UUID, role:string(pattern: "lead"\|"co-instructor"\|"mentor")}` | `CourseInstructorResponse` | 201, 400, 403, 409 |
| GET | `/api/v1/course-instructors` | CM/ADMIN/SYS_ADMIN | — | `List<CourseInstructorResponse>` | 200, 403 |
| GET | `/api/v1/course-instructors/{courseId}/{instructorId}` | CM/ADMIN/SYS_ADMIN | — (iki `UUID` path var) | `CourseInstructorResponse` | 200, 403, 404 |
| PUT | `/api/v1/course-instructors/{courseId}/{instructorId}` | CM/ADMIN/SYS_ADMIN | `CourseInstructorRequest` (tam) | `CourseInstructorResponse` | 200, 400, 403, 404 |
| PATCH | `/api/v1/course-instructors/{courseId}/{instructorId}` | CM/ADMIN/SYS_ADMIN | `CourseInstructorRequest` (qismən) | `CourseInstructorResponse` | 200, 400, 403, 404 |
| DELETE | `/api/v1/course-instructors/{courseId}/{instructorId}` | CM/ADMIN/SYS_ADMIN | — | boş | 204, 403, 404 |

`CourseInstructorResponse`: `courseId:UUID`, `instructorId:UUID`, `role:string`.

---

### 2.7 CourseGroupController — `/api/v1/course-groups` (hamısı CM/ADMIN/SYS_ADMIN)
Fayl: `controller/academics/CourseGroupController.java`

| Metod | Path | Rol | Request Body | Response Body | Status |
|---|---|---|---|---|---|
| POST | `/api/v1/course-groups` | CM/ADMIN/SYS_ADMIN | `CourseGroupRequest{courseId:UUID, groupCode:string(max40,alfanumerik+"-"/"_"), startDate:LocalDate, endDate:LocalDate?(≥startDate), registrationDeadline:Instant?, totalSeats:int(>0,≤10000), status:GroupStatus?, schedule:List<Map>?(max20)}` | `CourseGroupResponse` | 201, 400, 403, 409 |
| GET | `/api/v1/course-groups` | CM/ADMIN/SYS_ADMIN | — | `List<CourseGroupResponse>` | 200, 403 |
| GET | `/api/v1/course-groups/{id}` | CM/ADMIN/SYS_ADMIN | — (`id:UUID`) | `CourseGroupResponse` | 200, 403, 404 |
| PUT | `/api/v1/course-groups/{id}` | CM/ADMIN/SYS_ADMIN | `CourseGroupRequest` (tam) | `CourseGroupResponse` | 200, 400, 403, 404 |
| PATCH | `/api/v1/course-groups/{id}` | CM/ADMIN/SYS_ADMIN | `CourseGroupRequest` (qismən) | `CourseGroupResponse` | 200, 400, 403, 404 |
| DELETE | `/api/v1/course-groups/{id}` | CM/ADMIN/SYS_ADMIN | — | boş | 204, 403, 404 |

`CourseGroupResponse`: `id:UUID`, `courseId:UUID`, `groupCode:string`, `startDate:LocalDate`, `endDate:LocalDate?`, `registrationDeadline:Instant?`, `totalSeats:int`, `reservedSeats:int`, `status:GroupStatus`, `schedule:List<Map<string,object>>`, `createdAt:Instant`.

**QEYD (BFF üçün vacib):** `GET /api/v1/course-groups/**` **public deyil** — `courses`/`categories`-dən fərqli olaraq bu resurs CONTENT_MANAGER/ADMIN-only-dir, GET də daxil. Yəni tələbələrin/qonaqların frontend-i qrup siyahısını görmək üçün başqa (public) mənbə tapmalıdır — bu repo-da belə public endpoint tapılmadı.

---

### 2.8 EnrollmentController — `/api/v1/enrollments`
Fayl: `controller/academics/EnrollmentController.java`. **Path-based qayda YOXDUR** — SecurityConfig-in son sətrinə (`anyRequest().authenticated()`) düşür, yəni istənilən authenticated rol HTTP səviyyəsində keçir. Faktiki icazə **servis səviyyəsində** (`service/academics/EnrollmentService.java`) yoxlanılır:

- **STAFF_ROLES = `ADMIN`, `SYSTEM_ADMIN`, `SALES_CRM`** — bu rollar hər kəsin qeydiyyatına baxa/dəyişə bilər.
- Digər rollar (məs. `STUDENT`) yalnız **öz** qeydiyyatını yarada/görə/ləğv edə bilər.

| Metod | Path | Rol/icazə (servis səviyyəsində) | Request Body | Response Body | Status |
|---|---|---|---|---|---|
| POST | `/api/v1/enrollments` | authenticated; qeyri-staff yalnız `request.userId() == öz id-i` ola bilər, staff istənilən `userId` yaza bilər | `EnrollmentRequest{userId:UUID, groupId:UUID, status:EnrollmentStatus?, idempotencyKey:string(8-100,alfanumerik+"-"/"_"), consentVersion:string?(max20), consentGivenAt:Instant?(keçmiş/indiki)}` | `EnrollmentResponse` | 201, 400, 403 (özündən başqasını yazmaq), 409 (idempotencyKey təkrarı və ya eyni user+group cütü artıq var) |
| GET | `/api/v1/enrollments` | **yalnız staff** (STUDENT → 403) | — | `List<EnrollmentResponse>` | 200, 403 |
| GET | `/api/v1/enrollments/{id}` | staff **və ya** qeydin sahibi | — (`id:UUID`) | `EnrollmentResponse` | 200, 403 (özgə qeydə baxmaq), 404 |
| PUT | `/api/v1/enrollments/{id}` | **yalnız staff** | `EnrollmentRequest` (tam) | `EnrollmentResponse` | 200, 400, 403, 404 |
| PATCH | `/api/v1/enrollments/{id}` | **yalnız staff** | `EnrollmentRequest` (qismən) | `EnrollmentResponse` | 200, 400, 403, 404 |
| DELETE | `/api/v1/enrollments/{id}` | **yalnız staff** | — | boş | 204, 403, 404 |
| POST | `/api/v1/enrollments/{id}/cancel` | staff **və ya** qeydin sahibi | `CancelEnrollmentRequest{reason:string?(max255)}` — **body optional, `null` göndərmək olar** | `EnrollmentResponse` | 200, 403, 404, 409 (artıq ləğv olunub/tamamlanıb) |

**VACİB biznes qaydası:** `status` sahəsi `create`-də **yalnız staff** tərəfindən təyin oluna bilər — qeyri-staff bu sahəni göndərsə belə, backend onu görməzdən gəlir və həmişə `PENDING_PAYMENT`-lə yaradır (ödəniş axınını bypass etməyin qarşısı belə alınır).

`EnrollmentResponse`: `id:UUID`, `userId:UUID`, `groupId:UUID`, `status:EnrollmentStatus`, `idempotencyKey:string`, `consentVersion:string?`, `consentGivenAt:Instant?`, `holdExpiresAt:Instant?`, `enrolledAt:Instant`, `completedAt:Instant?`, `cancelledAt:Instant?`, `cancelReason:string?`.

---

### 2.9 PaymentController — `/api/v1/payments` (hamısı `ADMIN`,`SYSTEM_ADMIN`, `/callback` istisna olmaqla)
Fayl: `controller/billing/PaymentController.java`

| Metod | Path | Rol | Request Body | Response Body | Status |
|---|---|---|---|---|---|
| POST | `/api/v1/payments` | ADMIN/SYS_ADMIN | `PaymentRequest{enrollmentId:UUID, method:PaymentMethod, amount:decimal(0.01-1000000.00, 2 onluq), currency:string?(3 böyük hərf), externalTxnId:string?(max150), idempotencyKey:string(8-100), installments:List<Map>?(max60)}` | `PaymentResponse` | 201, 400, 403, 409 |
| GET | `/api/v1/payments` | ADMIN/SYS_ADMIN | — | `List<PaymentResponse>` | 200, 403 |
| GET | `/api/v1/payments/{id}` | ADMIN/SYS_ADMIN | — (`id:UUID`) | `PaymentResponse` | 200, 403, 404 |
| PUT | `/api/v1/payments/{id}` | ADMIN/SYS_ADMIN | `PaymentRequest` (tam) | `PaymentResponse` | 200, 400, 403, 404 |
| PATCH | `/api/v1/payments/{id}` | ADMIN/SYS_ADMIN | `PaymentRequest` (qismən) | `PaymentResponse` | 200, 400, 403, 404 |
| DELETE | `/api/v1/payments/{id}` | ADMIN/SYS_ADMIN | — | boş | 204, 403, 404 |
| POST | `/api/v1/payments/{id}/capture` | ADMIN/SYS_ADMIN | — | `PaymentResponse` (status → `CAPTURED`) | 200, 403, 404, 409 (yanlış status-dan capture) |
| POST | `/api/v1/payments/callback` | **permitAll** (bax §2.0 qayda-2) | `PaymentCallbackRequest{idempotencyKey:string(max100), status:string(pattern: "captured"\|"failed"), externalTxnId:string?(max150), failureReason:string?(max255)}` | `PaymentResponse` | 200, 400, 404 (idempotencyKey tapılmadı), 409 |

**TƏHLÜKƏSİZLİK QEYDİ (kod şərhindən):** `/callback` bilərəkdən public saxlanılıb, çünki real ödəniş gateway-i qoşulmayıb — kod şərhi açıq deyir ki, **real production inteqrasiyasında gateway-in imza header-i mütləq yoxlanılmalıdır**, hazırda belə yoxlama YOXDUR. BFF/frontend bu endpoint-i birbaşa çağırmamalıdır (bu, gateway-in webhook-u üçündür).

`PaymentResponse`: `id:UUID`, `enrollmentId:UUID`, `method:PaymentMethod`, `amount:decimal`, `currency:string`, `status:PaymentStatus`, `externalTxnId:string?`, `idempotencyKey:string`, `installments:List<Map<string,object>>`, `refundAmount:decimal`, `refundReason:string?`, `initiatedAt:Instant`, `capturedAt:Instant?`, `failureReason:string?`.

Qeyd: `PaymentService`-də ayrıca `refund(id, amount, reason)` metodu var, amma **heç bir controller endpoint-i ona bağlı deyil** — yəni refund üçün HTTP API YOXDUR (`// TƏSDİQLƏNMƏYİB` — bəlkə gələcəkdə əlavə olunacaq, hazırda yalnız daxili metod).

---

### 2.10 ScholarshipController — `/api/v1/scholarships` (hamısı `ADMIN`,`SYSTEM_ADMIN`)
Fayl: `controller/billing/ScholarshipController.java`

| Metod | Path | Rol | Request Body | Response Body | Status |
|---|---|---|---|---|---|
| POST | `/api/v1/scholarships` | ADMIN/SYS_ADMIN | `ScholarshipRequest{name:string(2-150), description:string?(max4000), discountPct:decimal?(0-100), maxRecipients:int?(>0,≤100000), validFrom:LocalDate?, validUntil:LocalDate?(≥validFrom), active:boolean?}` | `ScholarshipResponse` | 201, 400, 403 |
| GET | `/api/v1/scholarships` | ADMIN/SYS_ADMIN | — | `List<ScholarshipResponse>` | 200, 403 |
| GET | `/api/v1/scholarships/{id}` | ADMIN/SYS_ADMIN | — (`id:short`) | `ScholarshipResponse` | 200, 403, 404 |
| PUT | `/api/v1/scholarships/{id}` | ADMIN/SYS_ADMIN | `ScholarshipRequest` (tam) | `ScholarshipResponse` | 200, 400, 403, 404 |
| PATCH | `/api/v1/scholarships/{id}` | ADMIN/SYS_ADMIN | `ScholarshipRequest` (qismən) | `ScholarshipResponse` | 200, 400, 403, 404 |
| DELETE | `/api/v1/scholarships/{id}` | ADMIN/SYS_ADMIN | — | boş | 204, 403, 404 |

`ScholarshipResponse`: `id:short`, `name:string`, `description:string?`, `discountPct:decimal?`, `maxRecipients:int?`, `validFrom:LocalDate?`, `validUntil:LocalDate?`, `active:boolean`, `applications:List<Map<string,object>>`.

---

### 2.11 CmsContentController — `/api/v1/content/cms-content` (hamısı CM/ADMIN/SYS_ADMIN, GET də daxil)
Fayl: `controller/cms/CmsContentController.java`

| Metod | Path | Rol | Request Body | Response Body | Status |
|---|---|---|---|---|---|
| POST | `/api/v1/content/cms-content` | CM/ADMIN/SYS_ADMIN | `CmsContentRequest{key:string(max160,pattern), type:CmsContentType, title:string?(max250), body:string?(max100000), data:Map<string,object>?(max50), published:boolean?, sortOrder:int?(≥0)}` | `CmsContentResponse` | 201, 400, 403, 409 |
| GET | `/api/v1/content/cms-content` | CM/ADMIN/SYS_ADMIN | — | `List<CmsContentResponse>` | 200, 403 |
| GET | `/api/v1/content/cms-content/{id}` | CM/ADMIN/SYS_ADMIN | — (`id:long`) | `CmsContentResponse` | 200, 403, 404 |
| PUT | `/api/v1/content/cms-content/{id}` | CM/ADMIN/SYS_ADMIN | `CmsContentRequest` (tam) | `CmsContentResponse` | 200, 400, 403, 404 |
| PATCH | `/api/v1/content/cms-content/{id}` | CM/ADMIN/SYS_ADMIN | `CmsContentRequest` (qismən) | `CmsContentResponse` | 200, 400, 403, 404 |
| DELETE | `/api/v1/content/cms-content/{id}` | CM/ADMIN/SYS_ADMIN | — | boş | 204, 403, 404 |

**QEYD:** Bu resurs (sayt səhifələri, FAQ, banner, sosial linklər üçün nəzərdə tutulan CMS content) **public GET-ə malik deyil**. Əgər public marketinq saytı bu məzmunu görməlidirsə, hazırkı kodda buna icazə verən endpoint tapılmadı — `// TƏSDİQLƏNMƏYİB, aydınlaşdırılmalıdır` (arxitektural olaraq gözlənilməz görünür, amma kod budur).

`CmsContentResponse`: `id:long`, `key:string`, `type:CmsContentType`, `title:string?`, `body:string?`, `data:Map<string,object>`, `published:boolean`, `sortOrder:int`, `updatedBy:UUID?`, `updatedAt:Instant`.

---

### 2.12 CampaignController — `/api/v1/sales/campaigns` (hamısı `ADMIN`,`SYSTEM_ADMIN`,`SALES_CRM`)
Fayl: `controller/crm/CampaignController.java`

| Metod | Path | Rol | Request Body | Response Body | Status |
|---|---|---|---|---|---|
| POST | `/api/v1/sales/campaigns` | ADMIN/SYS_ADMIN/SALES_CRM | `CampaignRequest{name:string(2-150), bannerImageUrl:string?(URL), ctaUrl:string?(URL), discountPct:decimal?(0-100), startsAt:Instant, endsAt:Instant(>startsAt), active:boolean?, priority:int?(0-10000), courseIds:UUID[]?(max100)}` | `CampaignResponse` | 201, 400, 403 |
| GET | `/api/v1/sales/campaigns` | ADMIN/SYS_ADMIN/SALES_CRM | — | `List<CampaignResponse>` | 200, 403 |
| GET | `/api/v1/sales/campaigns/{id}` | ADMIN/SYS_ADMIN/SALES_CRM | — (`id:UUID`) | `CampaignResponse` | 200, 403, 404 |
| PUT | `/api/v1/sales/campaigns/{id}` | ADMIN/SYS_ADMIN/SALES_CRM | `CampaignRequest` (tam) | `CampaignResponse` | 200, 400, 403, 404 |
| PATCH | `/api/v1/sales/campaigns/{id}` | ADMIN/SYS_ADMIN/SALES_CRM | `CampaignRequest` (qismən) | `CampaignResponse` | 200, 400, 403, 404 |
| DELETE | `/api/v1/sales/campaigns/{id}` | ADMIN/SYS_ADMIN/SALES_CRM | — | boş | 204, 403, 404 |

`CampaignResponse`: `id:UUID`, `name:string`, `bannerImageUrl:string?`, `ctaUrl:string?`, `discountPct:decimal?`, `startsAt:Instant`, `endsAt:Instant`, `active:boolean`, `priority:int`, `courseIds:UUID[]`.

---

### 2.13 ChatSessionController — `/api/v1/sales/chat-sessions` (hamısı ADMIN/SYS_ADMIN/SALES_CRM)
Fayl: `controller/crm/ChatSessionController.java`

| Metod | Path | Rol | Request Body | Response Body | Status |
|---|---|---|---|---|---|
| POST | `/api/v1/sales/chat-sessions` | ADMIN/SYS_ADMIN/SALES_CRM | `ChatSessionRequest{userId:UUID?, leadId:UUID?, channel:string?(max30), messages:List<Map>?(max2000)}` — **heç bir sahə məcburi deyil** | `ChatSessionResponse` | 201, 400, 403 |
| GET | `/api/v1/sales/chat-sessions` | ADMIN/SYS_ADMIN/SALES_CRM | — | `List<ChatSessionResponse>` | 200, 403 |
| GET | `/api/v1/sales/chat-sessions/{id}` | ADMIN/SYS_ADMIN/SALES_CRM | — (`id:UUID`) | `ChatSessionResponse` | 200, 403, 404 |
| PUT | `/api/v1/sales/chat-sessions/{id}` | ADMIN/SYS_ADMIN/SALES_CRM | `ChatSessionRequest` | `ChatSessionResponse` | 200, 400, 403, 404 |
| PATCH | `/api/v1/sales/chat-sessions/{id}` | ADMIN/SYS_ADMIN/SALES_CRM | `ChatSessionRequest` | `ChatSessionResponse` | 200, 400, 403, 404 |
| DELETE | `/api/v1/sales/chat-sessions/{id}` | ADMIN/SYS_ADMIN/SALES_CRM | — | boş | 204, 403, 404 |

`ChatSessionResponse`: `id:UUID`, `userId:UUID?`, `leadId:UUID?`, `channel:string?`, `messages:List<Map<string,object>>`, `startedAt:Instant`, `endedAt:Instant?`.

---

### 2.14 ContactSubmissionController — `/api/v1/sales/contact-submissions` (hamısı ADMIN/SYS_ADMIN/SALES_CRM)
Fayl: `controller/crm/ContactSubmissionController.java`

| Metod | Path | Rol | Request Body | Response Body | Status |
|---|---|---|---|---|---|
| POST | `/api/v1/sales/contact-submissions` | ADMIN/SYS_ADMIN/SALES_CRM | `ContactSubmissionRequest{leadId:UUID?, type:SubmissionType, courseId:UUID?, fullName:string?(2-150), email:string?(@Email), phone:string?(pattern), message:string?(max4000), preferredTime:Instant?(gələcək/indiki)}` | `ContactSubmissionResponse` | 201, 400, 403 |
| GET | `/api/v1/sales/contact-submissions` | ADMIN/SYS_ADMIN/SALES_CRM | — | `List<ContactSubmissionResponse>` | 200, 403 |
| GET | `/api/v1/sales/contact-submissions/{id}` | ADMIN/SYS_ADMIN/SALES_CRM | — (`id:UUID`) | `ContactSubmissionResponse` | 200, 403, 404 |
| PUT | `/api/v1/sales/contact-submissions/{id}` | ADMIN/SYS_ADMIN/SALES_CRM | `ContactSubmissionRequest` | `ContactSubmissionResponse` | 200, 400, 403, 404 |
| PATCH | `/api/v1/sales/contact-submissions/{id}` | ADMIN/SYS_ADMIN/SALES_CRM | `ContactSubmissionRequest` | `ContactSubmissionResponse` | 200, 400, 403, 404 |
| DELETE | `/api/v1/sales/contact-submissions/{id}` | ADMIN/SYS_ADMIN/SALES_CRM | — | boş | 204, 403, 404 |

`ContactSubmissionResponse`: `id:UUID`, `leadId:UUID?`, `type:SubmissionType`, `courseId:UUID?`, `fullName:string?`, `email:string?`, `phone:string?`, `message:string?`, `preferredTime:Instant?`, **`status:string`** (⚠️ enum deyil, plain string — `LeadStatus`/başqa enum-la eyni deyil, kodda dəqiq hansı dəyərləri aldığı tapılmadı, `// TƏSDİQLƏNMƏYİB`), `submittedAt:Instant`.

**QEYD:** Bu endpoint tipik olaraq public "bizimlə əlaqə" formu kimi görünür, amma path `ADMIN/SYSTEM_ADMIN/SALES_CRM`-only-dir (`/api/v1/sales/**` qaydasına düşür) — public "contact form submit" üçün ayrıca açıq endpoint bu repo-da **tapılmadı**. Bu, public marketinq saytının bu formu göndərə bilməyəcəyi mənasına gəlir, `// TƏSDİQLƏNMƏYİB — bəlkə qəsdəndir (yalnız CRM daxili qeyd), bəlkə əskikdir`.

---

### 2.15 LeadController — `/api/v1/sales/leads` (hamısı ADMIN/SYS_ADMIN/SALES_CRM)
Fayl: `controller/crm/LeadController.java`

| Metod | Path | Rol | Request Body | Response Body | Status |
|---|---|---|---|---|---|
| POST | `/api/v1/sales/leads` | ADMIN/SYS_ADMIN/SALES_CRM | `LeadRequest{fullName:string?(2-150), email:string?(@Email), phone:string?(pattern), courseId:UUID?, source:LeadSource, assignedTo:UUID?, consentVersion:string?(max20)}` | `LeadResponse` | 201, 400, 403 |
| GET | `/api/v1/sales/leads` | ADMIN/SYS_ADMIN/SALES_CRM | — | `List<LeadResponse>` | 200, 403 |
| GET | `/api/v1/sales/leads/{id}` | ADMIN/SYS_ADMIN/SALES_CRM | — (`id:UUID`) | `LeadResponse` | 200, 403, 404 |
| PUT | `/api/v1/sales/leads/{id}` | ADMIN/SYS_ADMIN/SALES_CRM | `LeadRequest` | `LeadResponse` | 200, 400, 403, 404 |
| PATCH | `/api/v1/sales/leads/{id}` | ADMIN/SYS_ADMIN/SALES_CRM | `LeadRequest` | `LeadResponse` | 200, 400, 403, 404 |
| DELETE | `/api/v1/sales/leads/{id}` | ADMIN/SYS_ADMIN/SALES_CRM | — | boş | 204, 403, 404 |

`LeadResponse`: `id:UUID`, `fullName:string?`, `email:string?`, `phone:string?`, `courseId:UUID?`, `source:LeadSource`, `status:LeadStatus`, `assignedTo:UUID?`, `consentVersion:string?`, `consentGivenAt:Instant?`, `duplicateOfLeadId:UUID?`, `activityLog:List<Map<string,object>>`, `createdAt:Instant`, `updatedAt:Instant`.

---

### 2.16 OAuthAccountController — `/api/v1/oauth-accounts` (hamısı `ADMIN`,`SYSTEM_ADMIN`)
Fayl: `controller/identity/OAuthAccountController.java`

| Metod | Path | Rol | Request Body | Response Body | Status |
|---|---|---|---|---|---|
| POST | `/api/v1/oauth-accounts` | ADMIN/SYS_ADMIN | `OAuthAccountRequest{userId:UUID, provider:OAuthProvider, providerUserId:string(max255), accessTokenEnc:string?(max4000), refreshTokenEnc:string?(max4000)}` | `OAuthAccountResponse` | 201, 400, 403, 409 |
| GET | `/api/v1/oauth-accounts` | ADMIN/SYS_ADMIN | — | `List<OAuthAccountResponse>` | 200, 403 |
| GET | `/api/v1/oauth-accounts/{id}` | ADMIN/SYS_ADMIN | — (`id:long`) | `OAuthAccountResponse` | 200, 403, 404 |
| PUT | `/api/v1/oauth-accounts/{id}` | ADMIN/SYS_ADMIN | `OAuthAccountRequest` | `OAuthAccountResponse` | 200, 400, 403, 404 |
| PATCH | `/api/v1/oauth-accounts/{id}` | ADMIN/SYS_ADMIN | `OAuthAccountRequest` | `OAuthAccountResponse` | 200, 400, 403, 404 |
| DELETE | `/api/v1/oauth-accounts/{id}` | ADMIN/SYS_ADMIN | — | boş | 204, 403, 404 |

`OAuthAccountResponse`: `id:long`, `userId:UUID`, `provider:OAuthProvider`, `providerUserId:string`, `linkedAt:Instant`.

**QEYD:** Real Google/GitHub/LinkedIn OAuth login axını (redirect/callback URL-ləri) bu repo-da **tapılmadı** — bu, sırf CRUD-dur (əl ilə OAuth hesab qeydi əlavə/redaktə/silmək üçün). `spring-boot-starter-oauth2-client` kimi asılılıq da pom.xml-də yoxdur.

---

### 2.17 SessionController — `/api/v1/sessions` (hamısı `ADMIN`,`SYSTEM_ADMIN`)
Fayl: `controller/identity/SessionController.java`. **QEYD:** Bu, `/api/v1/auth/login`-in yaratdığı `Session` entity-lərinin (refresh token, password-reset, email-verify token-lərinin hash-ləndiyi cədvəl) əl ilə idarə interfeysidir — real istifadəçi "sessiyaları"nı deyil, `identity.sessions` DB cədvəlini təmsil edir.

| Metod | Path | Rol | Request Body | Response Body | Status |
|---|---|---|---|---|---|
| POST | `/api/v1/sessions` | ADMIN/SYS_ADMIN | `SessionRequest{userId:UUID, type:SessionType?, tokenHash:string(16-255,base64/hex-oxşar), ipAddress:string?(IPv4/IPv6), userAgent:string?(max1000), expiresAt:Instant(gələcək)}` | `SessionResponse` | 201, 400, 403 |
| GET | `/api/v1/sessions` | ADMIN/SYS_ADMIN | — | `List<SessionResponse>` | 200, 403 |
| GET | `/api/v1/sessions/{id}` | ADMIN/SYS_ADMIN | — (`id:UUID`) | `SessionResponse` | 200, 403, 404 |
| PUT | `/api/v1/sessions/{id}` | ADMIN/SYS_ADMIN | `SessionRequest` | `SessionResponse` | 200, 400, 403, 404 |
| PATCH | `/api/v1/sessions/{id}` | ADMIN/SYS_ADMIN | `SessionRequest` | `SessionResponse` | 200, 400, 403, 404 |
| DELETE | `/api/v1/sessions/{id}` | ADMIN/SYS_ADMIN | — | boş | 204, 403, 404 |

`SessionResponse`: `id:UUID`, `userId:UUID`, `type:SessionType`, `ipAddress:string?`, `userAgent:string?`, `issuedAt:Instant`, `expiresAt:Instant`, `usedAt:Instant?`, `revokedAt:Instant?`. (Xam token/tokenHash cavabda **qaytarılmır** — yalnız request-də qəbul olunur.)

---

### 2.18 NotificationController — `/api/v1/notifications` (hamısı `ADMIN`,`SYSTEM_ADMIN`)
Fayl: `controller/notify/NotificationController.java`. **QEYD:** İstifadəçiyə görə filtrlənən "mənim bildirişlərim" (`/me`) endpoint-i **YOXDUR** — yalnız admin tam CRUD.

| Metod | Path | Rol | Request Body | Response Body | Status |
|---|---|---|---|---|---|
| POST | `/api/v1/notifications` | ADMIN/SYS_ADMIN | `NotificationRequest{userId:UUID, type:string(max60,snake_case), channel:NotificationChannel, payload:Map<string,object>?(max50)}` | `NotificationResponse` | 201, 400, 403 |
| GET | `/api/v1/notifications` | ADMIN/SYS_ADMIN | — | `List<NotificationResponse>` | 200, 403 |
| GET | `/api/v1/notifications/{id}` | ADMIN/SYS_ADMIN | — (`id:UUID`) | `NotificationResponse` | 200, 403, 404 |
| PUT | `/api/v1/notifications/{id}` | ADMIN/SYS_ADMIN | `NotificationRequest` | `NotificationResponse` | 200, 400, 403, 404 |
| PATCH | `/api/v1/notifications/{id}` | ADMIN/SYS_ADMIN | `NotificationRequest` | `NotificationResponse` | 200, 400, 403, 404 |
| DELETE | `/api/v1/notifications/{id}` | ADMIN/SYS_ADMIN | — | boş | 204, 403, 404 |

`NotificationResponse`: `id:UUID`, `userId:UUID`, `type:string`, `channel:NotificationChannel`, `payload:Map<string,object>`, `status:NotificationStatus`, `sentAt:Instant?`, `readAt:Instant?`, `createdAt:Instant`.

---

### 2.19 KbArticleController — `/api/v1/kb-articles` (hamısı CM/ADMIN/SYS_ADMIN)
Fayl: `controller/ai/KbArticleController.java`

| Metod | Path | Rol | Request Body | Response Body | Status |
|---|---|---|---|---|---|
| POST | `/api/v1/kb-articles` | CM/ADMIN/SYS_ADMIN | `KbArticleRequest{sourceType:string(max30,lowercase+"_"), sourceRefId:string?(max255), title:string?(max250), content:string(max100000), active:boolean?}` | `KbArticleResponse` | 201, 400, 403 |
| GET | `/api/v1/kb-articles` | CM/ADMIN/SYS_ADMIN | — | `List<KbArticleResponse>` | 200, 403 |
| GET | `/api/v1/kb-articles/{id}` | CM/ADMIN/SYS_ADMIN | — (`id:UUID`) | `KbArticleResponse` | 200, 403, 404 |
| PUT | `/api/v1/kb-articles/{id}` | CM/ADMIN/SYS_ADMIN | `KbArticleRequest` | `KbArticleResponse` | 200, 400, 403, 404 |
| PATCH | `/api/v1/kb-articles/{id}` | CM/ADMIN/SYS_ADMIN | `KbArticleRequest` | `KbArticleResponse` | 200, 400, 403, 404 |
| DELETE | `/api/v1/kb-articles/{id}` | CM/ADMIN/SYS_ADMIN | — | boş | 204, 403, 404 |

`KbArticleResponse`: `id:UUID`, `sourceType:string`, `sourceRefId:string?`, `title:string?`, `content:string`, `active:boolean`, `updatedAt:Instant`.

**QEYD:** Bu, AI-çatbot üçün "bilgi bazası" görünür, amma controller-də heç bir AI/embedding/vector-search çağırışı yoxdur — sırf CRUD. `pgvector`/`vector` PostgreSQL extension-ının V1 migration-da qeyd olunduğunu gördük (`// TƏSDİQLƏNMƏYİB` — bu cədvəldə həqiqətən vector sütunu olub-olmadığı entity-də görünmür, entity faylı bu araşdırmada açılmadı).

---

### 2.20 CourseReviewController — `/api/v1/course-reviews`
Fayl: `controller/outcomes/CourseReviewController.java`. **Path-based qayda YOXDUR** (`anyRequest().authenticated()`-ə düşür). İcazə **servis səviyyəsində** (`service/outcomes/CourseReviewService.java`):

- **STAFF_ROLES = `ADMIN`, `SYSTEM_ADMIN`, `CONTENT_MANAGER`** — bu rollar istənilən rəyi redaktə/silə bilər.
- Digər rollar `create`-də yalnız `request.userId() == öz id-i` ola bilər; `update`/`patch`/`delete`-də yalnız **öz** rəyini dəyişə bilər.

| Metod | Path | Rol/icazə | Request Body | Response Body | Status |
|---|---|---|---|---|---|
| POST | `/api/v1/course-reviews` | authenticated; qeyri-staff yalnız özü üçün | `CourseReviewRequest{courseId:UUID, userId:UUID, enrollmentId:UUID?, rating:short(1-5), comment:string?(max4000)}` | `CourseReviewResponse` | 201, 400, 403 (özündən başqası adından yazmaq) |
| GET | `/api/v1/course-reviews` | **hər hansı authenticated istifadəçi** (sahiblik filtri YOXDUR — hamı bütün rəyləri görür) | — | `List<CourseReviewResponse>` | 200, 401 |
| GET | `/api/v1/course-reviews/{id}` | authenticated (istənilən) | — (`id:long`) | `CourseReviewResponse` | 200, 401, 404 |
| PUT | `/api/v1/course-reviews/{id}` | staff **və ya** rəyin sahibi | `CourseReviewRequest` (tam) | `CourseReviewResponse` | 200, 400, 403, 404 |
| PATCH | `/api/v1/course-reviews/{id}` | staff **və ya** rəyin sahibi | `CourseReviewRequest` (qismən) | `CourseReviewResponse` | 200, 400, 403, 404 |
| DELETE | `/api/v1/course-reviews/{id}` | staff **və ya** rəyin sahibi | — | boş | 204, 403, 404 |

`CourseReviewResponse`: `id:long`, `courseId:UUID`, `userId:UUID`, `enrollmentId:UUID?`, `rating:short`, `comment:string?`, `published:boolean`, `moderatedBy:UUID?`, `aiSentiment:Map<string,object>?`, `createdAt:Instant`.

**QEYD:** `create`-də hər zaman `published=false` təyin olunur (moderasiya gözləyir). `CourseReviewService.setPublished(id, published, moderatorId)` metodu var (moderasiya üçün), amma **heç bir controller endpoint-i bu metoda bağlı deyil** — yəni rəyi "publish et" HTTP API-si bu repo-da **tapılmadı** (`// TƏSDİQLƏNMƏYİB`).

---

### 2.21 GraduateOutcomeController — `/api/v1/graduate-outcomes` (hamısı CM/ADMIN/SYS_ADMIN)
Fayl: `controller/outcomes/GraduateOutcomeController.java`

| Metod | Path | Rol | Request Body | Response Body | Status |
|---|---|---|---|---|---|
| POST | `/api/v1/graduate-outcomes` | CM/ADMIN/SYS_ADMIN | `GraduateOutcomeRequest{userId:UUID, courseId:UUID, companyName:string?(2-150), jobTitle:string?(2-150), employedAt:LocalDate?(keçmiş/indiki), salaryBand:string?(max50), publicStory:boolean?, storyText:string?(max8000)}` | `GraduateOutcomeResponse` | 201, 400, 403 |
| GET | `/api/v1/graduate-outcomes` | CM/ADMIN/SYS_ADMIN | — | `List<GraduateOutcomeResponse>` | 200, 403 |
| GET | `/api/v1/graduate-outcomes/{id}` | CM/ADMIN/SYS_ADMIN | — (`id:long`) | `GraduateOutcomeResponse` | 200, 403, 404 |
| PUT | `/api/v1/graduate-outcomes/{id}` | CM/ADMIN/SYS_ADMIN | `GraduateOutcomeRequest` | `GraduateOutcomeResponse` | 200, 400, 403, 404 |
| PATCH | `/api/v1/graduate-outcomes/{id}` | CM/ADMIN/SYS_ADMIN | `GraduateOutcomeRequest` | `GraduateOutcomeResponse` | 200, 400, 403, 404 |
| DELETE | `/api/v1/graduate-outcomes/{id}` | CM/ADMIN/SYS_ADMIN | — | boş | 204, 403, 404 |

`GraduateOutcomeResponse`: `id:long`, `userId:UUID`, `courseId:UUID`, `companyName:string?`, `jobTitle:string?`, `employedAt:LocalDate?`, `salaryBand:string?`, `publicStory:boolean?`, `storyText:string?`, `createdAt:Instant`.

**QEYD:** "Public uğur hekayələri" mənasına baxmayaraq, public GET yoxdur — yalnız CONTENT_MANAGER/ADMIN.

---

### 2.22 AuditLogController — `/api/v1/admin/audit-logs` (hamısı `ADMIN`,`SYSTEM_ADMIN`)
Fayl: `controller/platform/AuditLogController.java`

| Metod | Path | Rol | Request Body | Response Body | Status |
|---|---|---|---|---|---|
| POST | `/api/v1/admin/audit-logs` | ADMIN/SYS_ADMIN | `AuditLogRequest{actorId:UUID?, action:string(max80,lowercase snake/dot-case), entityType:string(max60), entityId:string(max255), beforeState:Map<string,object>?(max100), afterState:Map<string,object>?(max100), ipAddress:string?(IPv4/IPv6)}` | `AuditLogResponse` | 201, 400, 403 |
| GET | `/api/v1/admin/audit-logs` | ADMIN/SYS_ADMIN | — | `List<AuditLogResponse>` | 200, 403 |
| GET | `/api/v1/admin/audit-logs/{id}` | ADMIN/SYS_ADMIN | — (`id:long`) | `AuditLogResponse` | 200, 403, 404 |
| PUT | `/api/v1/admin/audit-logs/{id}` | ADMIN/SYS_ADMIN | `AuditLogRequest` | `AuditLogResponse` | 200, 400, 403, 404 |
| PATCH | `/api/v1/admin/audit-logs/{id}` | ADMIN/SYS_ADMIN | `AuditLogRequest` | `AuditLogResponse` | 200, 400, 403, 404 |
| DELETE | `/api/v1/admin/audit-logs/{id}` | ADMIN/SYS_ADMIN | — | boş | 204, 403, 404 |

`AuditLogResponse`: `id:long`, `actorId:UUID?`, `action:string`, `entityType:string`, `entityId:string`, `beforeState:Map<string,object>?`, `afterState:Map<string,object>?`, `traceId:UUID?`, `ipAddress:string?`, `createdAt:Instant`.

---

### 2.23 TestMailController — `/test` (dev/test məqsədli, path-based qayda YOXDUR → `anyRequest().authenticated()`)
Fayl: `controller/test/TestMailController.java`

| Metod | Path | Rol | Request Body | Response Body | Status |
|---|---|---|---|---|---|
| POST | `/test/mail` | authenticated (istənilən rol) | yoxdur | **düz string** `"Mail sent"` (JSON deyil, `Content-Type: text/plain`) — DTO/envelope YOXDUR | 200 |

**QEYD:** Bu, MailHog/SMTP test etmək üçün development yardımçı endpoint-idir (kod git-də `staged` yeni fayl kimi görünür), sabit alıcı ünvanı (`test@gmail.com`) hardcode edilib. Production-da istifadə üçün nəzərdə tutulmayıb.

---

### 2.x Ümumi qeydlər (bütün endpoint-lər üçün)
- **Envelope YOXDUR.** Uğurlu cavablar həmişə düz DTO obyekti (`{...}`) və ya düz massiv (`[...]`) qaytarır — `{"data": ..., "meta": ...}` kimi sarma heç yerdə istifadə olunmur. Yeganə istisna: paginasiya olunan iki endpoint (`GET /api/v1/courses`, `GET /api/v1/users`) Spring Data-nın öz `Page<T>` obyektini qaytarır (bax §2.4).
- **Pagination yalnız 2 endpoint-də var**: `GET /api/v1/courses` və `GET /api/v1/users`. Digər bütün `findAll`/`GET (collection)` endpoint-ləri **paginasiyasız düz `List<T>` massivi** qaytarır (potensial performans riski böyük cədvəllərdə — bu barədə kodda heç bir limit/qeyd yoxdur).
- `POST` uğurlu yaratma zamanı **`201 Created`** + `Location` header-i qaytarır (`ResponseEntity.created(locationOf(...))`), body-də yaradılan resurs.
- `DELETE` və bir çox `Void` cavablı `POST` (logout, forgot-password, verify-email, resend-verification, change-password) **`204 No Content`** qaytarır, body yoxdur.
- Bütün `@RequestBody` DTO-ları Java **record**-durlar (immutable).
- `Map<String,Object>` tipli sahələr (`profile`, `content`, `data`, `payload`, `installments`, `applications`, `activityLog`, `messages`, `aiSentiment`, `beforeState`/`afterState`) sxemsiz sərbəst JSON-dur — kodda onların daxili strukturuna dair heç bir DTO/validasiya tapılmadı (C# tərəfində `Dictionary<string, object>` və ya `JsonElement` kimi modelləşdirilməlidir).

---

## 3. Xəta (Error) Formatı

Global handler: `src/main/java/az/demo/NexoraAcademy/exception/GlobalExceptionHandler.java` (`@RestControllerAdvice`). Bütün body-lər `ErrorResponse` record-una əsaslanır:
```java
record ErrorResponse(Instant timestamp, int status, String error, String message, String path, Map<String,String> errors)
```

**QEYD — İKİ FƏRQLİ JSON FORMASI VAR:**
1. `GlobalExceptionHandler`-dən keçən xətalar (400/404/409/500 və s., controller/servis daxilində atılanlar) — həmişə **6 açar** olur, `errors` açarı **mövcuddur** (dəyəri `null` və ya map).
2. Spring Security filter zəncirindən (dispatch-dən **əvvəl**) gələn 401/403 (`JwtAuthenticationEntryPoint`/`CustomAccessDeniedHandler`) — **yalnız 5 açar** olur, `errors` açarı **heç yoxdur** (JSON-da key özü yoxdur, `null` da deyil — tam yoxdur). BFF tərəfi `errors`-u optional/nullable kimi deserialize etməlidir.

Canlı nümunələr (real sınaqdan, `curl` ilə):

**400 — validasiya xətası** (`@Valid`/`@Validated` uğursuz, `MethodArgumentNotValidException`):
```json
{"timestamp":"2026-07-22T06:55:01.729382400Z","status":400,"error":"Bad Request","message":"Validation failed","path":"/api/v1/auth/register","errors":{"fullName":"must not be blank","email":"must not be blank","password":"must not be blank"}}
```
`errors` — `Map<sahə adı, mesaj>` formasıdır (`FieldError.getField()` → `FieldError.getDefaultMessage()`). Bir sahədə birdən çox pozulmuş qayda varsa, yalnız **sonuncusu** map-də qalır (map açar-təkrarında üzərinə yazılır — Bean Validation-ın sırası deterministik deyil).

**400 — malformed JSON body** (`HttpMessageNotReadableException`):
```json
{"timestamp":"2026-07-22T06:55:15.569588200Z","status":400,"error":"Bad Request","message":"Malformed or missing request body","path":"/api/v1/auth/login","errors":null}
```

**400 — path variable tip uyğunsuzluğu** (`MethodArgumentTypeMismatchException`, məs. UUID gözlənilən yerdə string):
```json
{"timestamp":"2026-07-22T06:55:15.660363600Z","status":400,"error":"Bad Request","message":"Parameter 'id' has an invalid value: not-a-uuid","path":"/api/v1/courses/not-a-uuid","errors":null}
```

**401 — autentifikasiya yoxdur/etibarsız** (`JwtAuthenticationEntryPoint`, filter zəncirindən):
```json
{"timestamp":"2026-07-22T06:55:01.835531Z","status":401,"error":"Unauthorized","message":"Full authentication is required to access this resource","path":"/api/v1/users/me"}
```
(`errors` açarı YOXDUR — yuxarı bax.) Login-də yanlış email/parol da 401-dir, amma bu `GlobalExceptionHandler.handleApiException`-dan keçir (`AuthService.login()` Spring Security-nin `BadCredentialsException`-ını tutub öz `InvalidCredentialsException`-una çevirir) və `errors:null` ilə **6 açarlı** formadadır:
```json
{"timestamp":"...","status":401,"error":"Unauthorized","message":"Invalid email or password","path":"/api/v1/auth/login","errors":null}
```
Qeyd: `GlobalExceptionHandler.handleBadCredentials` (`BadCredentialsException` üçün) kodda mövcuddur, amma `authenticationManager.authenticate()` yalnız `AuthService.login()`-də çağırılır və orada bu exception həmişə tutulub `InvalidCredentialsException`-a çevrilir — yəni bu handler-in hazırkı axınlarda faktiki işə düşdüyü yer tapılmadı (`// TƏSDİQLƏNMƏYİB` — ölü kod ola bilər, gələcək autentifikasiya yolları üçün saxlanılıb).

**403 — icazə yoxdur** (path-based, `CustomAccessDeniedHandler`, filter zəncirindən):
```json
{"timestamp":"2026-07-22T06:55:01.952550100Z","status":403,"error":"Forbidden","message":"Access Denied","path":"/api/v1/payments"}
```
(`errors` açarı YOXDUR.) Servis-səviyyəli 403-lər (`EnrollmentService`/`CourseReviewService`-dəki `AccessDeniedException`) isə `GlobalExceptionHandler.handleAccessDenied`-dan keçir və **6 açarlı** formadadır, `message` daha təsviridir, məs.:
```json
{"timestamp":"...","status":403,"error":"Forbidden","message":"Only staff may list all enrollments","path":"/api/v1/enrollments","errors":null}
```

**404 — resurs tapılmadı** (`ResourceNotFoundException`):
```json
{"timestamp":"2026-07-22T06:55:01.781434900Z","status":404,"error":"Not Found","message":"Course not found with id: 00000000-0000-0000-0000-000000000000","path":"/api/v1/courses/00000000-0000-0000-0000-000000000000","errors":null}
```

**409 — dublikat/konflikt** (`DuplicateResourceException` və ya `InvalidStateException`):
```json
{"timestamp":"2026-07-22T06:55:15.500846500Z","status":409,"error":"Conflict","message":"User already exists with email: apitest1@example.com","path":"/api/v1/auth/register","errors":null}
```
`InvalidStateException` da eyni formada (məs. "already cancelled", "course group is full" mesajları), **status 409** (400/422 deyil — kod belə seçib).

**409 — DB constraint pozuntusu** (`DataIntegrityViolationException`, məs. PATCH-də format düzgün ancaq DB-level check constraint pozulub):
```json
{"timestamp":"...","status":409,"error":"Conflict","message":"The request conflicts with existing data","path":"/api/v1/...","errors":null}
```
Bu halda **əsl səbəb gizlədilir** (yalnız server logunda, `log.warn`) — client-ə generic mesaj gedir.

**422 — bu status kodu heç yerdə istifadə olunmur.** Bütün validasiya xətaları **400** ilə qayıdır (`@RestControllerAdvice`-də 422 üçün handler tapılmadı).

**500 — gözlənilməz xəta** (`Exception.class` catch-all):
```json
{"timestamp":"...","status":500,"error":"Internal Server Error","message":"An unexpected error occurred","path":"...","errors":null}
```
Real exception detalı/stack-trace client-ə **heç vaxt** getmir (yalnız server logunda `log.error`).

### 3.1 `GlobalExceptionHandler`-də map olunan bütün exception tipləri
| Exception | HTTP status | Qeyd |
|---|---|---|
| `ApiException` (abstract, alt-sinifləri özündə `HttpStatus` daşıyır) | `ex.getStatus()` | `DuplicateResourceException`(409), `ResourceNotFoundException`(404), `InvalidStateException`(409), `InvalidCredentialsException`(401 — konstruktorda `HttpStatus.UNAUTHORIZED` təsdiqləndi), `InvalidTokenException`(401 — konstruktorda `HttpStatus.UNAUTHORIZED` təsdiqləndi, JWT/refresh-token etibarsız/vaxtı bitmiş/ləğv olunmuş hallarında) |
| `MethodArgumentNotValidException` | 400 | `@Valid`/`@Validated` sahə xətaları |
| `ConstraintViolationException` | 400 | metod parametri səviyyəsində (`@RequestParam`/`@PathVariable` üzərində) constraint pozuntusu |
| `HttpMessageNotReadableException` | 400 | boş/pozuq JSON body |
| `MethodArgumentTypeMismatchException` | 400 | path/query parametr tip uyğunsuzluğu |
| `DataIntegrityViolationException` | 409 | DB constraint (unique, check və s.) |
| `BadCredentialsException` (Spring Security) | 401 | login zamanı yanlış parol |
| `AccessDeniedException` (Spring Security, servis-səviyyəli) | 403 | `EnrollmentService`/`CourseReviewService`-dəki əl ilə atılan icazə xətaları |
| `Exception` (hər şey) | 500 | catch-all |

---

## 4. CORS və Şəbəkə

### 4.1 CORS
Fayl: `config/CorsConfig.java` + `config/CorsProperties.java` (`@ConfigurationProperties(prefix = "app.cors")`, sahə: `allowedOrigins: List<String>`).

- **`app.cors.allowed-origins` heç bir `application.yml`/`application-dev.yml`/`application-prod.yml`/`.env.example` faylında təyin olunmayıb** — `CorsProperties.allowedOrigins` default olaraq **boş siyahı** (`new ArrayList<>()`) qalır. Yəni hazırkı konfiqurasiya ilə **HEÇ BİR origin-ə CORS icazəsi yoxdur** (`Access-Control-Allow-Origin` heç vaxt uyğun gəlməyəcək) — bu, ya qəsdən belə buraxılıb (hər mühit üçün ayrıca env variable ilə doldurulacaq) ya da hələ tamamlanmamış konfiqurasiyadır. `// TƏSDİQLƏNMƏYİB — CORS_ALLOWED_ORIGINS (və ya bənzər) environment variable-ın harada təyin ediləcəyi aydınlaşdırılmalıdır`.
- Icazə verilən metodlar: `GET, POST, PUT, PATCH, DELETE, OPTIONS`.
- Icazə verilən header-lər: `*` (hamısı).
- Expose olunan header-lər: `Authorization`, `Content-Disposition`.
- `allowCredentials: true`.
- `maxAge: 3600` saniyə.
- Tətbiq olunan path: `/**` (bütün endpoint-lər).
- `SecurityConfig.securityFilterChain()`-də `.cors(Customizer.withDefaults())` çağırılır, yəni yuxarıdakı `CorsConfigurationSource` bean-i Spring Security tərəfindən avtomatik istifadə olunur.

### 4.2 Port / base path
- Port: **8081** (`server.port: 8081`, `application.yml:2`). `.env`/`SERVER_PORT` ilə override edilə bilən konfiqurasiya **tapılmadı** (hardcode-a yaxın, birbaşa `application.yml`-dədir, env variable-a bağlanmayıb).
- Global `context-path`/`/api` prefiksi Spring səviyyəsində **YOXDUR** — hər controller öz `@RequestMapping`-ində tam yolu yazır (`/api/v1/...`). Bütün domen endpoint-ləri konvensiya olaraq `/api/v1/` ilə başlayır, `TestMailController` isə istisnadır (`/test/mail`).
- Bazadakı `default_schema: identity` və Flyway `schemas: identity,catalog,academics,billing,outcomes,crm,cms,ai,notify,platform,analytics` — bu, API path-lərinə təsir etmir, sırf PostgreSQL schema adlarıdır.

### 4.3 Swagger / OpenAPI
- Asılılıq: `springdoc-openapi-starter-webmvc-ui:2.7.0` (`pom.xml`).
- `application.yml`-də springdoc üçün heç bir custom konfiqurasiya (path override və s.) tapılmadı → **springdoc-un default path-ləri** işləyir:
  - OpenAPI JSON: `GET /v3/api-docs` (canlı test edilib, **200** qaytarır)
  - Swagger UI: `GET /swagger-ui/index.html` (canlı test edilib, **200** qaytarır; `/swagger-ui.html` redirect kimi işləyə bilər — ayrıca test edilmədi)
  - YAML variant (`GET /v3/api-docs.yaml`) — canlı test edilib, **401 qaytarır** (aşağı bax, `permitAll` deyil).
- `/swagger-ui/**` və `/v3/api-docs/**` `SecurityConfig`-də `permitAll()`-dır (bax §2.0, qayda-4), **AMMA** bu Ant-pattern-lər `/v3/api-docs.yaml`-ı **əhatə etmir** (`/v3/api-docs/**` yalnız `/v3/api-docs` və altındakı `/` ilə ayrılan alt-path-lərə uyğun gəlir, `.yaml` suffiksli eyni-səviyyəli path-ə yox) — canlı test təsdiqlədi ki, `GET /v3/api-docs.yaml` **401** qaytarır (`anyRequest().authenticated()`-ə düşür). Yəni yalnız **JSON forması** (`/v3/api-docs`) publicdir, YAML forması authentication tələb edir.
- **Real `openapi.yaml`/`openapi.json` faylı repo-da tapılmadı** (statik commit edilmiş fayl yoxdur) — sənəd **runtime-da generasiya olunur**. Onu əldə etmək üçün:
  1. Tətbiqi işə salın (`./mvnw spring-boot:run` və ya cari işləyən instance, `localhost:8081`).
  2. `curl http://localhost:8081/v3/api-docs -o openapi.json` (JSON) və ya brauzerdə `http://localhost:8081/swagger-ui/index.html` açın.
  3. Build-zamanı statik fayl kimi çıxarmaq üçün ayrıca Maven plugin (məs. `springdoc-openapi-maven-plugin`) **pom.xml-də tapılmadı** — belə bir `mvn` goal-ı YOXDUR, yalnız runtime endpoint mövcuddur.

### 4.4 Mail / xarici servislər (əlaqəli infrastruktur)
- SMTP: `spring.mail.host/port` — dev-də MailHog (`docker-compose.yml`-də `nexora-mailhog` servisi, UI `http://localhost:8025`), env `MAIL_HOST`/`MAIL_PORT`/`MAIL_USERNAME`/`MAIL_PASSWORD`.
- PostgreSQL: `nexora-postgres` docker container, `DB_HOST`/`DB_PORT`/`DB_NAME`/`DB_USER`/`DB_PASSWORD` env variable-ları.
- Bunların BFF API-sinə birbaşa aidiyyəti yoxdur, sadəcə lokal dev mühitini tam qurmaq üçün qeyd olunur.

---

## 5. Fayl Yükləmə

- Mənbə kodda (`grep -r "MultipartFile\|multipart"`) **heç bir nəticə tapılmadı** — hazırkı controller-lərin heç birində fayl yükləmə endpoint-i **YOXDUR**.
- `pom.xml`-də **`cloudinary-http5:2.4.0`** asılılığı var (şəkil/media hosting SDK-sı), **amma bu asılılıq `src/main/java`-da heç yerdə import/istifadə olunmayıb** (`grep -r "Cloudinary"` — 0 nəticə). Yəni: kitabxana əlavə olunub, amma inteqrasiya (upload endpoint-i, `CloudinaryService` və s.) hələ **yazılmayıb**.
- Nəticə: `// TƏSDİQLƏNMƏYİB — fayl yükləmə funksionallığı planlaşdırılıb (Cloudinary asılılığından görünür) amma bu repo-da hələ implementasiya edilməyib`. BFF tərəfi hazırda fayl/şəkil upload üçün bu backend-ə güvənə bilməz — `InstructorRequest.photoUrl` və `CampaignRequest.bannerImageUrl` kimi sahələr yalnız **hazır URL string** qəbul edir (`@Pattern(regexp="^https?://.+")`), fayl bytes-i deyil.

---

## 6. Rol/İcazə Siyahısı

### 6.1 Rollar
Fayl: `entity/enums/UserRole.java`. Enum Java sabiti → DB-də saxlanılan `dbValue()` (Postgres native `ENUM`, `platform` schema-da, bax `V2__create_enum_types.sql`):

| Java sabiti | DB dəyəri | `ROLE_` authority-si (Spring Security) | SecurityConfig-də harda istifadə olunur |
|---|---|---|---|
| `GUEST` | `guest` | `ROLE_GUEST` | **Heç bir path qaydasında keçmir.** Kodda `UserRole.GUEST`-in təyin edildiyi yer (default rol, register axını və s.) tapılmadı — `// TƏSDİQLƏNMƏYİB`, yalnız enum sabiti olaraq mövcuddur. |
| `STUDENT` | `student` | `ROLE_STUDENT` | Default rol (`AuthService.register()` və `UserService.create()`-də `role==null` olanda). Xüsusi `hasAnyRole` qaydasında görünmür — yalnız `authenticated()` səviyyəli path-lərdə (öz `/me`, öz enrollment/review) işləyir. |
| `SALES_CRM` | `sales_crm` | `ROLE_SALES_CRM` | `/api/v1/sales/**` + `enrollments` üçün staff (servis-səviyyəli). |
| `CONTENT_MANAGER` | `content_manager` | `ROLE_CONTENT_MANAGER` | `/api/v1/content/**`, `courses`, `categories`, `instructors`, `course-groups`, `course-instructors`, `kb-articles`, `graduate-outcomes` + `course-reviews` üçün staff (servis-səviyyəli). |
| `ADMIN` | `admin` | `ROLE_ADMIN` | Demək olar bütün `hasAnyRole(...)` qaydalarında iştirak edir. |
| `SYSTEM_ADMIN` | `system_admin` | `ROLE_SYSTEM_ADMIN` | Hər yerdə `ADMIN` ilə eyni səviyyədə (kod şərhi: "SYSTEM_ADMIN da ADMIN-in bütün səlahiyyətlərinə malik olmalıdır") — **iyerarxiya deyil, hər iki rol ayrıca hər qaydaya əlavə olunub** (Spring Security role hierarchy mexanizmi istifadə olunmayıb, sadəcə `hasAnyRole("ADMIN","SYSTEM_ADMIN")` təkrarlanır). |

Bir istifadəçinin **yalnız bir rolu** ola bilər (`User.role` — tək sahə, `UserRole` enum, massiv/collection deyil) — çoxlu-rol dəstəyi YOXDUR.

### 6.2 Fine-grained permission sistemi
- Kodda ayrıca **`Permission` entity/enum tapılmadı** (əvvəlki commit-lərdə `entity/role/Permission.java` və `entity/role/Role.java` mövcud olub, git tarixçəsində "silinmiş fayl" kimi görünür — hazırkı strukturda **əvəzlənməyib**, sadəcə silinib).
- Hazırkı model: **tək-rol, path-based** icazə (bax §2.0, §6.1). Permission kodları/action-based fine-grained sistem bu repo-da **YOXDUR**.

---

## 7. Health/Status Endpoint

- `GET /actuator/health` — `permitAll()` (bax §2.0 qayda-5), autentifikasiya tələb olunmur.
- Asılılıq: `spring-boot-starter-actuator` (`pom.xml`-də kod şərhi ilə: "SecurityConfig permitAll-yə `/actuator/health` əlavə edib, amma bu asılılıq əvvəllər qoyulmamışdı — endpoint faktiki mövcud deyildi (404/500 verirdi)" — yəni bu, tarixən düzəldilmiş bir problemdir, hazırda işləyir).
- Canlı cavab nümunəsi (autentifikasiyasız, real test):
```json
{"groups":["liveness","readiness"],"status":"UP"}
```
- `management.health.mail.enabled: false` (`application.yml`) — SMTP qoşulmayanda `/actuator/health`-i `DOWN` göstərməsin deyə bilərəkdən söndürülüb (kod şərhi).
- `management.endpoints.web.exposure.include` heç bir yml-də tapılmadı → Spring Boot-un defaultu tətbiq olunur, yəni yalnız `health` (və broş şəkildə `info`, lakin `info` `SecurityConfig`-də permitAll deyil) web üzərindən açıqdır. Canlı test: `GET /actuator/info` → **401** (authenticated tələb olunur, çünki path-based qayda yoxdur → `anyRequest().authenticated()`), `GET /actuator` (kök) → **401**.
- Başqa "readiness/liveness" ayrıca endpoint-ləri mövcuddur (`GET /actuator/health/liveness`, `GET /actuator/health/readiness` — cavabdakı `"groups":["liveness","readiness"]` onların varlığını göstərir, Spring Boot-un default K8s probe dəstəyi), **AMMA canlı test təsdiqlədi ki, bunlar `permitAll` DEYİL — hər ikisi 401 qaytarır.** Səbəb: `SecurityConfig`-də matcher dəqiq `/actuator/health` string-idir (`/actuator/health/**` yox), ona görə alt-path-lər `anyRequest().authenticated()`-ə düşür. Yəni BFF/monitoring tərəfi yalnız **`/actuator/health`** (tam, alt-path olmadan) endpoint-ini autentifikasiyasız probe üçün istifadə edə bilər.

---

## Əlavə: Bu sənədin əhatə etmədiyi/aydınlaşdırılmalı sahələr
- `spring-boot-starter-data-jdbc` və `spring-boot-starter-data-jpa` **hər ikisi eyni vaxtda** `pom.xml`-dədir — build zamanı bir neçə repository interfeysi üçün "Could not safely identify store assignment" xəbərdarlığı yaranır (loglarda müşahidə edilib). API davranışına təsiri yoxdur, amma qeyd olunur.
- Entity-lərin tam sütun/constraint siyahısı (DB səviyyəsində) bu sənədə daxil edilməyib — yalnız DTO (API) səviyyəsi təsvir olunub. DB sxeması üçün `src/main/resources/db/migration/*.sql` migrasiya fayllarına baxılmalıdır.
- Event-driven yan-effektlər (`UserRegisteredEvent`, `EnrollmentConfirmedEvent`, `PaymentCompletedEvent`, `UserRoleChangedEvent`, `UserLoggedInEvent` və s.) mövcuddur (`event/` paketi) — bunlar email/notification/audit-log yaradır, amma sırf daxili proses olduğundan və API contract-a birbaşa təsir etmədiyindən bu sənədə daxil edilmədi.
- Rate limiting / throttling konfiqurasiyası bu araşdırmada **axtarılmadı** — BFF tərəfi bunu ayrıca yoxlamalıdır.
