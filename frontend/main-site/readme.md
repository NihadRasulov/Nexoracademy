Bu xətalar brauzerin Content-Security-Policy (CSP) (Məzmun Təhlükəsizliyi Siyasəti) qaydaları ilə bağlıdır. CSP veb-saytları XSS (Cross-Site Scripting) və digər zərərli kod hücumlarından qorumaq üçün tətbiq edilən təhlükəsizlik tədbiridir.
Lokal mühitdə (localhost) işləyərkən adətən bu qaydalar sərt olmur, lakin layihəni deploy etdikdə (serverə, Vercel, Netlify, Nginx və s. yüklədikdə) server bu təhlükəsizlik başlıqlarını (headers) aktivləşdirir. Brauzer də kodunuzun bu qaydalara zidd olduğunu görüb skriptlərin işləməsini bloklayır, nəticədə saytınız (və ya bəzi funksiyaları) işləmir.
Xətaları və onların həll yollarını aşağıda detallı izah edirəm:
1. "blocked an inline script (script-src-elem) ... violates script-src 'self'"
Səbəb:
Sizin CSP qaydanız (script-src 'self') yalnız xarici .js fayllarının (məsələn: <script src="app.js"></script>) işləməsinə icazə verir. Lakin sizin HTML faylınızın içində birbaşa yazılmış kod (inline script, yəni <script> console.log('salam') </script>) və ya hər hansı bir teqdə onclick="..." kimi bir kod var. Brauzer bunu təhlükəsizlik riski kimi görüb bloklayır.
Həlli:
●	Ən yaxşı yol: Həmin o <script> daxilindəki kodu ayrı bir .js faylına kopyalayın və HTML-ə src ilə qoşun.
●	Alternativ yol: Əgər mütləq HTML içində qalmalıdırsa, xətada sizə təklif olunan hashi ('sha256-1UdpbDzEc+TZL944D+ihDUcGFzQLTHwP1FA0zCS/Qh0=') serverinizin və ya index.html-in <meta http-equiv="Content-Security-Policy"...> teqinin içinə əlavə etməlisiniz.
2. "InstallTrigger is deprecated and will be removed"
Səbəb:
Bu sadəcə Firefox brauzerinə xas olan bir xəbərdarlıqdır (warning). Adətən brauzerinizə yüklədiyiniz hər hansı bir Extension (əlavə) və ya köhnə bir JavaScript kitabxanası tərəfindən yaradılır.
Həlli:
Bu xəta deploy-da problem yaratmır və saytın işləməsinə mane olmur. Buna məhəl qoymaya bilərsiniz. Gizli (Incognito) rejimdə extension-lar olmadan sayta girsəniz, çox güman ki, bu xətanı görməyəcəksiniz.
3. "blocked a JavaScript eval (script-src) ... (Missing 'unsafe-eval')"
Səbəb:
Kodunuzda, istifadə etdiyiniz hər hansı bir NPM paketində və ya React/Vue kimi framework-lərin "build" alətlərində (Webpack, Vite) eval() və ya new Function() istifadə olunur. CSP qaydaları eval()-ı çox təhlükəli hesab edir və standart olaraq bloklayır.
Həlli:
●	Əgər Webpack istifadə edirsinizsə, çox vaxt bu xəta devtool ayarından qaynaqlanır. webpack.config.js faylında devtool: 'eval-source-map' əvəzinə devtool: 'source-map' istifadə edin.
●	Əgər hansısa üçüncü tərəf kitabxanası buna məcbur edirsə (və başqa çarəniz yoxdursa), CSP ayarlarınıza 'unsafe-eval' icazəsini əlavə etməlisiniz.
Nümunə CSP Meta teqi (Əgər HTML-dən idarə edirsinizsə):



HTML
<meta http-equiv="Content-Security-Policy" content="script-src 'self' 'unsafe-inline' 'unsafe-eval';">

(Qeyd: 'unsafe-inline' və 'unsafe-eval' istifadəsi təhlükəsizliyi zəiflədir, lakin problemi sürətli həll etmək üçün serverin ayarlarında və ya HTML-də bu şəkildə dəyişiklik edə bilərsiniz).
Xülasə: Deploy problemini həll etmək üçün ya kodunuzu CSP qaydalarına uyğunlaşdırmalısınız (inline scriptləri təmizləmək, eval-dan qaçmaq), ya da server/hosting tərəfində (Vercel, Netlify, Nginx) CSP qaydalarını bir qədər yumşaltmalısınız.
