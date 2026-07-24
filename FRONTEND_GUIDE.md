# NexoraAcademy — Frontend Developer Guide

Bu sənəd `API_CONTRACT.md`-dəki (backend-BFF komandası üçün yazılmış, çox təfərrüatlı) məlumatın frontend developer üçün **praktiki, dizayn-yönümlü** xülasəsidir: hansı modellər var, hansı səhifələr qurulmalıdır, hansı endpoint hansı rolun hansı ekranına aiddir, və **hazırkı backend-in hələ dəstəkləmədiyi** şeylər. Dəqiq validasiya qaydaları/edge-case-lər üçün `API_CONTRACT.md`-ə istinad et — bu sənəd ondan törəyib, ziddiyyət olarsa `API_CONTRACT.md` daha ətraflıdır.

---

## 1. Məhsul nədir

NexoraAcademy — kurs satan bir təhsil platformasıdır (bootcamp/akademiya modeli). İki tərəfi var:

1. **Public sayt** — qonaqlar/tələbələr üçün: kurs kataloqu, kurs təfərrüatı, qeydiyyat/login, öz profili, öz enrollment-ləri, kurs rəyləri.
2. **Admin/CRM Panel** — daxili komanda üçün (4 rol: `ADMIN`, `SYSTEM_ADMIN`, `SALES_CRM`, `CONTENT_MANAGER`): kurs/kateqoriya/təlimçi idarəçiliyi, satış/CRM (lead-lər, kampaniyalar), ödəniş/təqaüd idarəçiliyi, istifadəçi idarəçiliyi, audit logları.

Bunlar **iki ayrı frontend tətbiqi** kimi düşünülməlidir (ayrı domenlər/route-lar altında ola bilər: `app.nexora-academy.com` public, `admin.nexora-academy.com` panel) — icazə modeli də elə buna görə dizayn olunub (bax §3).

---

## 2. Əsas Texniki Qaydalar

| Mövzu | Dəyər |
|---|---|
| Base URL (dev) | `http://localhost:8081` |
| Global prefiks | Yoxdur — hər endpoint özü `/api/v1/...` yazır |
| Content-Type | `application/json` (bütün request/response body-lər) |
| Auth header | `Authorization: Bearer <accessToken>` |
| Envelope | **Yoxdur** — cavab birbaşa DTO (`{...}`) və ya massiv (`[...]`)dir, `{"data":...}` kimi sarma yoxdur |
| Pagination | Yalnız **2 endpoint**-də: `GET /api/v1/courses`, `GET /api/v1/users` (Spring Data `Page<T>`, bax §6.9). Qalan bütün collection endpoint-ləri **limitsiz düz massiv** qaytarır |
| Uğurlu `POST` | `201 Created` + `Location` header + body-də yaradılan obyekt |
| `DELETE` / bir çox `Void` `POST` | `204 No Content`, body yoxdur |
| Tarix/vaxt | `Instant` sahələr ISO-8601 string (`"2026-07-24T06:47:41.100Z"`), `LocalDate` sahələr `"2026-07-24"` |
| ID tipləri | Əksər resurslar `UUID` (string), amma `categories`/`scholarships` → `short` (number), `content/cms-content`/`kb-articles`(UUID)/`graduate-outcomes`/`oauth-accounts`/`audit-logs` → `long`/`UUID` qarışıqdır — hər modul üçün §6-da dəqiq göstərilib |
| Sərbəst-schema sahələr | `Map<string, object>` tipli sahələr (`profile`, `content`, `data`, `payload`, `installments`, `schedule`, `certifications`, `applications`, `activityLog`, `messages`, `aiSentiment`, `beforeState`/`afterState`) — backend-də bunların daxili strukturu üçün heç bir validasiya/şablon yoxdur, frontend istədiyi kimi JSON obyekt göndərə bilər |

---

## 3. Rollar və Hansı UI-ya Aiddir

Bir istifadəçinin **yalnız bir rolu** var (array deyil, tək sahə):

| Rol | UI | Qeyd |
|---|---|---|
| `STUDENT` | Public sayt | Default rol, qeydiyyatdan keçən hər kəs |
| `GUEST` | Public sayt | Enum-da var, amma kodda heç yerdə təyin edilmir — nəzərə alma, praktikada görünməyəcək |
| `CONTENT_MANAGER` | Admin panel | Kurs/kateqoriya/təlimçi/kurs-qrupu/bilgi-bazası/məzun-hekayələri/CMS content idarəçiliyi |
| `SALES_CRM` | Admin panel | Lead/kampaniya/chat-session/contact-submission + enrollment-lərə staff girişi |
| `ADMIN` | Admin panel | Demək olar hər şey (yuxarıdakı ikisi + istifadəçilər + ödənişlər + s.) |
| `SYSTEM_ADMIN` | Admin panel | `ADMIN` ilə **eyni səlahiyyət** (iyerarxiya deyil, hər path qaydasında ikisi də ayrıca sadalanıb) |

**Login zamanı BFF/frontend cavabın formasına görə hansı UI-ya yönləndirəcəyini bilməlidir** — bax §4.2, çünki `POST /auth/login` cavabı **rola görə fərqli JSON formasıdır**.

---

## 4. Autentifikasiya Axını

### 4.1 Register (public sayt, STUDENT üçün)
```
POST /api/v1/auth/register
{ email, fullName, phone?, password }
→ 201 { userId, email, message }
```
Hesab `PENDING_VERIFICATION` statusu ilə yaradılır, email-ə **6 rəqəmli OTP** göndərilir (link deyil).

```
POST /api/v1/auth/verify-email
{ email, otp }
→ 204
```
Qeyd: email təsdiqlənməsə də istifadəçi **login ola bilir** (yalnız `PENDING_VERIFICATION` statusu qalır) — email verification login-i bloklamır. UI-da "email-i təsdiqlə" xatırlatması göstərmək istəyə bağlıdır, məcburi divar deyil.

### 4.2 Login — İKİ FƏRQLİ AXIN (eyni endpoint, rola görə fərqli cavab)

```
POST /api/v1/auth/login
{ email, password }
```

**Admin-panel rolları (`ADMIN`/`SYSTEM_ADMIN`/`SALES_CRM`/`CONTENT_MANAGER`)** → birbaşa token:
```json
{ "accessToken": "...", "refreshToken": "...", "tokenType": "Bearer", "expiresInSeconds": 900 }
```

**`STUDENT`/`GUEST`** → OTP addımı lazımdır, token YOXDUR:
```json
{ "message": "...", "email": "...", "expiresInSeconds": 600 }
```

**Frontend qaydası:** cavabda `accessToken` sahəsi varsa → giriş tamamlanıb, panelə yönləndir. Yoxdursa (`message`/`expiresInSeconds` var) → "6 rəqəmli kodu daxil et" ekranına keç:
```
POST /api/v1/auth/login/verify-otp
{ email, otp }
→ 200 { accessToken, refreshToken, tokenType, expiresInSeconds }
```

OTP xüsusiyyətləri: 10 dəqiqə etibarlı, 5 səhv cəhddən sonra kod ləğv olunur (yenidən `/login` çağırılmalıdır), eyni anda yalnız bir aktiv kod olur.

### 4.3 Token yeniləmə və çıxış
```
POST /api/v1/auth/refresh    { refreshToken } → 200 TokenResponse (YENİ cüt, köhnəsi bir daha işləməz)
POST /api/v1/auth/logout     { refreshToken } → 204 (DB-də refresh token ləğv olunur)
```
**VACİB:** logout yalnız refresh token-i DB-də ləğv edir. **Access token stateless-dir** — logout-dan sonra da öz 15 dəqiqəlik ömrü bitənə qədər texniki olaraq etibarlıdır (backend-də blacklist yoxdur). Frontend logout-da access token-i **özü** yaddaşdan silməlidir, bu, təkbaşına kifayət deyil bir təhlükəsizlik tədbiri kimi saymaq üçün, amma başqa mexanizm yoxdur.

### 4.4 Şifrə unutma
```
POST /api/v1/auth/forgot-password   { email } → 204 (həmişə, email mövcud olub-olmamasından asılı olmayaraq)
POST /api/v1/auth/reset-password    { token, newPassword } → 204
```
Qeyd: bura **link-token**-dır (email-dəki linkdən gələn token), OTP deyil — register/login-dən fərqli mexanizm.

### 4.5 Token idarəsi — praktiki tövsiyə
- **Access token**: yaddaşda (JS variable/React state) saxla, səhifə yenilənəndə itsin — 15 dəqiqəlik ömrü var, `localStorage`-da saxlamaq XSS riskini artırır.
- **Refresh token**: mümkünsə `httpOnly` cookie (backend bunu dəstəkləmir, JSON body-də qaytarır — frontend/BFF özü cookie-yə çevirməlidir), olmasa `localStorage` (30 gün ömrü var, diqqətli ol).
- 401 alanda: access token-i refresh et (`/auth/refresh`), alınmasa login səhifəsinə yönləndir.
- JWT-də **yalnız** `sub` (userId), `role`, `type`, `iss`, `iat`, `exp`, `jti` var — **email YOXDUR**. İstifadəçi məlumatı (email, ad, s.) üçün login-dən sonra ayrıca `GET /api/v1/users/me` çağır.

### 4.6 Xəta halları (login/OTP)
| Vəziyyət | Status | `message` |
|---|---|---|
| Yanlış email/parol | 401 | `"Invalid email or password"` (hansının səhv olduğu deyilmir — enumeration qorunur) |
| OTP tapılmadı/vaxtı bitib/artıq işlənib | 401 | `"Invalid or expired code"` |
| OTP vaxtı keçib (spesifik) | 401 | `"Code has expired"` |
| Səhv rəqəmlər | 401 | `"Invalid code"` |
| `/auth/**` endpoint-lərinə həddindən çox sorğu | 429 | Rate limit (IP-əsaslı, dəqiqədə 5-10 sorğu — bax §7) |

---

## 5. Ümumi TypeScript Tipləri

```typescript
// ---- Ümumi ----
type UUID = string;
type ISODateTime = string; // "2026-07-24T06:47:41.100Z"
type ISODate = string;     // "2026-07-24"

interface ErrorResponse {
  timestamp: ISODateTime;
  status: number;
  error: string;           // "Bad Request", "Unauthorized", "Forbidden", "Not Found", "Conflict", "Internal Server Error"
  message: string;
  path: string;
  errors?: Record<string, string> | null; // field-based validasiya xətaları (yalnız 400-də dolu olur); 401/403-də bəzən bu açar TAMAMILƏ yoxdur
}

interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;       // cari səhifə (0-based)
  size: number;
  numberOfElements: number;
  first: boolean;
  last: boolean;
  empty: boolean;
  pageable: { pageNumber: number; pageSize: number; offset: number; paged: boolean; unpaged: boolean; sort: { sorted: boolean; unsorted: boolean; empty: boolean } };
  sort: { sorted: boolean; unsorted: boolean; empty: boolean };
}

// ---- Enum-lar (bütün mümkün dəyərlər, JSON-da olduğu kimi UPPER_CASE) ----
type UserRole = "GUEST" | "STUDENT" | "SALES_CRM" | "CONTENT_MANAGER" | "ADMIN" | "SYSTEM_ADMIN";
type AccountStatus = "PENDING_VERIFICATION" | "ACTIVE" | "SUSPENDED" | "DEACTIVATED" | "BANNED";
type DifficultyLevel = "BEGINNER" | "INTERMEDIATE" | "ADVANCED";
type DeliveryFormat = "ONLINE" | "OFFLINE" | "HYBRID";
type GroupStatus = "PLANNED" | "OPEN" | "FULL" | "IN_PROGRESS" | "COMPLETED" | "CANCELLED";
type EnrollmentStatus = "WAITLISTED" | "HELD" | "PENDING_PAYMENT" | "CONFIRMED" | "COMPLETED" | "CANCELLED" | "REFUNDED";
type PaymentMethod = "CARD" | "BANK_TRANSFER" | "INSTALLMENT" | "SCHOLARSHIP_COVERED";
type PaymentStatus = "INITIATED" | "AUTHORIZED" | "CAPTURED" | "FAILED" | "CANCELLED" | "REFUNDED" | "PARTIALLY_REFUNDED";
type CmsContentType = "PAGE" | "FAQ" | "SOCIAL_LINK" | "BANNER";
type LeadSource = "CONTACT_FORM" | "DEMO_REQUEST" | "SYLLABUS_DOWNLOAD" | "NEWSLETTER" | "CHATBOT" | "REFERRAL";
type LeadStatus = "NEW" | "CONTACTED" | "QUALIFIED" | "CONVERTED" | "LOST" | "DISQUALIFIED";
type SubmissionType = "CONTACT" | "DEMO" | "SYLLABUS_DOWNLOAD" | "NEWSLETTER";
type OAuthProvider = "GOOGLE" | "GITHUB" | "LINKEDIN";
type SessionType = "SESSION" | "PASSWORD_RESET" | "EMAIL_VERIFY" | "LOGIN_OTP";
type NotificationChannel = "EMAIL" | "SMS" | "IN_APP" | "PUSH";
type NotificationStatus = "QUEUED" | "SENT" | "FAILED" | "READ";
```

---

## 6. Modul-modul Data Modelləri və Endpoint-lər

Format: `Metod Path — Rol`. `Rol: PUBLIC` = token lazım deyil. Bütün `POST/PUT/PATCH/DELETE` cavabları göstərilən Response tipini qaytarır, `DELETE` 204 boş qaytarır.

### 6.1 Auth — `/api/v1/auth` (hamısı PUBLIC)
```typescript
interface RegisterRequest { email: string; fullName: string; phone?: string; password: string; } // password: 8-72 simvol, ≥1 hərf + ≥1 rəqəm
interface RegisterResponse { userId: UUID; email: string; message: string; }
interface LoginRequest { email: string; password: string; }
interface LoginOtpResponse { message: string; email: string; expiresInSeconds: number; }
interface LoginOtpVerifyRequest { email: string; otp: string; } // otp: 6 rəqəm
interface TokenResponse { accessToken: string; refreshToken: string; tokenType: "Bearer"; expiresInSeconds: number; }
interface RefreshTokenRequest { refreshToken: string; }
interface ForgotPasswordRequest { email: string; }
interface ResetPasswordRequest { token: string; newPassword: string; }
interface VerifyEmailRequest { email: string; otp: string; }
interface ResendVerificationRequest { email: string; }
```

### 6.2 Users — `/api/v1/users`
```typescript
interface UserResponse {
  id: UUID; email: string; phone?: string; fullName: string;
  role: UserRole; status: AccountStatus; locale: string;
  profile: Record<string, unknown>;
  lastLoginAt?: ISODateTime; createdAt: ISODateTime; updatedAt: ISODateTime;
}
interface UpdateProfileRequest { email?: string; phone?: string; fullName?: string; locale?: string; profile?: Record<string, unknown>; }
interface ChangePasswordRequest { currentPassword: string; newPassword: string; }
interface UserRequest { email: string; phone?: string; fullName: string; password: string; role?: UserRole; status?: AccountStatus; locale?: string; profile?: Record<string, unknown>; }
```
- `GET /api/v1/users/me` — **authenticated** (istənilən rol, öz profili)
- `PATCH /api/v1/users/me` — **authenticated**
- `POST /api/v1/users/me/password` — **authenticated**
- `POST|GET|GET{id}|PUT{id}|PATCH{id}|DELETE{id} /api/v1/users` — **ADMIN, SYSTEM_ADMIN** (tam CRUD, `GET` list **paginated** — bax §6.9)

### 6.3 Categories — `/api/v1/categories` (id: `short`/number)
```typescript
interface CategoryRequest { slug: string; name: string; parentId?: number; sortOrder?: number; active?: boolean; } // slug: kebab-case, max 80
interface CategoryResponse { id: number; slug: string; name: string; parentId?: number; sortOrder: number; active: boolean; }
```
- `GET` (list, `GET /{id}`) — **PUBLIC**. Liste **List** qaytarır, pagination yoxdur.
- `POST/PUT/PATCH/DELETE` — **ADMIN, SYSTEM_ADMIN, CONTENT_MANAGER**

### 6.4 Courses — `/api/v1/courses` (id: `UUID`) — kataloqun mərkəzi
```typescript
interface CourseRequest {
  slug: string;                    // kebab-case, max 160
  categoryId: number;
  title: string;                   // 3-200
  shortDescription?: string; fullDescription?: string; targetAudience?: string;
  difficulty: DifficultyLevel;
  durationWeeks?: number;
  deliveryFormat: DeliveryFormat;
  locationText?: string;
  basePrice?: number; currency?: string; pricePeriod?: string;
  published?: boolean; active?: boolean; archived?: boolean;
  validFrom?: ISODateTime; validUntil?: ISODateTime;
  content?: Record<string, unknown>;
  relatedCourseIds?: UUID[];
}
interface CourseResponse extends CourseRequest { id: UUID; createdBy?: UUID; createdAt: ISODateTime; updatedAt: ISODateTime; }
```
- `GET /api/v1/courses` — **PUBLIC**, query: `q, categoryId, difficulty, deliveryFormat, published, active, page, size, sort` → **`Page<CourseResponse>`**
- `GET /api/v1/courses/{id}` — **PUBLIC**
- `POST/PUT/PATCH/DELETE` — **ADMIN, SYSTEM_ADMIN, CONTENT_MANAGER**

### 6.5 Instructors — `/api/v1/instructors` (id: `UUID`) — **PUBLIC GET YOXDUR**, hamısı staff-only
```typescript
interface InstructorRequest { userId?: UUID; fullName: string; bio?: string; photoUrl?: string; linkedinUrl?: string; certifications?: Record<string, unknown>[]; active?: boolean; }
interface InstructorResponse extends InstructorRequest { id: UUID; avgRating?: number; createdAt: ISODateTime; }
```
Rol: **CONTENT_MANAGER, ADMIN, SYSTEM_ADMIN** (GET də daxil). Kurs təfərrüatı səhifəsində "təlimçi haqqında" göstərmək istəyirsənsə, ya bu məlumatı `CourseResponse.content` (sərbəst JSON) daxilində saxla, ya da backend komandasından public alt-set üçün ayrıca endpoint istə (bax §8).

### 6.6 Course-Instructors — `/api/v1/course-instructors` (composite key: `courseId`+`instructorId`)
```typescript
interface CourseInstructorRequest { courseId: UUID; instructorId: UUID; role: "lead" | "co-instructor" | "mentor"; }
interface CourseInstructorResponse extends CourseInstructorRequest {}
```
Rol: hamısı **CONTENT_MANAGER, ADMIN, SYSTEM_ADMIN** (GET də daxil).

### 6.7 Course Groups — `/api/v1/course-groups` (kohortlar/tarixlər) — **PUBLIC GET YOXDUR**
```typescript
interface CourseGroupRequest {
  courseId: UUID; groupCode: string; startDate: ISODate; endDate?: ISODate;
  registrationDeadline?: ISODateTime; totalSeats: number; status?: GroupStatus;
  schedule?: Record<string, unknown>[];
}
interface CourseGroupResponse extends CourseGroupRequest { id: UUID; reservedSeats: number; createdAt: ISODateTime; }
```
Rol: hamısı **CONTENT_MANAGER, ADMIN, SYSTEM_ADMIN** (GET də daxil). ⚠️ Bax §8 — public sayt bunu göstərə bilmir hazırda.

### 6.8 Enrollments — `/api/v1/enrollments`
```typescript
interface EnrollmentRequest { userId: UUID; groupId: UUID; status?: EnrollmentStatus; idempotencyKey: string; consentVersion?: string; consentGivenAt?: ISODateTime; }
interface EnrollmentResponse {
  id: UUID; userId: UUID; groupId: UUID; status: EnrollmentStatus;
  idempotencyKey: string; consentVersion?: string; consentGivenAt?: ISODateTime;
  holdExpiresAt?: ISODateTime; enrolledAt: ISODateTime; completedAt?: ISODateTime;
  cancelledAt?: ISODateTime; cancelReason?: string;
}
interface CancelEnrollmentRequest { reason?: string; }
```
Rol: **hər hansı authenticated istifadəçi**, amma icazə servis səviyyəsində:
- `STUDENT` yalnız **öz** `userId`-si ilə yarada bilər, yalnız **öz** qeydini görə/ləğv edə bilər.
- `ADMIN`/`SYSTEM_ADMIN`/`SALES_CRM` istənilən istifadəçi adından yarada/görə/idarə edə bilər.
- `status` sahəsi create-də **yalnız staff** üçün effektivdir — STUDENT göndərsə belə backend onu görməzdən gəlir, həmişə `PENDING_PAYMENT` ilə yaradır.
- `POST /api/v1/enrollments/{id}/cancel` — sahib və ya staff.

### 6.9 Payments — `/api/v1/payments` (hamısı **ADMIN, SYSTEM_ADMIN** — student-facing DEYİL)
```typescript
interface PaymentRequest { enrollmentId: UUID; method: PaymentMethod; amount: number; currency?: string; externalTxnId?: string; idempotencyKey: string; installments?: Record<string, unknown>[]; }
interface PaymentResponse {
  id: UUID; enrollmentId: UUID; method: PaymentMethod; amount: number; currency: string;
  status: PaymentStatus; externalTxnId?: string; idempotencyKey: string;
  installments: Record<string, unknown>[]; refundAmount: number; refundReason?: string;
  initiatedAt: ISODateTime; capturedAt?: ISODateTime; failureReason?: string;
}
```
`POST /api/v1/payments/{id}/capture` — status → `CAPTURED`. `POST /api/v1/payments/callback` **PUBLIC**-dir amma bu, ödəniş gateway-inin webhook-udur — **frontend bunu birbaşa çağırmamalıdır**. Bax §8 — student özü "İndi öde" düyməsi ilə ödəniş başlada bilmir, bu, admin/staff əməliyyatıdır hazırkı dizaynda.

### 6.10 Scholarships — `/api/v1/scholarships` (id: `short`, hamısı ADMIN/SYSTEM_ADMIN)
```typescript
interface ScholarshipRequest { name: string; description?: string; discountPct?: number; maxRecipients?: number; validFrom?: ISODate; validUntil?: ISODate; active?: boolean; }
interface ScholarshipResponse extends ScholarshipRequest { id: number; applications: Record<string, unknown>[]; }
```

### 6.11 CMS Content — `/api/v1/content/cms-content` (id: `number`, hamısı CONTENT_MANAGER/ADMIN/SYSTEM_ADMIN — **PUBLIC GET YOXDUR**)
```typescript
interface CmsContentRequest { key: string; type: CmsContentType; title?: string; body?: string; data?: Record<string, unknown>; published?: boolean; sortOrder?: number; }
interface CmsContentResponse extends CmsContentRequest { id: number; updatedBy?: UUID; updatedAt: ISODateTime; }
```
⚠️ Bax §8 — public sayt banner/FAQ/statik səhifə məzmununu bu endpoint-dən **çəkə bilmir** (permitAll deyil).

### 6.12 CRM — `/api/v1/sales/*` (hamısı ADMIN/SYSTEM_ADMIN/SALES_CRM)
```typescript
interface CampaignRequest { name: string; bannerImageUrl?: string; ctaUrl?: string; discountPct?: number; startsAt: ISODateTime; endsAt: ISODateTime; active?: boolean; priority?: number; courseIds?: UUID[]; }
interface CampaignResponse extends CampaignRequest { id: UUID; }

interface ChatSessionRequest { userId?: UUID; leadId?: UUID; channel?: string; messages?: Record<string, unknown>[]; }
interface ChatSessionResponse extends ChatSessionRequest { id: UUID; startedAt: ISODateTime; endedAt?: ISODateTime; }

interface ContactSubmissionRequest { leadId?: UUID; type: SubmissionType; courseId?: UUID; fullName?: string; email?: string; phone?: string; message?: string; preferredTime?: ISODateTime; }
interface ContactSubmissionResponse extends ContactSubmissionRequest { id: UUID; status: string; submittedAt: ISODateTime; } // status: enum deyil, sərbəst string

interface LeadRequest { fullName?: string; email?: string; phone?: string; courseId?: UUID; source: LeadSource; assignedTo?: UUID; consentVersion?: string; }
interface LeadResponse extends LeadRequest {
  id: UUID; status: LeadStatus; consentGivenAt?: ISODateTime; duplicateOfLeadId?: UUID;
  activityLog: Record<string, unknown>[]; createdAt: ISODateTime; updatedAt: ISODateTime;
}
```
⚠️ Bax §8 — `campaigns` (marketinq banner-ləri) və `contact-submissions` (əlaqə forması) **public tərəfdən çağırıla bilmir** — hər ikisi `/api/v1/sales/**` altında, yəni CRM daxili qeyd üçündür, public "bizimlə əlaqə" formu bu endpoint-i işlədə bilməz.

### 6.13 Identity/Platform (admin-only, adətən UI-nin "Sistem" bölməsi)
```typescript
interface OAuthAccountRequest { userId: UUID; provider: OAuthProvider; providerUserId: string; accessTokenEnc?: string; refreshTokenEnc?: string; }
interface OAuthAccountResponse { id: number; userId: UUID; provider: OAuthProvider; providerUserId: string; linkedAt: ISODateTime; }
// Qeyd: real Google/GitHub OAuth-login axını YOXDUR, bu sırf CRUD-dur.

interface SessionResponse { id: UUID; userId: UUID; type: SessionType; ipAddress?: string; userAgent?: string; issuedAt: ISODateTime; expiresAt: ISODateTime; usedAt?: ISODateTime; revokedAt?: ISODateTime; }
// "Sessiyalar" = identity.sessions DB cədvəli (refresh/otp/reset token-ləri), istifadəçi "aktiv cihazlar" siyahısı DEYİL.

interface NotificationRequest { userId: UUID; type: string; channel: NotificationChannel; payload?: Record<string, unknown>; }
interface NotificationResponse extends NotificationRequest { id: UUID; status: NotificationStatus; sentAt?: ISODateTime; readAt?: ISODateTime; createdAt: ISODateTime; }
// Qeyd: istifadəçiyə görə filtrlənən "mənim bildirişlərim" endpoint-i YOXDUR — yalnız admin tam siyahı görür.

interface AuditLogResponse { id: number; actorId?: UUID; action: string; entityType: string; entityId: string; beforeState?: Record<string, unknown>; afterState?: Record<string, unknown>; traceId?: UUID; ipAddress?: string; createdAt: ISODateTime; }
```

### 6.14 Outcomes (content bölməsinin davamı)
```typescript
interface KbArticleRequest { sourceType: string; sourceRefId?: string; title?: string; content: string; active?: boolean; }
interface KbArticleResponse extends KbArticleRequest { id: UUID; updatedAt: ISODateTime; }
// "Bilgi bazası" adına baxmayaraq AI/embedding/search inteqrasiyası yoxdur — sırf CRUD.

interface CourseReviewRequest { courseId: UUID; userId: UUID; enrollmentId?: UUID; rating: number; comment?: string; } // rating: 1-5
interface CourseReviewResponse extends CourseReviewRequest { id: number; published: boolean; moderatedBy?: UUID; aiSentiment?: Record<string, unknown>; createdAt: ISODateTime; }
// create-də HƏMİŞƏ published=false (moderasiya gözləyir) — "publish et" üçün HTTP endpoint YOXDUR (bax §8).
// GET list HEÇ BİR courseId filtri qəbul etmir — bütün rəyləri qaytarır, kurs səhifəsində göstərmək üçün frontend client-side filtr etməlidir.

interface GraduateOutcomeRequest { userId: UUID; courseId: UUID; companyName?: string; jobTitle?: string; employedAt?: ISODate; salaryBand?: string; publicStory?: boolean; storyText?: string; }
interface GraduateOutcomeResponse extends GraduateOutcomeRequest { id: number; createdAt: ISODateTime; }
// "Public uğur hekayələri" adına baxmayaraq public GET yoxdur — CONTENT_MANAGER/ADMIN-only.
```
`course-reviews`: rol — authenticated (hər kəs görə bilər, `STUDENT` yalnız öz adından yaza/redaktə edə bilər). `kb-articles`/`graduate-outcomes`: **CONTENT_MANAGER/ADMIN/SYSTEM_ADMIN**-only, GET də daxil.

---

## 7. Xəta İdarəetməsi (Frontend Nümunəsi)

```typescript
async function apiFetch<T>(path: string, options: RequestInit = {}): Promise<T> {
  const res = await fetch(`${BASE_URL}${path}`, {
    ...options,
    headers: { "Content-Type": "application/json", ...(accessToken ? { Authorization: `Bearer ${accessToken}` } : {}), ...options.headers },
  });

  if (res.status === 204) return undefined as T;

  const body = await res.json();

  if (!res.ok) {
    const err = body as ErrorResponse;
    // err.errors mövcuddursa → form sahələrinə görə göstər (bax aşağı)
    // err.errors YOXDURSA (401/403-də ola bilər) → err.message-i generic toast kimi göstər
    throw err;
  }
  return body as T;
}
```

**Form validasiya xətalarını göstərmək** (400, `errors` doludursa):
```json
{ "status": 400, "message": "Validation failed", "errors": { "email": "must not be blank", "password": "must not be blank" } }
```
`errors` bir `Record<sahə adı, mesaj>`-dır — hər sahənin altında bir mesaj göstər. **Bir sahədə birdən çox qayda pozulsa yalnız sonuncu mesaj gəlir** (backend map-i üzərinə yazır) — bütün pozulan qaydaları eyni anda göstərə bilməzsən, təkcə birini.

**429 (rate limit, yalnız `/api/v1/auth/**`):** ayrıca UI mesajı göstər ("Çox sayda cəhd, bir az sonra yenidən yoxla") — bu, `ErrorResponse` formatında deyil, düz JSON `{"status":429,...}` (bax `AuthRateLimitingFilter`).

---

## 8. Bilinən Boşluqlar — Backend Komandası ilə Aydınlaşdırılmalı

Bunlar **hazırkı backend-in dəstəkləmədiyi** şeylərdir — dizayna başlamazdan əvvəl bunları bil, əks halda "niyə bu endpoint yoxdur" deyə vaxt itirərsən:

1. **Public marketinq məzmunu yoxdur.** Ana səhifə banner-i, FAQ, statik səhifələr (`cms-content`) və kampaniyalar (`campaigns`) yalnız admin-only endpoint-lərdədir. Ana səhifəni statik/hardcoded məzmunla başla, ya da backend komandasından public alt-set istə.
2. **Public "bizimlə əlaqə" formu yoxdur.** `contact-submissions` CRM-daxili, `ADMIN/SALES_CRM`-only-dir. Public contact/demo/syllabus-download formu göndərmək üçün açıq endpoint tapılmadı.
3. **Kurs qrupları (kohort/tarix/yer sayı) public deyil.** `course-groups` tam CONTENT_MANAGER-only. Kurs təfərrüatı səhifəsində "hansı tarixdə başlayır, neçə yer qalıb" göstərmək istəyirsənsə, bu, hazırda mümkün deyil.
4. **Öz-özünə checkout/ödəniş axını yoxdur.** `/payments/**` tamamilə admin-only-dir (public "İndi öde" başlatma endpoint-i yoxdur), yalnız `/payments/callback` public-dir (gateway webhook-u, frontend üçün deyil). Tələbə enrollment yaradanda status avtomatik `PENDING_PAYMENT` olur, amma ödənişi kim/necə "capture" edəcəyi hazırkı API-də yalnız admin paneldən mümkündür.
5. **Fayl/şəkil yükləmə endpoint-i yoxdur.** `photoUrl`, `bannerImageUrl` kimi sahələr yalnız **hazır URL string** qəbul edir (`^https?://.+`). Cloudinary kitabxanası `pom.xml`-də var, amma inteqrasiya yazılmayıb. Şəkilləri özün başqa yerə (Cloudinary/S3/imgur) yükləyib URL-i buraya yaz.
6. **Course review-lərin `courseId`-ə görə server-side filtri yoxdur.** `GET /course-reviews` bütün rəyləri qaytarır — kurs səhifəsində göstərmək üçün client-side `courseId` ilə filtrlə (böyük data-da performans üçün backend-dən query param istə).
7. **"Publish review" API-si yoxdur.** Rəy yaradılanda həmişə `published: false`; moderasiya metodu backend-də var, amma heç bir endpoint-ə bağlı deyil.
8. **Bildirişlərin "mənimkilər" görünüşü yoxdur** — `/notifications` admin-only tam siyahı, istifadəçiyə görə filtr yoxdur (in-app bildiriş zəngi qura bilməzsən hazırkı API ilə).
9. **Pagination yalnız 2 yerdə** (`courses`, `users`) — böyük admin cədvəlləri (leads, payments, enrollments s.) limitsiz massiv qaytarır, frontend client-side pagination/virtualizasiya etməli ola bilər (backend-dən server-side pagination istəməyi düşün).

---

## 9. Səhifə Xəritəsi Təklifi

### Public Sayt
| Səhifə | Əsas endpoint(lər) | Qeyd |
|---|---|---|
| Ana səhifə | — | §8/1 — hazırda statik məzmun |
| Kurs kataloqu (axtarış/filtr/səhifələmə) | `GET /courses` (paginated), `GET /categories` (filtr üçün) | |
| Kurs təfərrüatı | `GET /courses/{id}` | Rəylər üçün `GET /course-reviews` + client-side `courseId` filtri (§8/6); qrup/tarix göstərilə bilmir (§8/3) |
| Qeydiyyat | `POST /auth/register` → `POST /auth/verify-email` | |
| Giriş | `POST /auth/login` → (rola görə) `POST /auth/login/verify-otp` | §4.2 |
| Şifrəni unutdum | `POST /auth/forgot-password` → `POST /auth/reset-password` | |
| Tələbə profili | `GET/PATCH /users/me`, `POST /users/me/password` | |
| Mənim qeydiyyatlarım | `GET /enrollments/{id}` (öz ID-ləri), `POST /enrollments`, `POST /enrollments/{id}/cancel` | Ödəniş addımı hazırda admin-tərəfli (§8/4) |
| Kurs rəyi yaz | `POST /course-reviews` (öz `userId`-si ilə) | Dərhal görünməz, moderasiya gözləyir |

### Admin/CRM Panel (rol-əsaslı naviqasiya)
| Bölmə | Rol | Endpoint-lər |
|---|---|---|
| Dashboard/login | hamısı | `POST /auth/login` (OTP-siz, birbaşa token) |
| Kurs idarəçiliyi | CONTENT_MANAGER+ | `courses`, `categories`, `instructors`, `course-instructors`, `course-groups` |
| Bilgi bazası / məzun hekayələri / CMS | CONTENT_MANAGER+ | `kb-articles`, `graduate-outcomes`, `content/cms-content` |
| Satış/CRM | SALES_CRM+ | `sales/leads`, `sales/campaigns`, `sales/chat-sessions`, `sales/contact-submissions` |
| Qeydiyyatlar (bütün tələbələr) | SALES_CRM+ | `enrollments` (staff görünüşü) |
| Ödəniş/Maliyyə | ADMIN+ | `payments`, `payments/{id}/capture`, `scholarships` |
| İstifadəçi idarəçiliyi | ADMIN+ | `users` (tam CRUD, paginated) |
| Sistem/Təhlükəsizlik | ADMIN+ | `sessions`, `oauth-accounts`, `notifications`, `admin/audit-logs` |

*(+ işarəsi: həmin rol və ondan yuxarı — `ADMIN`/`SYSTEM_ADMIN` demək olar hər bölməyə çıxışa malikdir.)*

---

## 10. Local Development üçün Qeydlər

- Swagger UI: `http://localhost:8081/swagger-ui/index.html` (dev-də açıq, prod-da söndürülüb).
- Test hesabları (`AdminSeeder`, app hər başlayanda avtomatik yaradılır):
  | Email | Şifrə | Rol |
  |---|---|---|
  | `system-admin@nexora.com` | `system-admin1234` | SYSTEM_ADMIN |
  | `admin@nexora.com` | `admin1234` | ADMIN |
  | `sales-crm@nexora.com` | `sales-crm1234` | SALES_CRM |
  | `content-manager@nexora.com` | `content-manager1234` | CONTENT_MANAGER |
- Local frontend başqa portda işləyəcəksə (məs. `http://localhost:3000`), backend-in `.env`-indəki `CORS_ALLOWED_ORIGINS`-ə o origin-i əlavə etdirt — əks halda brauzer bütün cross-origin sorğuları bloklayacaq (backend qəsdən default-suz konfiqurasiya olunub, bax `PRODUCTION_READINESS.md`).
- MailHog (OTP/reset email-lərini görmək üçün, real email getmir): `http://localhost:8025`.
