# Chat-bot İnteqrasiyası

Bu sənəd qrup yoldaşının öz yazdığı (self-hosted, hazır AI **deyil**) chat-botu
NexoraAcademy backend-inə necə qoşmağı izah edir. Backend tərəfdəki bütün kod
artıq yazılıb — sənin yalnız bir neçə **env dəyəri** doldurmağın (SMTP-dəki kimi)
və botun API formatına uyğun kiçik düzəlişlər etmən lazım gələ bilər.

> ## ✅ Status: inteqrasiya işləyir (son yoxlama: 27.07.2026)
>
> Uçdan-uca test edilib — backend bota qoşulur, cavab alır və frontend-ə qaytarır.
> Detallı test nəticələri üçün bax **§5 Test**, açıq qalan işlər üçün
> **§6 Açıq işlər** və **§7 Qrup yoldaşı üçün TODO**.

---

## 1. Necə işləyir (ümumi məntiq)

```
Frontend ──POST /api/v1/chatbot/message──►  NexoraAcademy backend
                                                     │
                                                     │  (ChatbotService körpüsü)
                                                     ▼
                                       Qrup yoldaşının botu (kənar HTTP xidməti)
                                                     │
                                                     ▼
Frontend ◄──── reply + conversationId ──────  backend cavabı geri ötürür
```

Backend botun **məntiqini saxlamır** — sadəcə frontend-dən gələn mesajı bota
ötürüb cavabı geri qaytaran körpüdür. Bot söndürülübsə və ya əlçatan deyilsə
backend `503` qaytarır və tətbiq yenə də problemsiz işləyir.

---

## 2. Sənin görməli olduğun İŞ (ən qısa yol)

### Addım 1 — Qrup yoldaşından bu 4 sualı öyrən

Botu qoşmaq üçün onun API "müqaviləsini" bilmək lazımdır:

1. **URL nədir?** — məs. `http://192.168.1.50:5000` (onun kompüteri) və ya
   `https://bot.mysite.com` (deploy olunubsa).
2. **Hansı yola (path) və hansı metodla mesaj göndərilir?** — məs. `POST /chat`.
3. **Sorğu gövdəsi (request JSON) hansı formatdadır?** — məs.
   `{ "message": "salam" }` yoxsa `{ "text": "salam" }`?
4. **Cavab (response JSON) hansı formatdadır?** — məs.
   `{ "reply": "..." }` yoxsa `{ "response": "..." }`?
5. **API açarı (token) tələb edirmi?** — tələb edirsə hansı header-də?

### Addım 2 — `.env` faylını doldur

`.env` faylında (artıq əlavə olunub) bu sətirləri redaktə et:

```dotenv
CHATBOT_ENABLED=true
CHATBOT_BASE_URL=http://192.168.1.50:5000     # ← Addım 1-dəki URL
CHATBOT_CHAT_PATH=/chat                         # ← Addım 1-dəki path
CHATBOT_API_KEY=                                # ← açar varsa doldur, yoxdursa boş burax
CHATBOT_API_KEY_HEADER=X-API-Key                # ← açar başqa header-də gedirsə dəyiş
CHATBOT_CONNECT_TIMEOUT_MS=3000
CHATBOT_READ_TIMEOUT_MS=20000                   # ← bot yavaşdırsa artır
```

> **Qeyd:** `CHATBOT_ENABLED=false` qaldıqca bot çağırılmır və endpoint `503`
> qaytarır — tətbiq yenə də normal işə düşür. Bot hazır olanda `true` et.

### Addım 3 — Botun formatı bizimkindən fərqlidirsə (çox güman lazım olmayacaq)

Kod ən çox yayılmış formatları **avtomatik** tutur (aşağıda "Format uyğunlaşdırma"
bölməsinə bax). Yalnız botun formatı tamam fərqlidirsə 2 faylda kiçik düzəliş
edəcəksən. Əksər hallarda **heç nə dəyişməyə ehtiyac yoxdur** — sadəcə env kifayətdir.

### Addım 4 — Yenidən başlat və test et

Backend-i yenidən başlat, sonra "Test" bölməsindəki `curl` ilə yoxla.

---

## 3. Format uyğunlaşdırma (lazım olsa)

### Botun REAL formatı (27.07.2026 tarixli yoxlamaya əsasən)

Bot (`uvicorn` / FastAPI) `POST /api/chat` üzərindən bu formatda cavab qaytarır:

```json
{
  "reply": "Salam əziz dostum! Nexora Academy-nin süni intellekt köməkçisiyəm...",
  "state": "interest_selected",
  "actions": [
    { "type": "button", "label": "Proqramlaşdırma",  "value": "proqramlashdirma" },
    { "type": "button", "label": "Kibertəhlükəsizlik", "value": "kiber" },
    { "type": "button", "label": "Şəbəkə / DevOps",  "value": "shebeke" }
  ],
  "courses": [],
  "capture": "none"
}
```

- `reply` — bizim `ChatbotApiResponse.reply` sahəsi ilə **üst-üstə düşür** ✅
- `conversationId` — bot **qaytarmır**; `ChatbotService` frontend-in göndərdiyini
  geri qaytarır ki, zəncir qırılmasın.
- `state` / `actions` / `courses` / `capture` — bot qaytarır, amma backend hazırda
  bunları **atır** (`@JsonIgnoreProperties(ignoreUnknown = true)`). Bax **§6 Açıq işlər**.

### Sorğu (bizdən bota gedən JSON)

Backend bota bu formatda göndərir:

```json
{ "message": "istifadəçi mesajı", "conversationId": "...", "userId": "..." }
```

Bot **başqa açar** gözləyirsə (məs. `message` yox, `text`), bu faylı redaktə et:

`src/main/java/az/demo/NexoraAcademy/dto/ai/ChatbotApiRequest.java`

Sahə adını dəyiş, məsələn:

```java
public record ChatbotApiRequest(
        String text,            // "message" idi → bota uyğun "text" oldu
        String conversationId,
        String userId
) {}
```

(Və `ChatbotService`-də `new ChatbotApiRequest(...)` çağırışını uyğunlaşdır.)

### Cavab (botdan bizə gələn JSON)

Backend cavabdakı mətn sahəsini **avtomatik** tapır — bot bunlardan hansını
qaytarsa da işləyir: `reply`, `response`, `answer`, `message`, `text`, `output`.
Söhbət id-si üçün: `conversationId`, `conversation_id`, `session_id`, `sessionId`,
`chatId`, `chat_id`.

Bot bunların heç birinə uyğun gəlməyən ad işlədirsə (məs. iç-içə `data.reply`),
bu faylı redaktə et:

`src/main/java/az/demo/NexoraAcademy/dto/ai/ChatbotApiResponse.java`

`@JsonAlias(...)` siyahısına botun açarını əlavə et — kifayətdir.

---

## 4. Frontend qoşulması (mənim/frontend komandasının işi)

Frontend birbaşa bota YOX, **bizim backend-ə** müraciət edir:

### Endpoint

```
POST http://localhost:8081/api/v1/chatbot/message
```

### Header-lər

```
Content-Type: application/json
Authorization: Bearer <JWT_ACCESS_TOKEN>
```

> Hazırda endpoint **login tələb edir** (bax "Təhlükəsizlik" bölməsi). Yəni
> istifadəçi əvvəlcə `/api/v1/auth/...` ilə token almalı, sonra o token-i bu
> sorğunun `Authorization` header-ində göndərməlidir.

### Sorğu gövdəsi

```json
{
  "message": "Salam, kurslar haqqında məlumat verə bilərsən?",
  "conversationId": null
}
```

- `message` — istifadəçinin yazdığı mətn (məcburi, maks. 4000 simvol).
- `conversationId` — ilk mesajda `null` göndər. Cavabda qayıdan id-ni saxla və
  növbəti mesajlarda geri göndər ki, söhbətin konteksti qorunsun.

### Cavab

```json
{
  "reply": "Əlbəttə! Hansı istiqamət maraqlıdır?",
  "conversationId": "abc-123"
}
```

- `reply` — ekranda göstəriləcək bot cavabı.
- `conversationId` — yadda saxla, növbəti sorğuda geri göndər.

### Xəta halları

| Status | Mənası |
|--------|--------|
| `400`  | `message` boşdur və ya 4000 simvoldan uzundur |
| `401`  | Token yoxdur/etibarsızdır (əvvəlcə login lazımdır) |
| `503`  | Bot söndürülüb (`CHATBOT_ENABLED=false`) və ya əlçatan deyil (timeout/yanlış URL) |

### Frontend nümunə (JavaScript `fetch`)

```javascript
async function sendToChatbot(message, conversationId, accessToken) {
  const res = await fetch("http://localhost:8081/api/v1/chatbot/message", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "Authorization": `Bearer ${accessToken}`,
    },
    body: JSON.stringify({ message, conversationId }),
  });

  if (!res.ok) {
    if (res.status === 503) throw new Error("Bot hazırda əlçatan deyil");
    throw new Error("Chatbot xətası: " + res.status);
  }
  return res.json(); // { reply, conversationId }
}
```

---

## 5. Test (backend hazır olduğunu yoxlamaq)

Əvvəlcə login edib access token al, sonra:

```bash
curl -X POST http://localhost:8081/api/v1/chatbot/message \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -d '{"message":"salam","conversationId":null}'
```

- `503` gəlirsə → `CHATBOT_ENABLED` və `CHATBOT_BASE_URL`-i yoxla, backend-i
  yenidən başlat, botun işlədiyinə əmin ol.
- `401` gəlirsə → token yanlış/yoxdur.
- `reply` gəlirsə → 🎉 hər şey işləyir.

Swagger UI-də də görünür: `http://localhost:8081/swagger-ui/index.html` → **Chatbot**.

### Token almaq (test üçün seed olunmuş hesab)

```bash
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"system-admin@nexora.com","password":"system-admin1234"}'
```

(Şifrələr `.env`-dəki `ADMIN_SEED_*` dəyərlərindən gəlir — bax `config/AdminSeeder.java`.)

### Son test nəticələri — 27.07.2026

Konfiqurasiya: `CHATBOT_ENABLED=true`,
`CHATBOT_BASE_URL=https://customary-fading-sensuous.ngrok-free.dev`,
`CHATBOT_CHAT_PATH=/api/chat`, açar yoxdur.

| # | Ssenari | Gözlənilən | Nəticə |
|---|---------|-----------|--------|
| 1 | Bota **birbaşa** `POST /api/chat` (backend-siz) | 200 + `reply` | ✅ 200, ~5 san |
| 2 | `POST /api/v1/chatbot/message`, token ilə, `conversationId: null` | 200 + `reply` | ✅ 200, ~7.8 san |
| 3 | Davam mesajı (`conversationId: "conv-abc-123"`) | 200, kontekst qorunur | ✅ 200, ~2.9 san |
| 4 | Tokensiz sorğu | 401 | ✅ 401 |
| 5 | Boş `message` | 400 validation | ✅ 400 (`message: must not be blank`) |

Bot Nexora kurslarını, qiymətlərini və səviyyələrini düzgün qaytarır — körpü
uçdan-uca işləyir.

**Aşkarlanan risk:** testlər zamanı bir sorğu **25 saniyədən çox** çəkdi (LLM
gecikməsi). Adi hal 3–8 saniyədir, amma ara-sıra uzanır. Cari
`CHATBOT_READ_TIMEOUT_MS=20000` bu spike-larda 503 verəcək — bax **§6**.

---

## 6. Açıq işlər (backend tərəfi)

### 6.1 Botun `actions` / `courses` / `state` sahələri itir — 🔴 vacib

Bot cavabında istifadəçiyə göstəriləcək **düymələr** (`actions`) və kurs siyahısı
(`courses`) gəlir, amma `ChatbotApiResponse` yalnız `reply` və `conversationId`
oxuyur — qalanı `ignoreUnknown` ilə atılır. Nəticə: frontend widget-də yalnız
mətn görünəcək, **düymələr görünməyəcək**, halbuki botun UX-i düymələr üzərində
qurulub.

Düzəliş üçün dəyişməli olan fayllar:

| Fayl | Nə etməli |
|------|-----------|
| `dto/ai/ChatbotApiResponse.java` | `state`, `actions`, `courses`, `capture` sahələrini əlavə et (+ `ChatbotAction` record) |
| `dto/ai/ChatbotMessageResponse.java` | eyni sahələri frontend cavabına ötür |
| `service/ai/ChatbotService.java` | `new ChatbotMessageResponse(...)` çağırışını uyğunlaşdır |

Bundan sonra `POST /api/v1/chatbot/message` cavabı belə olacaq:

```json
{
  "reply": "...",
  "conversationId": "conv-abc-123",
  "state": "interest_selected",
  "actions": [{ "type": "button", "label": "Proqramlaşdırma", "value": "proqramlashdirma" }],
  "courses": [],
  "capture": "none"
}
```

### 6.2 `CHATBOT_READ_TIMEOUT_MS` azdır — 🟡 orta

`.env`-də `20000` → **`45000`** et. Səbəb: §5-dəki 25+ saniyəlik spike.

```dotenv
CHATBOT_READ_TIMEOUT_MS=45000
```

### 6.3 ngrok free URL sabit deyil — 🟡 orta

`customary-fading-sensuous.ngrok-free.dev` pulsuz tuneldir — qrup yoldaşı botu
yenidən başladanda **URL dəyişir** və backend 503 verməyə başlayır. Test üçün
uyğundur, **prod üçün deyil**. Prod-da bot sabit domenə (və ya eyni k8s/docker
şəbəkəsinə) deploy olunmalı, sonra `CHATBOT_BASE_URL` ona yönəldilməlidir.

### 6.4 Public widget üçün rate-limit — 🟡 (yalnız `.permitAll()` edilsə)

Endpoint public edilərsə (bax §8), IP üzrə rate-limit əlavə olunmalıdır —
`security/AuthRateLimitingFilter` nümunə kimi götürülə bilər.

---

## 7. Qrup yoldaşı üçün TODO (bot tərəfi)

Aşağıdakılar **botun kodunda** həll olunmalıdır — backend tərəfdən düzəldilə bilməz.

### 7.1 Sessiya ayrılığı yoxdur — 🔴 kritik

**Problem:** bot `conversationId` (və ya `session_id`) sahəsini nə qəbul edir, nə də
qaytarır. Testdə fərqli `conversationId` göndərilməsinə baxmayaraq botun `state`
dəyəri ardıcıl sorğularda dəyişirdi (`interest_selected` → `level_selected`) —
yəni bot söhbət vəziyyətini **qlobal (bütün istifadəçilər üçün ortaq)** saxlayır.

**Nəticəsi:** iki istifadəçi eyni anda yazsa, söhbətlər bir-birinə qarışacaq —
biri "Proqramlaşdırma" seçəndə digərinin botu da səviyyə soruşmağa keçəcək.

**Nə etməli:** state qlobal dəyişəndə yox, `conversationId` açarı üzrə dictionary /
Redis-də saxlanmalıdır. Backend hər sorğuda bu sahələri artıq göndərir:

```json
{ "message": "...", "conversationId": "...", "userId": "..." }
```

- `conversationId` — söhbəti tanımaq üçün açar. Boş gəlirsə, bot **yeni id yaratmalı**
  və cavabda `conversationId` (və ya `session_id`) sahəsində geri qaytarmalıdır.
- `userId` — daxil olmuş istifadəçinin UUID-si (loglama/analitika üçün, məcburi deyil).

Cavabda `conversationId` qaytarılan kimi backend tərəfdə **heç nə dəyişmək lazım
deyil** — `ChatbotApiResponse` onu `session_id`, `sessionId`, `chat_id`, `chatId`
adları ilə də avtomatik tutur.

### 7.2 Cavab sürəti — 🟡

Ara-sıra 25 saniyədən çox çəkir. Mümkünsə:
- LLM cavabını **stream** etmək və ya model/prompt-u qısaltmaq,
- tez-tez təkrarlanan suallara **keş** qoymaq,
- ən azı hansı mərhələnin (LLM? DB?) yavaş olduğunu loglamaq.

### 7.3 Sabit deploy — 🟡

ngrok pulsuz tunel əvəzinə sabit ünvan lazımdır (bax §6.3). Variantlar:
öz serverində domen + HTTPS, yaxud botu bizim `docker-compose` / `k8s` stack-inə
container kimi əlavə etmək (onda URL `http://chatbot:5000` kimi daxili olur).

### 7.4 API açarı — 🟢 tövsiyə

Hazırda bot açarsızdır — ngrok URL-ni bilən hər kəs birbaşa bota sorğu ata bilər.
Sadə bir `X-API-Key` yoxlaması əlavə etsə, backend tərəfdə sadəcə `.env`-də
`CHATBOT_API_KEY=...` doldurulacaq, **kod dəyişmir**.

### 7.5 Sağlamlıq endpoint-i — 🟢 tövsiyə

`GET /health` → `{"status":"ok"}` kimi sadə endpoint əlavə etsin — botun ayaqda
olub-olmadığını monitorinq edə bilərik.

### Bota qoyulan tələblərin xülasəsi

| # | Tələb | Prioritet |
|---|-------|-----------|
| 1 | `conversationId` üzrə sessiya ayrılığı + cavabda geri qaytarmaq | 🔴 kritik |
| 2 | Sabit deploy (ngrok free deyil) | 🟡 orta |
| 3 | Cavab müddətini 20 san-dən aşağı saxlamaq | 🟡 orta |
| 4 | `X-API-Key` autentifikasiyası | 🟢 tövsiyə |
| 5 | `GET /health` endpoint-i | 🟢 tövsiyə |

---

## 8. Təhlükəsizlik — botu girişsiz (public) etmək

Hazırda `/api/v1/chatbot/**` **login tələb edir** (yalnız daxil olmuş istifadəçilər
mesaj göndərə bilər). Botu saytda hər kəsə açıq widget kimi (login olmadan) istifadə
etmək istəsən:

`src/main/java/az/demo/NexoraAcademy/config/SecurityConfig.java` faylında bu bloku tap:

```java
.requestMatchers("/api/v1/chatbot/**").authenticated()
```

və `.authenticated()` yerinə `.permitAll()` yaz. Onda token lazım olmayacaq
(frontend `Authorization` header-i göndərməyəcək), qalan hər şey eyni işləyəcək.

> Diqqət: public etsən, bot xərci/sui-istifadəyə qarşı rate-limit düşünmək
> lazımdır — layihədə artıq `AuthRateLimitingFilter` var, oranı nümunə götür.

---

## 9. Nə əlavə olundu (arayış üçün)

Yeni fayllar:

| Fayl | Rolu |
|------|------|
| `config/ChatbotProperties.java` | env dəyərlərini oxuyan config (prefix `app.chatbot`) |
| `config/ChatbotClientConfig.java` | bota HTTP sorğu üçün `RestClient` (timeout-larla) |
| `dto/ai/ChatbotMessageRequest.java` | frontend → backend sorğusu |
| `dto/ai/ChatbotMessageResponse.java` | backend → frontend cavabı |
| `dto/ai/ChatbotApiRequest.java` | backend → **bot** sorğusu (formatı burada uyğunlaşdırılır) |
| `dto/ai/ChatbotApiResponse.java` | **bot** → backend cavabı (formatı burada uyğunlaşdırılır) |
| `service/ai/ChatbotService.java` | əsas körpü məntiqi + xəta idarəsi |
| `controller/ai/ChatbotController.java` | `POST /api/v1/chatbot/message` endpoint-i |
| `exception/ChatbotUnavailableException.java` | bot əlçatmaz olanda `503` |

Dəyişdirilən fayllar:

| Fayl | Dəyişiklik |
|------|-----------|
| `resources/application.yml` | `app.chatbot` config bloku |
| `config/SecurityConfig.java` | `/api/v1/chatbot/**` üçün icazə qaydası |
| `.env` / `.env.example` | `CHATBOT_*` dəyişənləri |

---

## Xülasə — mənim görməli olduğum minimum

1. Qrup yoldaşından **URL + path + request/response formatını** öyrən.
2. `.env`-də `CHATBOT_ENABLED=true` et və `CHATBOT_BASE_URL`-i doldur.
3. Lazım olsa `ChatbotApiRequest`/`ChatbotApiResponse`-də sahə adlarını uyğunlaşdır.
4. Backend-i yenidən başlat, `curl` ilə test et.
5. Frontend-i `POST /api/v1/chatbot/message`-ə qoş (JWT token ilə).
