# NexoraAcademy Admin Panel — ASP.NET Core (C#) BFF İnkişaf Tapşırığı

> Bu sənəd bir **prompt/texniki tapşırıqdır** — məqsəd ayrıca komanda/developer (və ya AI coding assistant) üçün NexoraAcademy-nin admin panelini (ASP.NET Core Web API, BFF — Backend For Frontend) inkişaf etdirməkdir. Bu sənədin özü kod yazmır, yalnız **nə tikiləcəyini, necə tikiləcəyini və hansı qaydalara əməl olunacağını** dəqiq təyin edir.

---

## 0. Əvvəlcə oxu

Bu tapşırığı yerinə yetirməzdən **əvvəl** repo kökündəki **`API_CONTRACT.md`** faylını tam oxu. O sənəd Spring Boot backend-in kodunun bilavasitə oxunması ilə hazırlanıb və **yeganə həqiqət mənbəyidir** (source of truth). Bu prompt-da yazılanlarla `API_CONTRACT.md` arasında ziddiyyət olarsa, **`API_CONTRACT.md` üstündür**.

**Qəti qayda:** Heç bir endpoint, sahə, rol, status kodu və ya davranış **uydurma**. Yalnız `API_CONTRACT.md`-də yazılanı əsas götür. Orada `// TƏSDİQLƏNMƏYİB` və ya "tapılmadı, aydınlaşdırılmalıdır" işarəli yerlər varsa:
- Ya bu barədə sual ver (tapşırığı verən şəxsdən aydınlaşdırma istə),
- Ya da konservativ/təhlükəsiz defolt seç və kodda **aydın şərh** yaz ki, bu qərarın fərziyyə olduğunu bildirəsən (məs. `// FƏRZİYYƏ: backend-də X aydın deyil, buna görə Y davranışı seçildi — təsdiqlənməlidir`).

---

## 1. Layihənin məqsədi

NexoraAcademy — kurs/akademiya idarəetmə sistemidir. Backend artıq **Spring Boot (Java)** ilə yazılıb və işləyir (`localhost:8185`, dev mühitində). Sənin tapşırığın: bu backend-in üzərində **ASP.NET Core Web API BFF** qatı yazmaq ki, ayrıca bir **admin panel frontend**-i (hansı texnologiya ilə yazılacağı bu tapşırığın əhatəsində deyil — React/Angular/Blazor ola bilər) bu BFF ilə danışsın, BFF isə arxa planda Spring Boot backend-ə HTTP sorğuları göndərsin.

```
[Admin Panel Frontend] <--HTTP/JSON--> [ASP.NET Core BFF (sənin işin)] <--HTTP/JSON--> [Spring Boot Backend (localhost:8185)]
```

**BFF-in məqsədi sadəcə "proxy" deyil** — aşağıdakı əlavə dəyəri verməlidir:
1. **JWT-ni brauzerdən gizlətmək** — Spring Boot-un access/refresh JWT-ləri browser-ə heç vaxt birbaşa ötürülməməlidir. BFF login-i qəbul edir, Spring Boot-dan token alır, öz tərəfində (server-side, secure) saxlayır, admin panel frontend-inə isə öz sessiya mexanizmini (məs. `HttpOnly` cookie və ya BFF-in öz JWT-si) verir.
2. **Refresh axınının idarəsi** — Spring Boot-un refresh token-ləri **bir dəfəlik**dir (rotation + reuse-detection, bax `API_CONTRACT.md` §1.6). BFF bu axını tam öz üzərinə götürməlidir ki, frontend heç vaxt refresh məntiqi ilə məşğul olmasın.
3. **Xəta formatının normallaşdırılması** — Spring Boot-un iki fərqli error JSON forması var (bax `API_CONTRACT.md` §3). BFF bunları admin panel üçün **vahid, proqnozlaşdırıla bilən** formaya çevirməlidir.
4. **Rol-əsaslı UI dəstəyi** — backend-in path-based icazə modelini (bax §2.0, §6) BFF səviyyəsində əks etdirməli ki, frontend hansı düymələri/menyuları göstərəcəyini bilsin.

---

## 2. Texnologiya tələbləri

- **.NET 8 (LTS) və ya daha yeni**, ASP.NET Core Web API (minimal API və ya controller-based — komanda seçimi, amma layihə boyu **tutarlı** olsun).
- `System.Net.Http.HttpClient` (`IHttpClientFactory` ilə, typed client-lər) — Spring Boot backend-ə çağırışlar üçün. Refit və ya oxşar kitabxana istifadə oluna bilər, məcburi deyil.
- `System.Text.Json` — serialization/deserialization (aşağı §6-ya bax, enum və case həssaslığı üçün vacib qeydlər var).
- JWT emalı üçün: `System.IdentityModel.Tokens.Jwt` və ya `Microsoft.IdentityModel.JsonWebTokens` (BFF-in öz sessiya token-i üçün, **Spring Boot-un JWT-sini yenidən imzalamaq YOX** — sadəcə saxlamaq/ötürmək üçün).
- Konfiqurasiya: `appsettings.json` + environment-specific override-lar. Backend base URL mütləq konfiqurasiya edilə bilən olmalıdır (məs. `NexoraApi:BaseUrl`, dev-də `http://localhost:8185`), heç vaxt hardcode edilməsin.
- Loglama: `ILogger<T>` ilə struktur loglama, xüsusilə backend-dən gələn 401/403/500 cavabları üçün (amma **JWT/parol dəyərlərini heç vaxt loglama**).

---

## 3. Arxitektura və qat bölgüsü

Tövsiyə olunan layihə strukturu (adlar sərbəstdir, konsepsiya vacibdir):

```
NexoraAcademy.AdminBff/
├── Controllers/  (və ya Endpoints/, minimal API seçilibsə)   — admin panel frontend-inə baxan öz API-mız
├── Clients/                     — Spring Boot backend-ə typed HttpClient-lər (məs. IAuthApiClient, ICourseApiClient, IUserApiClient, ...)
├── Contracts/
│   ├── Backend/                 — Spring Boot-un DTO-larının C# əks etdirilməsi (API_CONTRACT.md §2-yə əsasən, 1:1)
│   └── Bff/                     — BFF-in öz frontend-inə verdiyi DTO-lar (backend DTO-larından fərqli ola bilər — məs. sadələşdirilmiş, aggregasiya olunmuş)
├── Auth/                        — token saxlama, refresh məntiqi, BFF-in öz cookie/sessiya autentifikasiyası
├── Middleware/                  — backend xətalarının normallaşdırılması, exception handling
├── Mapping/                     — Backend DTO ↔ BFF DTO çevrilmələri (əl ilə və ya Mapster/AutoMapper)
└── Program.cs / Startup
```

**Vacib prinsip:** admin panel frontend-i Spring Boot-un DTO-larını **görməməlidir** — o, yalnız BFF-in öz `Contracts/Bff` modelini görür. Bu, gələcəkdə backend dəyişəndə frontend-ə təsir etməməsi üçündür (klassik BFF izolyasiyası). Sadə CRUD hallarında `Bff` DTO-su `Backend` DTO-sunun demək olar eynisi ola bilər — bu normaldır, məcburi fərqləndirmə tələb olunmur, sadəcə **qat ayrılığı** olmalıdır (heç vaxt backend DTO-sunu birbaşa controller-dən qaytarma).

---

## 4. Auth axını — dəqiq tələblər

`API_CONTRACT.md` §1-ə əsasən (bax §1.0 — bu bölmə 2026-07-22-də login/register-ə email OTP əlavə olunduqdan sonra yenilənib):

1. **Login iki addımlıdır, BİR addım DEYİL:**
   - Addım 1: BFF `POST /api/v1/auth/login` çağırır (`{email, password}`). Backend **tokens qaytarmır** — uğurlu olsa `LoginOtpResponse{message, email, expiresInSeconds}` qaytarır və istifadəçinin email-inə 6-rəqəmli kod göndərir.
   - Addım 2: BFF admin panel frontend-inə "kodu daxil et" ekranı göstərməlidir; istifadəçi kodu daxil edəndə BFF `POST /api/v1/auth/login/verify-otp` çağırır (`{email, otp}`) — bu, əsl `TokenResponse{accessToken, refreshToken, tokenType, expiresInSeconds}` qaytarır.
   - **BFF-in login endpoint-i (öz frontend-inə baxan) bu iki addımı əks etdirməlidir** — məs. BFF-in öz `POST /auth/login`-i "OTP göndərildi" cavabı versin, ayrıca `POST /auth/login/verify-otp` (və ya bənzər) BFF endpoint-i əlavə edilsin.
2. BFF token cütünü (yalnız addım-2-dən sonra əldə olunur) **server-side** saxlamalıdır (məs. distributed cache, DB, ya da BFF-in öz encrypted `HttpOnly` cookie-si daxilində — komanda qərar versin, amma **heç vaxt** admin panel frontend-in JavaScript-inə çıplaq JWT ötürülməsin).
3. Hər backend çağırışında BFF `Authorization: Bearer <accessToken>` header-ini özü əlavə edir.
4. **Access token 15 dəqiqədən sonra bitir** (`API_CONTRACT.md` §1.4). BFF 401 alanda (backend-dən) avtomatik olaraq `POST /api/v1/auth/refresh` çağırıb yeni token cütü almalı və orijinal sorğunu bir dəfə təkrarlamalıdır (transparent refresh, frontend bunu hiss etməməlidir).
5. **Refresh token bir dəfəlikdir** — refresh çağırıldıqda backend həm yeni access, həm yeni refresh token qaytarır, BFF hər ikisini yeniləməlidir. Köhnə refresh token-i saxlamaq mənasızdır (artıq işləməyəcək).
6. **Logout:** BFF `POST /api/v1/auth/logout` çağırıb (`{refreshToken}`) sonra öz saxladığı sessiyanı təmizləməlidir. **Diqqət:** backend logout-da yalnız refresh token-i ləğv edir, access token öz təbii 15-dəqiqəlik ömrünü yaşayır (`API_CONTRACT.md` §1.7) — BFF bunu bilməli və logout-dan sonra öz tərəfində həmin access token-i bir daha **istifadə etməməlidir** (BFF-in öz sessiyası bağlandığı üçün bu avtomatik təmin olunmalıdır, amma diqqətlə yoxla).
7. **Rol claim-i:** JWT-də `role` claim-i (tək string, massiv deyil — `API_CONTRACT.md` §1.5). BFF bunu öz sessiyasında saxlayıb (admin panelin hansı bölmələri göstərəcəyini müəyyən etmək üçün) istifadə etməlidir.
8. Email/istifadəçi profili JWT-də **yoxdur** — login tamamlandıqdan (addım-2) sonra `GET /api/v1/users/me` çağırılaraq tam profil əldə edilməlidir.
9. **Register axını da dəyişib:** `POST /api/v1/auth/register` uğurlu olandan sonra istifadəçi email-ə göndərilən 6-rəqəmli kodu `POST /api/v1/auth/verify-email {email, otp}` ilə təsdiqləməlidir (əvvəllər link-based idi, indi OTP-based-dir — `{token}` DEYİL, `{email, otp}`). Admin panel bu axını da UI-də əks etdirməlidirsə (yeni istifadəçi yaratma axınında), buna görə dizayn edilməlidir.
10. **OTP-lər 6 rəqəmdir, 10 dəqiqə etibarlıdır (default), 5 səhv cəhddən sonra ləğv olunur** — BFF frontend-ə uyğun UX göstərməlidir (saymaq, "yeni kod göndər" düyməsi kimi — "yeni kod" sadəcə `/login` və ya `/resend-verification`-ı təkrar çağırmaqdır, əvvəlki kod avtomatik ləğv olunur).

**Sual/aydınlaşdırma tələb edən nöqtə (frontend-in auth mexanizmi barədə):** Bu prompt admin panel frontend-inin BFF ilə necə autentifikasiya olunacağını (cookie-based session, öz JWT-si, s.) qəti təyin etmir — bu, layihəni tapşıran şəxslə aydınlaşdırılmalıdır. Tövsiyə: **`HttpOnly` + `Secure` + `SameSite=Strict` cookie ilə BFF-in özünün session-ı**, çünki bu XSS-ə qarşı ən təhlükəsiz standart BFF nümunəsidir.

---

## 5. DTO/Model mapping — kritik qeydlər

`API_CONTRACT.md` §2-də hər endpoint üçün dəqiq request/response sahələri var. C# modelləri yazarkən bunlara **hərfiyyən** riayət et:

- **JSON case:** Backend-in JSON sahə adları `camelCase`-dir (Jackson default). C# DTO-larında `[JsonPropertyName("fieldName")]` istifadə et və ya global `JsonNamingPolicy.CamelCase` seç.
- **Enum-lar JSON-da BÖYÜK HƏRFLƏRLƏ Java sabit adı kimi gəlir** (məs. `"role":"ADMIN"`, `"status":"CONFIRMED"`, `"difficulty":"INTERMEDIATE"`) — bu, canlı backend-dən test edilərək təsdiqlənib. C# tərəfdə `JsonStringEnumConverter` istifadə edərkən enum üzv adlarını **məhz bu böyük-hərfli formada** tərif et (məs. `public enum EnrollmentStatus { WAITLISTED, HELD, PENDING_PAYMENT, CONFIRMED, COMPLETED, CANCELLED, REFUNDED }` — C#-da alt-xətli adlar underscore ilə saxlanmalıdır, `PENDING_PAYMENT` kimi, C# konvensiyasına (`PascalCase`) çevirməyə cəhd ETMƏ, əks halda deserialization sınacaq). Bütün enum-ların tam siyahısı `API_CONTRACT.md` §6.1 və hər endpoint-in DTO təsvirində var.
- **`Map<string,object>` sahələri** (`profile`, `content`, `data`, `payload`, `installments`, `activityLog`, `messages`, `aiSentiment`, `beforeState`/`afterState` və s.) — bunlar sxemsiz sərbəst JSON-dur. C#-da `Dictionary<string, object>` və ya `JsonElement`/`JsonDocument` kimi modelləşdir, **qəti tipli class yaratmağa cəhd etmə** (kodda onların daxili strukturunu təyin edən heç nə tapılmadı).
- **`UUID` sahələri** → C# `Guid`.
- **`Instant` (Java)** → ISO-8601 UTC timestamp string, C# `DateTimeOffset` (və ya `DateTime` amma UTC-ni qorumaq üçün `DateTimeOffset` tövsiyə olunur).
- **`LocalDate` (Java)** → tarix-yalnız string (`yyyy-MM-dd`), C# `DateOnly`.
- **`BigDecimal` (Java)** → C# `decimal`.
- **Nullable sahələr:** DTO təsvirlərində `?` işarəsi olan hər sahə C#-da `nullable` olmalıdır (`string?`, `Guid?`, `int?` və s.) — bu, xüsusilə **PATCH** request-lərində vacibdir (aşağı bax).
- **`Page<T>` (yalnız `GET /api/v1/courses` və `GET /api/v1/users`):** Spring Data-nın öz default JSON forması — `API_CONTRACT.md` §2.4-dəki tam nümunəyə bax. Minimum lazım olan sahələr: `content` (massiv), `totalElements`, `totalPages`, `number` (cari səhifə, 0-based), `size`. Qalan sahələri (`pageable`, `sort` daxili strukturu) BFF-in öz `PagedResult<T>` modelinə map edərkən nəzərə almaq məcburi deyil, sadəcə `content`/`totalElements`/`totalPages`/`number`/`size`-i çıxarmaq kifayətdir.
- **Digər bütün `GET` collection endpoint-ləri paginasiyasız düz JSON massiv qaytarır** (`List<T>`) — bunları `Page<T>` kimi parse etməyə cəhd etmə, sadəcə array deserialize et.

---

## 6. PATCH semantikası — vacib

Backend-də **hər resurs üçün** `PUT` (tam əvəzləmə, bütün məcburi sahələr tələb olunur) və `PATCH` (qismən yeniləmə) ayrıca dəstəklənir. `PATCH`-də:
- Göndərilməyən (C#-da `null` qalan) sahələr **toxunulmaz qalır**.
- Göndərilən sahələr **format baxımından yoxlanılır** (email formatı, pattern və s.), amma "bu sahə məcburidir" tələbi PATCH-də tətbiq olunmur.

BFF öz PATCH endpoint-lərini dizayn edərkən bu semantikanı qorumalıdır: C# DTO-da bütün sahələr `nullable` olmalı, yalnız client-in göndərdiyi (JSON-da mövcud olan) sahələr backend-ə ötürülməlidir. **Diqqət:** sadə `null`-check kifayət etməyə bilər, çünki "sahə göndərilməyib" ilə "sahə açıq şəkildə `null` göndərilib" arasında fərq lazımdırsa, `System.Text.Json`-da bunun üçün `JsonElement?`/`Optional<T>` pattern-i və ya "hansı property-lər set olunub" izləyən yanaşma istifadə edilə bilər — sadə hallarda (backend-in DTO-ları da real-mənada "null = dəyişmə" məntiqi ilə işlədiyi üçün) düz `nullable` sahələr adətən kifayətdir.

---

## 7. Xəta emalı (Error Handling)

`API_CONTRACT.md` §3-ə əsasən backend-dən **iki fərqli** JSON forması gələ bilər:

1. **Filter-zənciri xətaları (401/403, dispatch-dən əvvəl):** `{timestamp, status, error, message, path}` — **`errors` açarı YOXDUR**.
2. **`GlobalExceptionHandler`-dən keçən xətalar (400/401/403/404/409/500):** `{timestamp, status, error, message, path, errors}` — `errors` `null` və ya `{sahə: mesaj}` map-i ola bilər (yalnız validasiya xətalarında dolu olur).

BFF-in backend-ə çağıran hissəsi (typed HttpClient-lər) bu iki formanı da **eyni tərzdə** parse edə bilməlidir (`errors`-u optional/nullable saxla, JSON-da yoxdursa deserialize zamanı sınmamalıdır). Sonra BFF öz frontend-inə **vahid** xəta formatı qaytarmalıdır (bu formatı komanda özü dizayn edə bilər, məsələn `{code, message, fieldErrors}`), backend-in xam JSON-unu birbaşa frontend-ə ötürməkdənsə.

**Xüsusi hallar ki, BFF-in xüsusi məntiqlə qarşılamalı olduğu:**
- **409 (`DataIntegrityViolationException`-dan gələn "The request conflicts with existing data")** — bu, adətən PATCH-də göndərilən bir sahənin DB-level constraint-i pozması deməkdir; mesaj generic-dir, əsl səbəb backend logunda qalır. BFF bunu istifadəçiyə anlaşıqlı formada göstərməli (məs. "Göndərilən dəyər DB tərəfindən rədd edildi, formatı yoxlayın").
- **403 servis-səviyyəli (`Enrollment`/`CourseReview`)** — bu hallarda `message` daha təsviridir (məs. `"Only staff may list all enrollments"`, `"You can only enroll yourself"`) — BFF bu mesajı olduğu kimi göstərə bilər, çünki artıq insan-oxunaqlıdır.
- **422 status kodu backend-də HEÇ VAXT istifadə olunmur** — bütün validasiya xətaları 400-dür. BFF öz tərəfində 422 istifadə etmək istəyirsə, bu, backend-dən müstəqil öz qərarı olmalıdır.

---

## 8. Modul-modul funksionallıq (admin panel bölmələri)

Aşağıdakı cədvəl admin panelin əhatə etməli olduğu bütün sahələri backend-in müvafiq controller-i ilə əlaqələndirir. **Hər bölmə üçün dəqiq endpoint/DTO/rol siyahısını `API_CONTRACT.md`-in göstərilən bölməsindən oxu** — burada təkrar yazılmır (tək-mənbə prinsipi).

| Admin panel bölməsi | Backend mənbəyi (`API_CONTRACT.md` bölməsi) | Tələb olunan rol (backend tərəfi) |
|---|---|---|
| Login / sessiya idarəsi | §2.1 (AuthController) | permitAll (login özü), sonra BFF sessiyası |
| Öz profilim | §2.2 (`/me` alt-hissəsi) | istənilən authenticated |
| İstifadəçi idarəetməsi (CRUD, axtarış, paginasiya) | §2.2 (UserController) | ADMIN, SYSTEM_ADMIN |
| Kateqoriyalar | §2.3 | GET public, yazma: CONTENT_MANAGER+ |
| Kurslar (axtarış, filtrlər, paginasiya) | §2.4 | GET public, yazma: CONTENT_MANAGER+ |
| Müəllimlər (Instructors) | §2.5 | CONTENT_MANAGER+ (GET də daxil) |
| Kurs-Müəllim əlaqələri | §2.6 | CONTENT_MANAGER+ |
| Kurs qrupları | §2.7 | CONTENT_MANAGER+ (GET də daxil — **public deyil**) |
| Qeydiyyatlar (Enrollments) | §2.8 | authenticated + servis-səviyyəli sahiblik (diqqətlə oxu — staff vs özü) |
| Ödənişlər | §2.9 | ADMIN/SYSTEM_ADMIN yalnız |
| Təqaüdlər (Scholarships) | §2.10 | ADMIN/SYSTEM_ADMIN yalnız |
| CMS məzmun | §2.11 | CONTENT_MANAGER+ (public GET **yoxdur**) |
| CRM: Kampaniyalar | §2.12 | SALES_CRM+ |
| CRM: Chat sessiyaları | §2.13 | SALES_CRM+ |
| CRM: Əlaqə formaları | §2.14 | SALES_CRM+ (public submit **yoxdur** — diqqət) |
| CRM: Lead-lər | §2.15 | SALES_CRM+ |
| OAuth hesabları | §2.16 | ADMIN/SYSTEM_ADMIN yalnız (real OAuth login axını backend-də YOXDUR, sırf CRUD) |
| Sessiyalar (token qeydləri) | §2.17 | ADMIN/SYSTEM_ADMIN yalnız |
| Bildirişlər | §2.18 | ADMIN/SYSTEM_ADMIN yalnız (istifadəçiyə-görə "mənim bildirişlərim" **yoxdur**) |
| Bilgi bazası məqalələri (KB) | §2.19 | CONTENT_MANAGER+ |
| Kurs rəyləri | §2.20 | authenticated + servis-səviyyəli sahiblik (publish endpoint-i **yoxdur**) |
| Məzun uğur hekayələri | §2.21 | CONTENT_MANAGER+ (public GET **yoxdur**) |
| Audit loqları | §2.22 | ADMIN/SYSTEM_ADMIN yalnız |

**Enrollments və Course Reviews üçün xüsusi diqqət:** bu iki resurs `SecurityConfig`-də path-based qayda ilə deyil, **servis səviyyəsində** qorunur (bax `API_CONTRACT.md` §2.8, §2.20). Admin panel konteksti adətən staff (ADMIN/CONTENT_MANAGER/SALES_CRM) tərəfindən istifadə olunacağı üçün bu, əməli olaraq "staff hər şeyi görür/dəyişir" davranışına uyğun gələcək, amma BFF-in login olmuş adminin rolunu yoxlamadan bu endpoint-lərə kor-koranə güvənməməsi tövsiyə olunur (backend-in özü 403 qaytaracaq, BFF sadəcə bunu düzgün emal etməlidir).

---

## 9. CORS barədə qeyd

`API_CONTRACT.md` §4.1-ə əsasən Spring Boot backend-in CORS `allowedOrigins` konfiqurasiyası **hazırda boşdur** (heç bir origin-ə icazə yoxdur). Amma bu, sənin BFF-inə **birbaşa təsir etməməlidir** — çünki BFF backend-ə **server-side (server-to-server)** HTTP çağırışları edəcək, brauzer-CORS qaydaları yalnız brauzerdən gələn cross-origin sorğulara aiddir. BFF-in Spring Boot-a etdiyi `HttpClient` çağırışları CORS-a tabe deyil.

**Sənin öz CORS problemin fərqli səviyyədədir:** admin panel frontend-i (brauzer) ilə **sənin BFF-in** arasında. Bunu ASP.NET Core-un öz CORS middleware-i ilə (`AddCors`) konfiqurasiya etməlisən — frontend-in domenini (dev-də, məs. `http://localhost:3000` və ya frontend-in işlədiyi port) `AllowedOrigins`-ə əlavə et, `AllowCredentials(true)` istifadə et (əgər cookie-based sessiya seçilibsə).

---

## 10. Bilinən boşluqlar / məhdudiyyətlər (BFF dizaynında nəzərə al)

Bunların hamısı `API_CONTRACT.md`-də ətraflı izah olunub, burada sadəcə checklist kimi təkrarlanır ki, planlaşdırarkən unudulmasın:

- [ ] **Fayl/şəkil yükləmə backend-də YOXDUR.** `photoUrl`, `bannerImageUrl` kimi sahələr yalnız hazır URL string qəbul edir. Admin panel şəkil upload UI-si tələb edirsə, bu, BFF-in özündə (məs. ayrıca Azure Blob/S3 inteqrasiyası ilə) həll olunmalıdır — Spring Boot backend bura kömək etmir.
- [ ] **Ödəniş refund üçün HTTP API yoxdur** (yalnız daxili servis metodu var, endpoint-ə bağlı deyil).
- [ ] **Kurs rəyini "publish et" üçün HTTP API yoxdur** (yalnız daxili servis metodu var).
- [ ] **CMS məzmunun public GET-i yoxdur** — əgər admin panel "sayt önizləməsi" göstərmək istəyirsə, bunu necə edəcəyi aydınlaşdırılmalıdır.
- [ ] **Public "bizimlə əlaqə" forması yoxdur** — `contact-submissions` yalnız CRM-daxili (SALES_CRM+) yazma nöqtəsidir.
- [ ] **Access token logout-da server-side ləğv olunmur** (yalnız refresh token) — BFF öz tərəfində qısa-müddətli keşləmə/yoxlama əlavə etmək istəyə bilər, məcburi deyil.
- [ ] **Bütün collection GET endpoint-lərinin əksəriyyəti paginasiyasızdır** — böyük datasetlərdə (məs. `sessions`, `audit-logs`) performans/UX riski var, BFF səviyyəsində client-side və ya BFF-side səhifələmə əlavə etmək düşünülə bilər (backend özü dəstəkləmir).
- [ ] **`GUEST` rolu** enum-da var, amma heç bir yerdə təyin edilmir/istifadə olunmur — admin panel UI-də bu rol üçün xüsusi məntiq qurmağa ehtiyac yoxdur, sadəcə mövcudluğunu bil.

---

## 11. Qəbul meyarları (Definition of Done)

- Bütün §8-də sadalanan bölmələr üçün BFF endpoint-ləri mövcuddur və backend-in müvafiq endpoint-inə düzgün proxy edir (rol/status kodu/DTO baxımından `API_CONTRACT.md`-ə uyğun).
- Login → token saxlama → avtomatik refresh → logout tam işləyir (server-side, frontend-ə çılpaq JWT sızmadan).
- Bütün enum-lar düzgün (böyük-hərfli, underscore-lu) serialize/deserialize olunur — canlı backend-ə qarşı ən azı bir neçə enum-lu endpoint (məs. kurs yaratmaq, `difficulty`/`deliveryFormat` ilə) əl ilə test edilib.
- PATCH sorğularında göndərilməyən sahələr backend-də dəyişmir (test edilib).
- Paginasiyalı 2 endpoint (`courses`, `users`) düzgün `totalElements`/`totalPages` göstərir.
- 401 (access token bitib) → avtomatik refresh → orijinal sorğu təkrarlanır, istifadəçi kəsinti hiss etmir.
- 403 (rol kifayət etmir və ya sahiblik pozulub) → frontend-ə anlaşıqlı mesaj, admin panel UI-də müvafiq düymələr rola görə gizlədilir/deaktiv edilir.
- Backend əlçatmaz olanda (connection refused, timeout) BFF `502`/`503` kimi düzgün status qaytarır, admin panel-ə "backend down" mesajı gedir — sükutla boş cavab qaytarılmır.

---

## 12. Sual yarandıqda

Bu sənəd (və istinad etdiyi `API_CONTRACT.md`) tam olmaya bilər. Əgər tətbiq zamanı:
- backend-in davranışı gözlənilənlə üst-üstə düşmürsə,
- `API_CONTRACT.md`-də "// TƏSDİQLƏNMƏYİB" işarəli bir yerə əsaslanmaq lazım gəlirsə,
- və ya bu promptda əhatə olunmayan yeni bir ssenari ortaya çıxarsa —

**uydurma qərar vermə.** Ya canlı backend-ə (`localhost:8185`) qarşı sınaq (`curl`/Postman) apararaq faktı yoxla, ya da tapşırığı verən şəxsdən aydınlaşdırma istə.
