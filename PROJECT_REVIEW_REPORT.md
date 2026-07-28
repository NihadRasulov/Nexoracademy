# NexoraAcademy — Tam Layihə Nəzərdən Keçirmə Hesabatı

**Tarix:** 2026-07-24
**Əhatə:** Bütün 211 Java faylı (entity, enum, DTO, validation, repository, service, controller, exception, logging, security, config), 5 `application*.yml`/`docker-compose*.yml`, 13 Flyway migration, `pom.xml`.
**Metod:** Hər paketin mənbə kodu birbaşa oxundu; `./mvnw clean compile` və `test-compile` işlədildi; tətbiq real Postgres-ə qarşı işə salınıb runtime warning-lər yoxlanıldı.

**Ümumi qiymət:** Layihə **yaxşı strukturlaşdırılıb** — təmiz təbəqələmə (controller → service → repository), tutarlı exception iyerarxiyası, düzgün validation qrupları, təhlükəsizlik-şüurlu OTP/JWT axını, constant-time imza müqayisələri. Kompilyasiya **xətasızdır**. Tapılan problemlərin əksəriyyəti **warning səviyyəsində** idi; 3 problem bu sessiyada düzəldildi, qalanları aşağıda qərar üçün sənədləşdirilib.

---

## 1. ✅ Düzəldilən Problemlər (bu sessiyada tətbiq olundu)

### 1.1 [WARNING → DÜZƏLDİLDİ] `data-jdbc` + `data-jpa` eyni vaxtda — "store ambiguity"
**Fayl:** `pom.xml`
**Problem:** `spring-boot-starter-data-jdbc` (istifadə olunmayan) və `spring-boot-starter-data-jpa` hər ikisi classpath-də idi. Bütün repository-lər `JpaRepository`-dir, heç yerdə Spring Data JDBC (`CrudRepository`/`JdbcTemplate`) istifadə olunmur (qrep ilə təsdiqləndi — 0 nəticə). İki modul birlikdə olanda Spring Data hər repository interfeysi üçün açılışda *"Could not safely identify store assignment for repository candidate"* xəbərdarlığı verirdi.
**Düzəliş:** `spring-boot-starter-data-jdbc` və `spring-boot-starter-data-jdbc-test` silindi (izahlı şərh əlavə olundu).
**Doğrulama:** Tətbiq 8082 portunda yenidən işə salındı → `Started NexoraAcademyApplication in 5.9s`, **heç bir "store assignment" warning-i yoxdur**. `test-compile` də keçir (silinən test-starter heç bir testdə istifadə olunmurdu).

### 1.2 [WARNING → DÜZƏLDİLDİ] Deprecated `org.springframework.lang.NonNull`
**Fayllar:** `security/JwtAuthenticationFilter.java`, `security/AuthRateLimitingFilter.java`, `security/PaymentCallbackSignatureFilter.java`, `logging/CorrelationIdFilter.java`
**Problem:** Spring Framework 7 (Boot 4.1) `org.springframework.lang.NonNull`-u deprecate edib, əvəzinə JSpecify tövsiyə olunur. Kompilyator hər fayl üçün deprecation warning verirdi.
**Düzəliş:** `org.jspecify.annotations.NonNull`-a keçirildi (JSpecify 1.0.0 artıq Spring-in transitive asılılığı kimi classpath-dədir — yoxlanıldı).
**Doğrulama:** `-Dmaven.compiler.showDeprecation=true` ilə compile → **heç bir deprecation warning-i yoxdur**.

### 1.3 [BUG → DÜZƏLDİLDİ] CourseReview rəyini başqasının adına keçirmək olurdu
**Fayl:** `service/outcomes/CourseReviewService.java` (`update()` və `patch()`)
**Problem:** `assertOwnerOrStaff(review)` rəyin **mövcud** sahibini yoxlayır — qeyri-staff istifadəçi öz rəyini redaktə edərkən `request.userId()`-ni **başqa istifadəçinin** id-si ilə göndərsə, yoxlama keçir (çünki hələ öz rəyidir), amma sonra `review.setUser(...)` rəyi həmin başqa istifadəçiyə təyin edirdi. Nəticədə tələbə öz rəyini başqasının adına "köçürə" bilərdi. (`create()`-də bu düzgün qorunurdu, `update`/`patch`-də yox.)
**Düzəliş:** `assertNotReassigningOwner(review, request.userId())` guard-ı əlavə olundu — yalnız staff başqa `userId` təyin edə bilər, qeyri-staff fərqli `userId` göndərsə `403 AccessDeniedException`.
**Doğrulama:** Compile keçir; məntiq `create()`-dəki mövcud qorunma nümunəsi ilə uyğundur.

---

## 2. ⚠️ Tapılan, Lakin Avtomatik Düzəldilməyən (qərar/iş tələb edir)

### 2.1 [CONCURRENCY — orta] Yer sayımında race condition
**Fayl:** `service/academics/EnrollmentService.java` (`occupySeat`/`releaseSeat`/`assertEnrollable`)
**Problem:** `reservedSeats` sahəsi read-modify-write pattern ilə yenilənir (oxu → `reservedSeats < totalSeats` yoxla → +1). Locking yoxdur. Eyni qrupun son yerinə **eyni anda** iki qeydiyyat gələrsə, hər ikisi `reservedSeats < totalSeats` yoxlamasını keçib yeri tuta bilər → **overselling** (totalSeats aşılır).
**Niyə avtomatik düzəltmədim:** Həlli entity/schema dəyişikliyi tələb edir — `CourseGroup`-a `@Version` (optimistic lock) əlavə etmək + migration + konflikt-retry məntiqi, ya da `SELECT ... FOR UPDATE` (pessimistic lock, `@Lock(LockModeType.PESTIMISTIC_WRITE)` repository metodu). Hər ikisi yük altında test tələb edir. **Tövsiyə:** `@Version private Long version;` `CourseGroup`-a + `V14` migration (`ALTER TABLE academics.course_groups ADD COLUMN version BIGINT NOT NULL DEFAULT 0`).

### 2.2 [WARNING — aşağı] İstifadə olunmayan `cloudinary-http5` asılılığı
**Fayl:** `pom.xml` (sətir ~72)
**Problem:** `com.cloudinary:cloudinary-http5:2.4.0` asılılığı var, amma `src/main/java`-da heç yerdə import/istifadə olunmur (0 nəticə). Fayl yükləmə funksionallığı planlaşdırılıb, amma implementasiya edilməyib.
**Niyə saxladım:** Gələcək fayl-yükləmə üçün qəsdən qoyulmuş ola bilər — silmək məhsul qərarıdır. İstifadə olunmayacaqsa, sil (build-i yüngülləşdirir). İstifadə olunacaqsa, `InstructorRequest.photoUrl`/`CampaignRequest.bannerImageUrl` üçün upload endpoint-i yaz.

### 2.3 [CODE SMELL — aşağı] Boş `OpenApiConfig` stub sinfi
**Fayl:** `config/OpenApiConfig.java`
**Problem:** Sinif tamamilə boşdur (`@Configuration` yoxdur, heç bir `@Bean` yoxdur) — ölü koddur. springdoc onsuz da default konfiqurasiya ilə işləyir.
**Niyə saxladım:** Zərərsizdir; gələcək OpenAPI özəlləşdirməsi (API başlığı/versiya/security scheme) üçün placeholder ola bilər. Lazım deyilsə, sil. Lazımdırsa, `@Configuration` + `@Bean OpenAPI customOpenApi()` ilə doldur (məs. `Authorize` düyməsi üçün JWT bearer scheme).

### 2.4 [DESIGN — məlum, qəbul edilmiş] Access token ləğv edilə bilmir
**Fayllar:** `service/AuthService.java` (`logout`), `service/JwtService.java`
**Problem:** `logout` yalnız refresh token-i DB-də ləğv edir. Access token stateless-dir — logout-dan sonra da 15 dəqiqəlik təbii ömrü bitənə qədər etibarlıdır (blacklist yoxdur).
**Status:** Qısa TTL (15 dəq) səbəbindən qəbul edilə biləndir; `API_CONTRACT.md`-də sənədləşdirilib. Tam ləğv lazımdırsa, Redis-əsaslı `jti` blacklist lazımdır. **Düzəliş tələb olunmur**, yalnız məlumat üçün.

### 2.5 [SECURITY — aşağı, qəbul edilmiş] OTP saltlanmamış SHA-256 ilə saxlanılır
**Fayl:** `service/AuthService.java` (`hash()`, `verifyOtp()`)
**Problem:** 6-rəqəmli OTP `SHA-256(otp)` kimi saxlanılır (salt yoxdur). DB-oxu icazəsi olan hücumçu 6-rəqəmli məkanı (yalnız 1M kombinasiya) rainbow-table ilə tərsinə çevirə bilər.
**Status:** Cəhd-limiti (5) + qısa TTL (10 dəq) real brute-force-u məhdudlaşdırır; bu, DB-oxu təhlükəsi olan ssenaridir (o zaman onsuz da böyük problem var). **Aşağı prioritet.** İstəsən, `hash(userId + otp)` ilə per-user salt əlavə et.

### 2.6 [DESIGN — public API boşluqları] Bax `FRONTEND_GUIDE.md` §8
Bunlar bug deyil, arxitektura seçimidir, amma qeyd üçün: public marketinq məzmunu (CMS/banner/FAQ), public contact-form, public course-group (tarix/yer sayı), self-service checkout, fayl yükləmə — heç biri public endpoint kimi mövcud deyil. Frontend komandası üçün `FRONTEND_GUIDE.md`-də ətraflı sadalanıb.

---

## 3. ✅ Yaxşı Vəziyyətdə Olan Sahələr (nəzərdən keçirildi, problem yoxdur)

| Sahə | Nəticə |
|---|---|
| **Entity mapping** | UUID/IDENTITY strategiyaları düzgün; `citext`/`jsonb`/`inet`/`uuid[]`/`bpchar` custom tipləri düzgün; enum-lar `@Converter(autoApply=true)` ilə DB `dbValue()` string-lərinə map olunur; `@CreationTimestamp`/`@UpdateTimestamp` düzgün. |
| **Enum ↔ migration uyğunluğu** | `V2__create_enum_types.sql`-dəki bütün PostgreSQL ENUM dəyərləri Java enum `dbValue()`-larıyla hərfi uyğundur; `V13` `login_otp` dəyərini düzgün əlavə edir. |
| **Validation** | `ValidationGroups` (OnCreate/Default) create vs patch fərqini düzgün idarə edir; `@DateRange` custom validator null-safe və reflection-əsaslı düzgün işləyir. |
| **Repository** | Hamısı `JpaRepository`; query-metod adları düzgün (`findFirstByUser_IdAndType...OrderByIssuedAtDesc` s.); `CourseSpecifications`/`UserSpecifications` null-safe dinamik filtr. |
| **Exception handling** | `GlobalExceptionHandler` bütün əsas exception tiplərini map edir; stack-trace/daxili detal client-ə sızmır; filter-chain (401/403) ilə controller (400/404/409) formaları tutarlı. |
| **AuthService** | OTP axını (issue/verify/revoke), refresh token rotation + reuse-detection, enumeration qorunması (forgot-password/login eyni cavab), admin-panel bypass məntiqi — hamısı düzgün. |
| **PaymentService** | Idempotency-key yoxlaması, status keçid qaydaları (capture/refund/callback), refund məbləğ yoxlaması — düzgün. |
| **CrudLoggingAspect** | DTO field dəyərləri heç vaxt loglanmır (yalnız sinif adı) — şifrə/token sızması yoxdur; correlation-id inteqrasiyası işləyir. |
| **Security/JWT** | HS256 imza, issuer yoxlaması, access/refresh tip ayrımı, constant-time imza müqayisəsi (`MessageDigest.isEqual`), BCrypt şifrə hash — hamısı düzgün. |
| **CORS/config** | `CorsConfig` xarici konfiqurasiyaya bağlı, wildcard+credentials kombinasiyası yoxdur; `DotenvEnvironmentPostProcessor` Boot 4.x-in yeni interfeysi üçün düzgün yazılıb. |

---

## 4. Kompilyasiya / Build Statusu

```
./mvnw clean compile              → BUILD SUCCESS, 0 warning (əvvəl: NonNull deprecation + store ambiguity)
./mvnw test-compile               → BUILD SUCCESS
./mvnw spring-boot:run (8082)     → Started in 5.9s, 0 runtime warning
```

**Qeyd:** Tam inteqrasiya test paketi (`./mvnw test`) bu mühitdə işlədilmədi, çünki testlər canlı Postgres-ə qoşulur və port 8081-də artıq bir instance işləyirdi. Kod dəyişiklikləri compile + test-compile + real boot ilə doğrulandı.

---

## 5. Dəyişdirilən Fayllar (bu sessiya)

| Fayl | Dəyişiklik |
|---|---|
| `pom.xml` | `spring-boot-starter-data-jdbc` + `-data-jdbc-test` silindi (izahlı şərhlə) |
| `security/JwtAuthenticationFilter.java` | `NonNull` importu JSpecify-ə keçirildi |
| `security/AuthRateLimitingFilter.java` | `NonNull` importu JSpecify-ə keçirildi |
| `security/PaymentCallbackSignatureFilter.java` | `NonNull` importu JSpecify-ə keçirildi |
| `logging/CorrelationIdFilter.java` | `NonNull` importu JSpecify-ə keçirildi |
| `service/outcomes/CourseReviewService.java` | `assertNotReassigningOwner` guard-ı əlavə olundu (update + patch) |

Heç bir dəyişiklik commit edilməyib — hamısı işçi qovluqda, nəzərdən keçirmək üçün açıqdır.
