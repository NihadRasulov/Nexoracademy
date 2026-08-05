# Nexora Academy — Admin Panel

![.NET](https://img.shields.io/badge/.NET-10-512BD4?logo=dotnet&logoColor=white)
![React](https://img.shields.io/badge/React-19-61DAFB?logo=react&logoColor=black)
![TypeScript](https://img.shields.io/badge/TypeScript-6-3178C6?logo=typescript&logoColor=white)
![Vite](https://img.shields.io/badge/Vite-8-646CFF?logo=vite&logoColor=white)
![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-4-06B6D4?logo=tailwindcss&logoColor=white)
![Memarlıq](https://img.shields.io/badge/memarlıq-BFF-2ea44f)
![Lisenziya](https://img.shields.io/badge/lisenziya-xüsusi-lightgrey)

Nexora Academy platformasının idarəetmə panelidir. İki hissədən ibarətdir: React əsaslı **interfeys (SPA)** və interfeyslə əsl Nexora backend-i arasında dayanan **BFF (Backend-for-Frontend)** qatı. BFF autentifikasiyanı `HttpOnly` cookie ilə idarə edir və brauzerə heç bir token sızdırmadan sorğuları backend API-yə proxy edir.

İnterfeys mətnləri Azərbaycan dilindədir.

---

## Mündəricat

- [Memarlıq](#memarlıq)
- [Ekran Görüntüləri](#ekran-görüntüləri)
- [Texnologiya Yığını](#texnologiya-yığını)
- [Layihə Strukturu](#layihə-strukturu)
- [Tələblər](#tələblər)
- [Quraşdırma və İşə Salma](#quraşdırma-və-işə-salma)
- [Konfiqurasiya](#konfiqurasiya)
- [Autentifikasiya Axını](#autentifikasiya-axını)
- [Rollar və İcazələr](#rollar-və-icazələr)
- [İdarə Olunan Resurslar](#idarə-olunan-resurslar)
- [Əmr Arayışı](#əmr-arayışı)
- [Əlavə Sənədlər](#əlavə-sənədlər)
- [Lisenziya](#lisenziya)

---

## Memarlıq

```
┌──────────────────┐      cookie (HttpOnly)      ┌──────────────────┐      Bearer token      ┌──────────────────┐
│                  │ ─────────────────────────►  │                  │ ─────────────────────► │                  │
│  İnterfeys (SPA) │                             │  Admin BFF       │                        │  Nexora Backend  │
│  React + Vite    │  ◄─────────────────────────  │  ASP.NET Core    │  ◄───────────────────  │  API             │
│                  │         JSON / 401 / 403     │  (.NET 10)       │                        │                  │
└──────────────────┘                             └──────────────────┘                        └──────────────────┘
     :5173                                             :5075 / :7280
```

- **İnterfeys** birbaşa backend-ə deyil, yalnız BFF-ə sorğu göndərir (`VITE_API_BASE_URL`).
- **BFF** istifadəçinin sessiyasını cookie-yə bağlayır; backend giriş token-ını server tərəfindəki session store-da saxlayır və hər sorğuya `BackendAuthorizationHandler` vasitəsilə əlavə edir.
- Bu dizayn sayəsində giriş token-ları heç vaxt brauzerə çatmır və XSS zamanı token sızması riski əhəmiyyətli dərəcədə azalır.
- Admin SPA və bütün BFF endpoint-ləri `AdminSettings:SecretPath` ilə verilən ortaq prefix altındadır. Məsələn, `sys-control-9912` üçün login səhifəsi `/sys-control-9912/login`, login API-si isə `/sys-control-9912/api/auth/login` olur.

---

## Ekran Görüntüləri

> Şəkillər `docs/screenshots/` qovluğuna əlavə olunduqda aşağıda görünür. Hələlik yer tutucudur — öz ekran görüntülərinizi bu adlarla qoyanda avtomatik gəlir.

| Giriş | Ana Panel |
|:---:|:---:|
| ![Giriş ekranı](docs/screenshots/login.png) | ![Ana panel](docs/screenshots/dashboard.png) |

| Kurslar | İstifadəçilər |
|:---:|:---:|
| ![Kurslar](docs/screenshots/courses.png) | ![İstifadəçilər](docs/screenshots/users.png) |

<sub>Qeyd: Panel açıq/tünd tema dəstəkləyir (`next-themes`).</sub>

---

## Texnologiya Yığını

**İnterfeys — `NexoraAdminPanelUI/`**

| Sahə | İstifadə olunan |
|------|-----------------|
| Framework | React 19 + TypeScript |
| Build aləti | Vite 8 |
| Stil | Tailwind CSS 4 |
| Komponentlər | shadcn/ui + Radix UI + Base UI |
| Data qatı | TanStack Query |
| Yönləndirmə | React Router 7 |
| Formlar | React Hook Form + Zod |
| İkonlar | lucide-react |
| Bildiriş | Sonner |
| Lint | Oxlint |

**BFF — `NexoraAdminPanel/`**

| Sahə | İstifadə olunan |
|------|-----------------|
| Runtime | .NET 10 (ASP.NET Core Web API) |
| Autentifikasiya | Cookie Authentication (`HttpOnly`, `SameSite=Strict`) |
| Sessiya saxlama | `IDistributedCache` (defolt: yaddaşda / in-memory) |
| Backend girişi | Tipli `HttpClient`-lər (`IHttpClientFactory`) |
| API sənədləşməsi | OpenAPI (yalnız Development) |
| Token emalı | `System.IdentityModel.Tokens.Jwt` |

---

## Layihə Strukturu

```
NexoraAcademy/
├── NexoraAdminPanel/                      # BFF (backend)
│   └── NexoraAdminPanel/
│       ├── NexoraAdminPanel.sln
│       └── src/NexoraAcademy.AdminBff/
│           ├── Program.cs                  # Application pipeline and endpoint composition
│           ├── Controllers/                # BFF son nöqtələri (Auth, User, Course, ...)
│           ├── Clients/                    # Nexora backend-ə gedən tipli HTTP klientləri
│           ├── Auth/                        # Session store, icazə handler-i, rollar
│           ├── Contracts/                   # Backend/Bff sorğu-cavab modelləri
│           ├── Middleware/                  # Mərkəzi xəta idarəetməsi
│           └── appsettings*.json
│
└── NexoraAdminPanelUI/                    # İnterfeys (frontend)
    ├── src/
    │   ├── App.tsx                         # Route tərifləri + rol qorumaları
    │   ├── auth/                            # Auth context, qorunan route-lar, rol qrupları
    │   ├── layout/                          # Yan panel, üst panel, naviqasiya
    │   ├── pages/                           # Xüsusi səhifələr (Dashboard, Users, Courses, ...)
    │   ├── resources/                       # Konfiqurasiya ilə yaradılan ümumi CRUD infrastrukturu
    │   ├── components/                      # Təkrar istifadə olunan UI (DataTable, dialog, və s.)
    │   └── lib/                             # API klienti, xəta idarəetməsi, köməkçilər
    ├── .env.development
    └── vite.config.ts
```

> **Qeyd:** Ümumi `resources/` qatı əksər idarəetmə ekranlarını (kateqoriya, kampaniya, sessiya, audit qeydi və s.) tək bir `ResourcePage` + konfiqurasiya obyekti ilə yaradır. Yalnız xüsusi davranış tələb edən ekranlar (`courses`, `users`, `payments`, `enrollments`) ayrıca səhifə kimi yazılıb.

---

## Tələblər

- [.NET 10 SDK](https://dotnet.microsoft.com/download)
- [Node.js](https://nodejs.org/) 20+ və npm
- Əlçatan bir **Nexora Backend API** nümunəsi (BFF onun önündə dayanır)

---

## Quraşdırma və İşə Salma

Panel iki servisdən ibarət olduğu üçün hər ikisini işə salmaq lazımdır.

### 1) BFF (backend)

```bash
cd NexoraAdminPanel/NexoraAdminPanel/src/NexoraAcademy.AdminBff
dotnet restore
dotnet run
```

Defolt ünvanlar:
- HTTP: `http://localhost:5075`
- HTTPS: `https://localhost:7280`

OpenAPI sənədi yalnız Development mühitində secret prefix altında yayımlanır. Defolt ayarla ünvan `/sys-control-9912/openapi/v1.json` olur.

### 2) İnterfeys (frontend)

```bash
cd NexoraAdminPanelUI
npm install
npm run dev
```

İnterfeys defolt olaraq `http://localhost:5173/sys-control-9912/login` üzərində işləyir (BFF-in CORS ağ siyahısında `5173` və `3000` mövcuddur). Vite development server-i secret prefix-i BFF-in `appsettings.json` faylından oxuyur.

Production publish zamanı BFF layihəsi `npm ci` və `npm run build` əmrlərini avtomatik işlədir, yaranan SPA fayllarını `wwwroot`-a əlavə edir və onları yalnız secret prefix altında servis edir:

```bash
dotnet publish -c Release
```

---

## Konfiqurasiya

### İnterfeys — `NexoraAdminPanelUI/.env.development`

| Dəyişən | İzah |
|---------|------|
| `VITE_API_BASE_URL` | BFF-in ünvanı (məs. `http://localhost:5075`) |
| `VITE_DEMO_MODE` | `true` olduqda bütün `/api` çağırışları `src/lib/demo/` altındakı saxta data ilə simulyasiya olunur; real BFF/backend tələb olunmur. Real inteqrasiyanı test etmək üçün `false` edin. |

### BFF — `appsettings.json` / `appsettings.Development.json`

| Ayar | İzah |
|------|------|
| `AdminSettings:SecretPath` | Admin SPA və BFF API-lərinin ortaq, tək-seqmentli prefix-i. 12–64 simvol, yalnız kiçik hərf, rəqəm və defis qəbul edilir. |
| `AdminSettings:LoginRateLimit:PermitLimit` | Eyni client IP-si üçün pəncərə daxilində icazə verilən login sorğularının sayı (defolt: `5`). |
| `AdminSettings:LoginRateLimit:WindowSeconds` | Login rate-limit pəncərəsi, saniyə ilə (defolt: `60`). |
| `AdminSettings:SessionIdleTimeoutMinutes` | Cookie və server sessiyasının hərəkətsizlik müddəti (defolt: `480`). |
| `NexoraApi:BaseUrl` | Əsl Nexora backend API-nin baza ünvanı. **Məcburidir**; yoxdursa tətbiq işə düşərkən xəta verir. |
| `NexoraApi:TimeoutSeconds` | Java API çağırışlarının maksimum gözləmə müddəti (defolt: `30`). |
| `Cors:AllowedOrigins` | Cookie-li sorğulara icazə verilən interfeys mənbələri (məs. `http://localhost:5173`). |
| `ReverseProxy:KnownProxies` | `X-Forwarded-For` / `X-Forwarded-Proto` başlıqlarına etibar ediləcək reverse proxy IP-ləri. Yalnız idarə etdiyiniz proxy-ləri əlavə edin. |

Production-da repo daxilindəki nümunə route-u dəyişmək üçün konfiqurasiyanı source-a yazmaq əvəzinə environment variable istifadə edin:

```bash
AdminSettings__SecretPath=your-random-production-path
```

> **Təhlükəsizlik qeydi:** Secret route panelin avtomatik skanlarla tapılmasını çətinləşdirən əlavə qatdır, authentication mexanizmi deyil. Cookie `HttpOnly` və `SameSite=Strict`-dir, production-da `Secure` bayrağı həmişə aktivdir (`CookieSecurePolicy.Always`), controller-lərdəki authentication və rol yoxlamaları isə qüvvədə qalır. `/admin`, `/admin/login`, `/administrator`, köhnə kök `/api/*` və `/login` ünvanları `404` qaytarır.

> **Çox-instanslı deployment:** Daxili ASP.NET Core rate limiter hər BFF instansı üçün ayrıca sayğac saxlayır. Bir neçə replica işlədilirsə, eyni `5/dəqiqə/IP` limitini gateway/WAF səviyyəsində də tətbiq edin.

---

## Autentifikasiya Axını

1. İstifadəçi interfeysdəki giriş formundan e-poçt + parol göndərir → `POST /{secret-path}/api/auth/login`.
2. BFF kimlik məlumatlarını Nexora backend-ə ötürür.
   - Backend **OTP** tələb edərsə, BFF brauzer sessiyası yaratmadan `OTP_REQUIRED` qaytarır. Tam OTP təsdiq axını ayrıca əlavə olunmalıdır.
3. Təsdiq uğurludursa, BFF backend giriş token-ını server tərəfindəki session store-a yazır və brauzerə yalnız bir sessiya cookie-si qaytarır.
4. Sonrakı hər sorğuda `BackendAuthorizationHandler` sessiyadakı token-ı backend çağırışlarına `Authorization` başlığı kimi əlavə edir.
5. Sessiya məlumatı `GET /{secret-path}/api/auth/me` ilə oxunur; `POST /{secret-path}/api/auth/logout` sessiyanı bitirir.

İcazəsiz və ya vaxtı bitmiş sorğularda BFF HTML yönləndirməsi əvəzinə JSON qaytarır: `401 UNAUTHORIZED` / `403 FORBIDDEN`.

---

## Rollar və İcazələr

İcazələndirmə həm BFF-də (`Auth/Roles.cs`), həm də interfeysdə (`auth/roles.ts`) eyni rol qrupları ilə həyata keçirilir:

| Qrup | Əhatə etdiyi rollar | Nümunə giriş |
|------|---------------------|--------------|
| `adminOnly` | `ADMIN`, `SYSTEM_ADMIN` | İstifadəçilər, ödənişlər, sessiyalar, audit qeydləri |
| `contentManager` | `ADMIN`, `SYSTEM_ADMIN`, `CONTENT_MANAGER` | Kurslar, kateqoriyalar, müəllimlər, CMS məzmunu |
| `salesCrm` | `ADMIN`, `SYSTEM_ADMIN`, `SALES_CRM` | Qeydiyyatlar, kampaniyalar, potensial müştərilər, çat sessiyaları |

İnterfeysdə route-lar `RequireAuth` və `RequireRole` komponentləri ilə qorunur; yan panel naviqasiyası da istifadəçinin roluna görə süzülür.

---

## İdarə Olunan Resurslar

Panel vasitəsilə idarə olunan əsas sahələr:

- **İstifadəçilər və Giriş** — istifadəçilər, OAuth hesabları, sessiyalar, audit qeydləri
- **Akademik Məzmun** — kateqoriyalar, kurslar, müəllimlər, kurs-müəllim əlaqələri, kurs qrupları, kurs rəyləri, məzun hekayələri
- **Məzmun və Bilik** — CMS məzmunu, bilik bazası məqalələri
- **Satış və CRM** — qeydiyyatlar (enrollments), kampaniyalar, potensial müştərilər (leads), əlaqə formaları, çat sessiyaları
- **Maliyyə** — ödənişlər, təqaüdlər
- **Sistem** — bildirişlər, sağlamlıq yoxlaması (health check)

---

## Əmr Arayışı

**İnterfeys** (`NexoraAdminPanelUI/`)

| Əmr | İzah |
|-----|------|
| `npm run dev` | İnkişaf serveri (HMR) |
| `npm run build` | TypeScript kompilyasiyası + production build |
| `npm run preview` | Production build-in lokal önizləməsi |
| `npm run lint` | Oxlint ilə lint |
| `npm run typecheck` | Strict TypeScript yoxlaması |
| `npm run check` | Lint + production build |

**BFF** (`.../NexoraAcademy.AdminBff/`)

| Əmr | İzah |
|-----|------|
| `dotnet run` | Tətbiqi işə sal |
| `dotnet build` | Kompilyasiya et |
| `dotnet restore` | NuGet asılılıqlarını bərpa et |

---

## Əlavə Sənədlər

- [Arxitektura və təhlükəsizlik sərhədləri](docs/ARCHITECTURE.md)
- [Mövcud funksiyalar və prioritetləşdirilmiş çatışmazlıqlar](docs/FEATURE_MATRIX.md)
- [Developer contribution qaydaları](CONTRIBUTING.md)
- [Logo və brand asset qaydaları](docs/BRANDING.md)

---

## Lisenziya

Bu layihə **xüsusi (proprietary)** işdir. Bütün hüquqlar qorunur © Nexora Academy. Depoda ayrıca açıq mənbə lisenziyası göstərilmədiyi müddətdə kod sahibinin yazılı icazəsi olmadan kopyalana, yayıla və ya dəyişdirilə bilməz.

> Layihəni açıq mənbə etmək istəyirsinizsə, kök qovluğa bir `LICENSE` faylı əlavə edin (məs. MIT, Apache-2.0) və yuxarıdakı lisenziya nişanını ona uyğun yeniləyin.
