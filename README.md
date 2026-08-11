# Nexora Academy — Admin Panel

![.NET](https://img.shields.io/badge/.NET-10-512BD4?logo=dotnet&logoColor=white)
![React](https://img.shields.io/badge/React-19-61DAFB?logo=react&logoColor=black)
![TypeScript](https://img.shields.io/badge/TypeScript-5%2B-3178C6?logo=typescript&logoColor=white)
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
- Bu dizayn sayəsində giriş token-ları heç vaxt brauzerə çatmır (XSS-ə qarşı token sızması riski aradan qalxır).

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

**İnterfeys — `frontend/admin-ui/`**

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

**BFF — `backend/admin-bff/NexoraAdminPanel/`**

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
├── backend/
│   ├── api/                          # Spring Boot (əsas API)
│   │   └── src/main/java/...         # Kontrollerlər, servislər, migrasiya-lar
│   ├── chatbot-api/                  # Python FastAPI (AI chatbot)
│   └── admin-bff/                    # Admin BFF (backend)
│       └── NexoraAdminPanel/
│           ├── NexoraAdminPanel.sln
│           └── src/NexoraAcademy.AdminBff/
│               ├── Program.cs                  # DI, auth, CORS, HttpClient qeydiyyatları
│               ├── Controllers/                # BFF son nöqtələri (Auth, User, Course, ...)
│               ├── Clients/                    # Nexora backend-ə gedən tipli HTTP klientləri
│               ├── Auth/                        # Session store, icazə handler-i, rollar
│               ├── Contracts/                   # Backend/Bff sorğu-cavab modelləri
│               ├── Middleware/                  # Mərkəzi xəta idarəetməsi
│               └── appsettings*.json
│
└── frontend/
    ├── main-site/                    # Əsas veb səhifə (frontend)
    └── admin-ui/                     # İnterfeys (frontend)
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
cd backend/admin-bff/NexoraAdminPanel/NexoraAdminPanel/src/NexoraAcademy.AdminBff
dotnet restore
dotnet run
```

Defolt ünvanlar:
- HTTP: `http://localhost:5075`
- HTTPS: `https://localhost:7280`

OpenAPI sənədi yalnız Development mühitində `/openapi` altında yayımlanır.

### 2) İnterfeys (frontend)

```bash
cd frontend/admin-ui
npm install
npm run dev
```

İnterfeys defolt olaraq `http://localhost:5173` üzərində işləyir (BFF-in CORS ağ siyahısında `5173` və `3000` mövcuddur).

---

## Konfiqurasiya

### İnterfeys — `frontend/admin-ui/.env.development`

| Dəyişən | İzah |
|---------|------|
| `VITE_API_BASE_URL` | BFF-in ünvanı (məs. `http://localhost:5075`) |
| `VITE_DEMO_MODE` | `true` olduqda bütün `/api` çağırışları `src/lib/demo/` altındakı saxta data ilə simulyasiya olunur; real BFF/backend tələb olunmur. Real inteqrasiyanı test etmək üçün `false` edin. |

### BFF — `appsettings.json` / `appsettings.Development.json`

| Ayar | İzah |
|------|------|
| `NexoraApi:BaseUrl` | Əsl Nexora backend API-nin baza ünvanı. **Məcburidir**; yoxdursa tətbiq işə düşərkən xəta verir. |
| `Cors:AllowedOrigins` | Cookie-li sorğulara icazə verilən interfeys mənbələri (məs. `http://localhost:5173`). |

> **Təhlükəsizlik qeydi:** Cookie `HttpOnly` və `SameSite=Strict`-dir. Production-da `Secure` bayrağı həmişə aktivdir (`CookieSecurePolicy.Always`); Development-da tələbata görə yumşaldılır.

---

## Autentifikasiya Axını

1. İstifadəçi interfeysdəki giriş formundan e-poçt + parol göndərir → `POST /api/v1/auth/login`.
2. Spring Backend **OTP** tələb edərsə, `OTP_REQUIRED` qaytarır. Tam OTP təsdiq axını ayrıca əlavə olunmalıdır.
3. Təsdiq uğurludursa, backend giriş token-larını (access + refresh) qaytarır.
4. Sonrakı hər sorğuda admin UI backend-ə Bearer token ilə sorğu göndərir.
5. İstifadəçi məlumatı `GET /api/v1/users/me` ilə oxunur; `POST /api/v1/auth/logout` sessiyanı bitirir.

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

**İnterfeys** (`frontend/admin-ui/`)

| Əmr | İzah |
|-----|------|
| `npm run dev` | İnkişaf serveri (HMR) |
| `npm run build` | TypeScript kompilyasiyası + production build |
| `npm run preview` | Production build-in lokal önizləməsi |
| `npm run lint` | Oxlint ilə lint |

**BFF** (`backend/admin-bff/NexoraAdminPanel/NexoraAdminPanel/src/NexoraAcademy.AdminBff/`)

| Əmr | İzah |
|-----|------|
| `dotnet run` | Tətbiqi işə sal |
| `dotnet build` | Kompilyasiya et |
| `dotnet restore` | NuGet asılılıqlarını bərpa et |

---

## Lisenziya

Bu layihə **xüsusi (proprietary)** işdir. Bütün hüquqlar qorunur © Nexora Academy. Depoda ayrıca açıq mənbə lisenziyası göstərilmədiyi müddətdə kod sahibinin yazılı icazəsi olmadan kopyalana, yayıla və ya dəyişdirilə bilməz.

> Layihəni açıq mənbə etmək istəyirsinizsə, kök qovluğa bir `LICENSE` faylı əlavə edin (məs. MIT, Apache-2.0) və yuxarıdakı lisenziya nişanını ona uyğun yeniləyin.
