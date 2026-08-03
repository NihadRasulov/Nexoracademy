# Production — YALNIZ Mənim Etməli Olduqlarım (Docker deploy)

**Tarix:** 2026-07-29 · **Deploy üsulu:** Docker Compose (Kubernetes YOX)

Bu siyahıda **yalnız** mənim (layihə sahibi) edə biləcəyim işlər var — real sirlər,
real domenlər, biznes qərarları və geri dönməz əməliyyatlar. Kod və konfiqurasiya
tərəfi hamısı hazırdır: sadəcə dəyəri doldurmaq / qərar vermək qalır.

Texniki detallar: `PRODUCTION_HANDOVER.md`
İnfrastruktur faylları: `docker-compose.prod.yml`, `deploy/nginx.conf`, `Dockerfile`

> **Vacib:** Docker ilə gedirsinizsə, `.env` faylı **yeganə konfiqurasiya mənbəyidir**.
> `k8s/` qovluğu istifadə olunmur — gələcək üçün saxlanılıb.

---

## 🔴 Bunlarsız deploy etmək OLMAZ

### 1. Ödəniş webhook secret-i
- **Fayl:** `.env` sətir 72 → `PAYMENT_GATEWAY_WEBHOOK_SECRET=` (**boşdur**)
- **Nə etməli:** Ödəniş gateway-ini seç → panelindən webhook secret-ini götür → yaz.
  İmza başlığının adı fərqlidirsə `PAYMENT_GATEWAY_SIGNATURE_HEADER`-i də dəyiş.
- **Niyə mən edə bilmirəm:** Gateway seçimi biznes qərarıdır, secret onun panelindədir.
- **İndiki risk:** Boş olduğu üçün `/api/v1/payments/callback` **imza yoxlanmadan**
  qəbul edilir — istənilən kəs saxta "ödəniş uğurlu" göndərib pulsuz qeydiyyat aça bilər.

### 2. Real domen — 2 dəyər
| Dəyər | Fayl | İndiki (səhv) |
|---|---|---|
| `CORS_ALLOWED_ORIGINS` | `.env:37` | `http://localhost:5500, ...` |
| `FRONTEND_BASE_URL` | `.env:43` | `http://localhost:3000` |

- **Nə yazmalı:** frontend-in real ünvanı, məs. `https://nexoraacademy.az`
- **Risk:** CORS səhv olsa brauzer BÜTÜN sorğuları bloklayır. `FRONTEND_BASE_URL`
  səhv olsa email-lərdəki doğrulama linkləri işləmir.
- **Qeyd:** `CORS_ALLOWED_ORIGINS` prod-da defaultsuzdur — boş qalsa tətbiq açılmır.

### 3. Serverin domeni (nginx)
- **Fayl:** `deploy/nginx.conf` → `api.example.com` (**3 yerdə**)
- **Nə etməli:** Real API domeni yaz + həmin domen üçün DNS **A qeydi** serverin
  IP-sinə yönləndirilsin.
- **Niyə mən edə bilmirəm:** Domen almaq və DNS idarə etmək sənin hesabındadır.

### 4. TLS sertifikatı
- **Qovluq:** `deploy/certs/` → `fullchain.pem` + `privkey.pem`
- **Nə etməli:** Serverdə:
  ```bash
  certbot certonly --standalone -d api.<domen>
  # sonra fullchain.pem və privkey.pem-i deploy/certs/ altına köçür
  ```
- **Niyə mən edə bilmirəm:** Domen sahibliyinin təsdiqi və serverə çıxış lazımdır.
- ⚠️ Sertifikat 90 günə bitir — **avtoyenilənmə (certbot renew cron) qurulmalıdır**,
  yoxsa 3 aydan sonra sayt tam düşür.

### 5. Server (VPS) — hardware və giriş
- **Nə etməli:** Server al (minimum **4 GB RAM, 2 vCPU, 40 GB disk**), Docker +
  Docker Compose qur, SSH açarı ilə giriş qur (parol ilə girişi bağla),
  firewall: yalnız **22, 80, 443** açıq olsun.
- **Niyə mən edə bilmirəm:** Provayder hesabı, ödəniş və SSH açarları sənindir.
- **Qeyd:** Tətbiq + Postgres + nginx eyni maşındadır — RAM 2 GB olsa JVM OOM olur.

### 6. `.env` faylını serverə köçür
- **Nə etməli:** `.env` git-də **yoxdur** (və olmamalıdır). Serverə əl ilə (`scp`)
  köçürməlisən: `scp .env user@server:/opt/nexora/.env`
- Fayl icazəsi: `chmod 600 .env`
- **Niyə mən edə bilmirəm:** Serverin ünvanı və açarları sənindədir.

### 7. Admin hesabı — ilk deploy ardıcıllığı
- **Fayl:** `.env:65` → `ADMIN_SEED_ENABLED=true` (hazırda **artıq true-dur** ✅)
- **Nə etməli:**
  1. İlk deploy `true` ilə → 4 admin hesabı yaranır
  2. Sonra `false` et və `docker compose -f docker-compose.prod.yml up -d app`
- **Vacib:** `.env`-dəki 4 `ADMIN_SEED_*_PASSWORD` dəyərini **parol menecerinə köçür** —
  seeder mövcud hesabın şifrəsini bir daha yeniləmir, itirsən girişi bərpa etmək çətindir.

### 8. Backup qovluğu və saxlama yeri
- **Qovluq:** `./backups` (compose avtomatik yaradır)
- **Nə etməli:** ① Serverdə bu qovluğun diskində yer olduğuna əmin ol
  ② **Backup-ları serverdən KƏNARA köçür** (S3 / başqa server / Google Drive) —
  server ölsə backup da ölür ③ **bir dəfə real bərpa sınağı keçir**:
  ```bash
  pg_restore -d "postgresql://user:pass@localhost/test_restore" --clean --if-exists backups/nexora-*.dump
  ```
- **Niyə mən edə bilmirəm:** Bulud hesabı və prod məlumatı lazımdır.
- **İndiki vəziyyət:** RPO = 24 saat. **Sınanmamış backup = backup yoxdur.**

---

## 🟠 Deploy-dan qısa müddət sonra mütləq

### 9. Gmail → real SMTP provayderi
- **Fayl:** `.env` → `MAIL_HOST`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM`
- **Problem:** Pulsuz Gmail gündə **~500 məktub**. OTP + email doğrulama + şifrə
  bərpası hamısı məktuba bağlıdır → limit dolan gün **qeydiyyat və login tam dayanır**.
- **Nə etməli:** SES / SendGrid / Postmark hesabı aç. **Kod dəyişmir**, yalnız `.env`.
- **Niyə mən edə bilmirəm:** Hesab açmaq + kart + domen doğrulaması lazımdır.
- ⚠️ Həmçinin `MAIL_FROM` real domeninlə eyni olsun və **SPF/DKIM** qur — yoxsa
  məktublar spam-a düşür.

### 10. Chat-bot URL-i (ngrok müvəqqətidir)
- **Fayl:** `.env:78` → `CHATBOT_BASE_URL=https://customary-fading-sensuous.ngrok-free.dev`
- **Problem:** ngrok tuneli bağlananda URL ölür → bot işləməz.
- **Nə etməli:** Bot dostunla razılaş: ya sabit domen, ya botu eyni compose-a servis
  kimi əlavə et (onda `http://chatbot:8000` kifayətdir).
- İstəmirsənsə `CHATBOT_ENABLED=false` et — tətbiq problemsiz işləyir.

### 11. Monitorinq — kimsə xəbər tutmalıdır
- **Vəziyyət:** Metrikalar hazırdır (`:9091/actuator/prometheus`), **amma heç kim
  onlara baxmır və heç bir bildiriş getmir.**
- **Nə etməli (minimum):** Uptime izləyicisi qur (UptimeRobot / BetterStack — pulsuz
  variantı var) → API-yə hər 5 dəqiqədən bir sorğu → düşəndə sənə mail/telegram.
- **Niyə mən edə bilmirəm:** Xarici xidmət hesabı və sənin əlaqə kanalın lazımdır.

---

## 🟡 Vacib, amma deploy-u bloklamır

### 12. Git tarixçəsindəki köhnə sirlər
- **Commit-lər:** `e466ec7`, `a52c18f`, `c8cfc98` — köhnə `.env.example` dəyərləri
- **Nə etməli:** `git filter-repo` və ya BFG + force-push
- **Niyə mən edə bilmirəm:** Tarixçəni yenidən yazan **geri dönməz** əməliyyatdır,
  komanda ilə koordinasiya və force-push tələb edir.
- **Prioritet:** Aşağı — JWT_SECRET və DB_PASSWORD onsuz da rotasiya edilib.
  Repo public olacaqsa prioritet qalxır.

### 13. Rate limiting — tək instansda problem yoxdur
- Docker ilə **bir `app` konteyneri** işlədiyi üçün mövcud limiter tam işləyir ✅
- Əlavə qat kimi nginx-də də limit var (`deploy/nginx.conf`)
- **Yalnız gələcəkdə** `app`-ı çoxaltsan (`--scale app=3`) limit replikaya bölünür —
  o zaman Redis-li limiter lazım olacaq.

### 14. Test HTML faylları
- **Fayllar:** `api-test.html`, `demo-page.html` (repo kökündə, commit olunmayıb)
- **Nə etməli:** Ya sil, ya `docs/` altına köçür. Prod image-ə düşmür, sadəcə səliqə.

---

## ✅ Yekun yoxlama siyahısı

```
[ ] 1.  PAYMENT_GATEWAY_WEBHOOK_SECRET dolduruldu        (.env:72)
[ ] 2.  CORS_ALLOWED_ORIGINS + FRONTEND_BASE_URL         (.env:37,43)
[ ] 3.  nginx domeni + DNS A qeydi                       (deploy/nginx.conf, 3 yer)
[ ] 4.  TLS sertifikatı + certbot renew cron             (deploy/certs/)
[ ] 5.  Server (4GB RAM+), Docker, firewall 22/80/443
[ ] 6.  .env serverə köçürüldü, chmod 600
[ ] 7.  ADMIN_SEED_ENABLED=true → deploy → false; şifrələr saxlanıldı
[ ] 8.  Backup serverdən kənara + bərpa sınağı
[ ] 9.  Real SMTP provayderi + SPF/DKIM
[ ] 10. Chat-bot sabit URL (və ya enabled=false)
[ ] 11. Uptime monitorinqi
[ ] 12. Git tarixçəsi təmizləndi
[ ] 13. (rate limiting — Docker-də hazırda problem yoxdur)
[ ] 14. Test HTML faylları
```

**Minimum canlıya çıxış:** 1–8.
