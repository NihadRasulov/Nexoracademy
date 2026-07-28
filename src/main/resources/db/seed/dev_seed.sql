-- =====================================================================
--  dev_seed.sql — DEV/TEST üçün saxta (fake) data
-- =====================================================================
--  Məqsəd: frontend komandası bütün ekranları real məzmunla qura bilsin.
--  Miqrasiya DEYİL — qəsdən db/migration-dan KƏNARDA saxlanılır ki, Flyway
--  onu avtomatik işlətməsin və heç vaxt prod-a düşməsin.
--
--  İşə salmaq:
--    docker exec -i nexora-postgres psql -U nexora_app -d nexora_academy \
--      < src/main/resources/db/seed/dev_seed.sql
--
--  ⚠️  DİQQƏT — bu skript aşağıdakı cədvəlləri ƏVVƏLCƏ TAM TƏMİZLƏYİR
--  (təkrar-təkrar işlədilə bilsin deyə). Yalnız 4 admin hesabı (AdminSeeder
--  tərəfindən yaradılan admin@/system-admin@/sales-crm@/content-manager@)
--  toxunulmaz qalır. REAL data olan bazada İŞLƏTMƏ.
--
--  Bütün saxta istifadəçilərin şifrəsi:  student1234
--
--  ⚠️ TARİXÇƏ: ilk versiyada hash `admin@nexora.com`-dan kopyalanmışdı və şifrəsinin
--  "admin1234" olduğu güman edilirdi. Bu SƏHV idi — həmin hesab AdminSeeder-in
--  legacy miqrasiyasından gəlir və hash başqa şifrəyə aiddir. Nəticədə seed edilən
--  24 istifadəçinin heç birinə login etmək mümkün olmurdu (401). 28.07.2026-da
--  düzəldildi: aşağıdakı hash BCrypt("student1234")-dür və doğrulanıb.
--
--  QEYD: STUDENT/GUEST rolu login-də OTP tələb edir (bax AuthService) — şifrə düz
--  olsa belə cavabda token yox, "6-digit code sent" mesajı gəlir və kod e-poçta
--  göndərilir. Bu ünvanlar (@nexora-test.az) real deyil, ona görə kod çatmayacaq:
--    · OTP-siz test üçün → admin hesablarından istifadə et (.env ADMIN_SEED_*)
--    · Tələbə axınını sınamaq üçün → .env-də MAIL_HOST=localhost, MAIL_PORT=587,
--      MAIL_SMTP_AUTH=false, MAIL_SMTP_STARTTLS=false et (MailHog-a yönəlir),
--      sonra kodu http://localhost:8025 ünvanından oxu.
-- =====================================================================

BEGIN;

-- ---------------------------------------------------------------------
-- 0. Təmizlik — FK asılılıq sırası ilə
-- ---------------------------------------------------------------------
DELETE FROM platform.audit_logs;
DELETE FROM notify.notifications;
DELETE FROM outcomes.graduate_outcomes;
DELETE FROM outcomes.course_reviews;
DELETE FROM billing.payments;
DELETE FROM academics.enrollments;
DELETE FROM academics.course_groups;
DELETE FROM crm.chat_sessions;
DELETE FROM crm.contact_submissions;
DELETE FROM crm.leads;
DELETE FROM crm.campaigns;
DELETE FROM catalog.course_instructors;
DELETE FROM catalog.instructors;
DELETE FROM catalog.courses;
DELETE FROM catalog.categories;
DELETE FROM cms.cms_content;
DELETE FROM ai.kb_articles;
DELETE FROM identity.sessions;
DELETE FROM identity.oauth_accounts;
DELETE FROM billing.scholarships;
DELETE FROM identity.users
 WHERE email NOT IN ('admin@nexora.com','system-admin@nexora.com',
                     'sales-crm@nexora.com','content-manager@nexora.com');

-- ---------------------------------------------------------------------
-- 1. identity.users — 20 tələbə + 2 müəllim hesabı + 2 qonaq
--    password_hash = BCrypt("admin1234")
-- ---------------------------------------------------------------------
INSERT INTO identity.users
  (id, email, email_verified_at, phone, full_name, password_hash, role, status, locale, profile, last_login_at, created_at)
VALUES
 ('a0000000-0000-4000-8000-000000000001','aysel.mammadova@nexora-test.az', now()-interval '120 days','+994501112201','Aysel Məmmədova','$2b$10$pYmfcxfiqVssARHvw8VX..Kuq/04TVUd1fTQVM/8.iBTRfH.bsTzC','student','active','az-AZ','{"city":"Bakı","birthYear":1999,"interests":["web","design"]}', now()-interval '2 days',  now()-interval '120 days'),
 ('a0000000-0000-4000-8000-000000000002','reshad.aliyev@nexora-test.az',   now()-interval '115 days','+994501112202','Rəşad Əliyev',    '$2b$10$pYmfcxfiqVssARHvw8VX..Kuq/04TVUd1fTQVM/8.iBTRfH.bsTzC','student','active','az-AZ','{"city":"Bakı","birthYear":1997,"interests":["security"]}',      now()-interval '5 days',  now()-interval '115 days'),
 ('a0000000-0000-4000-8000-000000000003','nigar.huseynova@nexora-test.az', now()-interval '110 days','+994501112203','Nigar Hüseynova', '$2b$10$pYmfcxfiqVssARHvw8VX..Kuq/04TVUd1fTQVM/8.iBTRfH.bsTzC','student','active','az-AZ','{"city":"Gəncə","birthYear":2001,"interests":["data"]}',        now()-interval '1 day',   now()-interval '110 days'),
 ('a0000000-0000-4000-8000-000000000004','elvin.quliyev@nexora-test.az',   now()-interval '100 days','+994501112204','Elvin Quliyev',   '$2b$10$pYmfcxfiqVssARHvw8VX..Kuq/04TVUd1fTQVM/8.iBTRfH.bsTzC','student','active','az-AZ','{"city":"Bakı","birthYear":1995,"interests":["devops"]}',      now()-interval '9 days',  now()-interval '100 days'),
 ('a0000000-0000-4000-8000-000000000005','gunel.safarova@nexora-test.az',  now()-interval '95 days', '+994501112205','Günel Səfərova',  '$2b$10$pYmfcxfiqVssARHvw8VX..Kuq/04TVUd1fTQVM/8.iBTRfH.bsTzC','student','active','az-AZ','{"city":"Sumqayıt","birthYear":2000,"interests":["mobile"]}',  now()-interval '3 days',  now()-interval '95 days'),
 ('a0000000-0000-4000-8000-000000000006','tural.bayramov@nexora-test.az',  now()-interval '90 days', '+994501112206','Tural Bayramov',  '$2b$10$pYmfcxfiqVssARHvw8VX..Kuq/04TVUd1fTQVM/8.iBTRfH.bsTzC','student','active','az-AZ','{"city":"Bakı","birthYear":1998}',                            now()-interval '12 days', now()-interval '90 days'),
 ('a0000000-0000-4000-8000-000000000007','leyla.ismayilova@nexora-test.az',now()-interval '85 days', '+994501112207','Leyla İsmayılova','$2b$10$pYmfcxfiqVssARHvw8VX..Kuq/04TVUd1fTQVM/8.iBTRfH.bsTzC','student','active','az-AZ','{"city":"Bakı","birthYear":2002}',                            now()-interval '4 days',  now()-interval '85 days'),
 ('a0000000-0000-4000-8000-000000000008','orxan.nabiyev@nexora-test.az',   now()-interval '80 days', '+994501112208','Orxan Nəbiyev',   '$2b$10$pYmfcxfiqVssARHvw8VX..Kuq/04TVUd1fTQVM/8.iBTRfH.bsTzC','student','active','az-AZ','{"city":"Şəki","birthYear":1996}',                            now()-interval '20 days', now()-interval '80 days'),
 ('a0000000-0000-4000-8000-000000000009','sabina.karimova@nexora-test.az', now()-interval '75 days', '+994501112209','Səbinə Kərimova', '$2b$10$pYmfcxfiqVssARHvw8VX..Kuq/04TVUd1fTQVM/8.iBTRfH.bsTzC','student','active','az-AZ','{"city":"Bakı","birthYear":1999}',                            now()-interval '7 days',  now()-interval '75 days'),
 ('a0000000-0000-4000-8000-000000000010','kamran.rzayev@nexora-test.az',   now()-interval '70 days', '+994501112210','Kamran Rzayev',   '$2b$10$pYmfcxfiqVssARHvw8VX..Kuq/04TVUd1fTQVM/8.iBTRfH.bsTzC','student','active','az-AZ','{"city":"Bakı","birthYear":1994}',                            now()-interval '6 days',  now()-interval '70 days'),
 ('a0000000-0000-4000-8000-000000000011','fidan.abbasova@nexora-test.az',  now()-interval '65 days', '+994501112211','Fidan Abbasova',  '$2b$10$pYmfcxfiqVssARHvw8VX..Kuq/04TVUd1fTQVM/8.iBTRfH.bsTzC','student','active','az-AZ','{"city":"Mingəçevir","birthYear":2003}',                      now()-interval '11 days', now()-interval '65 days'),
 ('a0000000-0000-4000-8000-000000000012','nihad.rasulov@nexora-test.az',   now()-interval '60 days', '+994501112212','Nihad Rəsulov',   '$2b$10$pYmfcxfiqVssARHvw8VX..Kuq/04TVUd1fTQVM/8.iBTRfH.bsTzC','student','active','az-AZ','{"city":"Bakı","birthYear":2000}',                            now()-interval '1 day',   now()-interval '60 days'),
 ('a0000000-0000-4000-8000-000000000013','ulviyya.jafarova@nexora-test.az',now()-interval '55 days', '+994501112213','Ülviyyə Cəfərova','$2b$10$pYmfcxfiqVssARHvw8VX..Kuq/04TVUd1fTQVM/8.iBTRfH.bsTzC','student','active','az-AZ','{"city":"Bakı","birthYear":1997}',                            now()-interval '14 days', now()-interval '55 days'),
 ('a0000000-0000-4000-8000-000000000014','emin.sultanov@nexora-test.az',   now()-interval '50 days', '+994501112214','Emin Sultanov',   '$2b$10$pYmfcxfiqVssARHvw8VX..Kuq/04TVUd1fTQVM/8.iBTRfH.bsTzC','student','active','az-AZ','{"city":"Lənkəran","birthYear":1993}',                        now()-interval '8 days',  now()-interval '50 days'),
 ('a0000000-0000-4000-8000-000000000015','zeynab.mirzayeva@nexora-test.az',now()-interval '45 days', '+994501112215','Zeynəb Mirzəyeva','$2b$10$pYmfcxfiqVssARHvw8VX..Kuq/04TVUd1fTQVM/8.iBTRfH.bsTzC','student','active','az-AZ','{"city":"Bakı","birthYear":2001}',                            now()-interval '2 days',  now()-interval '45 days'),
 ('a0000000-0000-4000-8000-000000000016','ramin.hasanov@nexora-test.az',   now()-interval '40 days', '+994501112216','Ramin Həsənov',   '$2b$10$pYmfcxfiqVssARHvw8VX..Kuq/04TVUd1fTQVM/8.iBTRfH.bsTzC','student','active','az-AZ','{"city":"Bakı","birthYear":1998}',                            now()-interval '16 days', now()-interval '40 days'),
 ('a0000000-0000-4000-8000-000000000017','aytan.valiyeva@nexora-test.az',  now()-interval '35 days', '+994501112217','Aytən Vəliyeva',  '$2b$10$pYmfcxfiqVssARHvw8VX..Kuq/04TVUd1fTQVM/8.iBTRfH.bsTzC','student','active','az-AZ','{"city":"Gəncə","birthYear":2002}',                           now()-interval '3 days',  now()-interval '35 days'),
 ('a0000000-0000-4000-8000-000000000018','murad.shirinov@nexora-test.az',  NULL,                     '+994501112218','Murad Şirinov',   '$2b$10$pYmfcxfiqVssARHvw8VX..Kuq/04TVUd1fTQVM/8.iBTRfH.bsTzC','student','pending_verification','az-AZ','{"city":"Bakı"}',                       NULL,                     now()-interval '6 days'),
 ('a0000000-0000-4000-8000-000000000019','lala.nasirova@nexora-test.az',   now()-interval '25 days', '+994501112219','Lalə Nəsirova',   '$2b$10$pYmfcxfiqVssARHvw8VX..Kuq/04TVUd1fTQVM/8.iBTRfH.bsTzC','student','suspended','az-AZ','{"city":"Bakı","note":"ödəniş gecikməsi"}',                 now()-interval '22 days', now()-interval '25 days'),
 ('a0000000-0000-4000-8000-000000000020','javid.ahmadov@nexora-test.az',   now()-interval '20 days', '+994501112220','Cavid Əhmədov',   '$2b$10$pYmfcxfiqVssARHvw8VX..Kuq/04TVUd1fTQVM/8.iBTRfH.bsTzC','student','active','az-AZ','{"city":"Bakı","birthYear":1996}',                            now()-interval '1 day',   now()-interval '20 days'),
 -- müəllim hesabları (rol: content_manager — enum-da ayrıca "instructor" rolu yoxdur)
 ('a0000000-0000-4000-8000-000000000021','elchin.mammadov@nexora-test.az', now()-interval '300 days','+994501112221','Dr. Elçin Məmmədov','$2b$10$pYmfcxfiqVssARHvw8VX..Kuq/04TVUd1fTQVM/8.iBTRfH.bsTzC','content_manager','active','az-AZ','{"title":"Baş müəllim"}', now()-interval '1 day', now()-interval '300 days'),
 ('a0000000-0000-4000-8000-000000000022','aynur.hasanova@nexora-test.az',  now()-interval '280 days','+994501112222','Aynur Həsənova',   '$2b$10$pYmfcxfiqVssARHvw8VX..Kuq/04TVUd1fTQVM/8.iBTRfH.bsTzC','content_manager','active','az-AZ','{"title":"Data mentoru"}', now()-interval '2 days', now()-interval '280 days'),
 -- qonaqlar
 ('a0000000-0000-4000-8000-000000000023','guest.one@nexora-test.az',       NULL, NULL,'Qonaq İstifadəçi 1','$2b$10$pYmfcxfiqVssARHvw8VX..Kuq/04TVUd1fTQVM/8.iBTRfH.bsTzC','guest','active','az-AZ','{}', NULL, now()-interval '10 days'),
 ('a0000000-0000-4000-8000-000000000024','guest.two@nexora-test.az',       NULL, NULL,'Qonaq İstifadəçi 2','$2b$10$pYmfcxfiqVssARHvw8VX..Kuq/04TVUd1fTQVM/8.iBTRfH.bsTzC','guest','active','en-US','{}', NULL, now()-interval '4 days');

-- ---------------------------------------------------------------------
-- 2. catalog.categories — 12 (iyerarxiya ilə)
--    id GENERATED ALWAYS olduğu üçün OVERRIDING SYSTEM VALUE lazımdır
-- ---------------------------------------------------------------------
INSERT INTO catalog.categories (id, slug, name, parent_id, sort_order, is_active)
OVERRIDING SYSTEM VALUE VALUES
 (1,'proqramlasdirma',    'Proqramlaşdırma',          NULL, 10, true),
 (2,'kibertehlukesizlik', 'Kibertəhlükəsizlik',       NULL, 20, true),
 (3,'sebeke-devops',      'Şəbəkə və DevOps',         NULL, 30, true),
 (4,'dizayn',             'Dizayn',                   NULL, 40, true),
 (5,'data-ai',            'Data və Süni İntellekt',   NULL, 50, true),
 (6,'veb-inkisaf',        'Veb İnkişaf',                 1, 11, true),
 (7,'mobil-inkisaf',      'Mobil İnkişaf',               1, 12, true),
 (8,'backend-inkisaf',    'Backend İnkişaf',             1, 13, true),
 (9,'penetrasiya-testi',  'Penetrasiya Testi',           2, 21, true),
 (10,'soc-monitorinq',    'SOC və Monitorinq',           2, 22, true),
 (11,'bulud-texnologiyalari','Bulud Texnologiyaları',    3, 31, true),
 (12,'ui-ux',             'UI/UX',                       4, 41, true);
SELECT setval(pg_get_serial_sequence('catalog.categories','id'), 12, true);

-- ---------------------------------------------------------------------
-- 3. catalog.courses — 12 kurs
--    Qiymət/adlar chat-botun bildiyi kurslarla eyni saxlanılıb ki, bot
--    cavabları ilə saytdakı katalog bir-birinə uyğun gəlsin.
-- ---------------------------------------------------------------------
INSERT INTO catalog.courses
  (id, slug, category_id, title, short_description, full_description, target_audience,
   difficulty, duration_weeks, delivery_format, location_text, base_price, currency,
   price_period, is_published, is_active, is_archived, content, created_by, created_at)
VALUES
 ('c0000000-0000-4000-8000-000000000001','full-stack-veb-inkisafi',6,'Full-Stack Veb İnkişafı (JavaScript / React / Node.js)',
  'Sıfırdan müasir veb tətbiqlər qurmağı öyrən — frontend-dən backend-ə qədər.',
  'Kurs HTML/CSS/JavaScript əsaslarından başlayır, React ilə interaktiv interfeyslər, Node.js və Express ilə REST API, PostgreSQL ilə verilənlər bazası və deploy mərhələsi ilə tamamlanır. Kurs boyunca 3 real layihə hazırlanır.',
  'Proqramlaşdırmaya yeni başlayanlar və frontend biliyini backend ilə tamamlamaq istəyənlər.',
  'intermediate',24,'hybrid','Bakı, Nərimanov — Nexora kampus',890,'AZN','total',true,true,false,
  '{"modules":["HTML/CSS əsasları","JavaScript ES6+","React və state idarəsi","Node.js və Express","PostgreSQL","Deploy və CI/CD"],"outcomes":["Tam funksional veb tətbiq qurmaq","REST API dizaynı","Git ilə komanda işi"],"projects":3}',
  'a0b7447e-c5ac-4c1f-a309-4caecfbbc061', now()-interval '200 days'),

 ('c0000000-0000-4000-8000-000000000002','python-ve-data-analitika',5,'Python və Data Analitika',
  'Python, Pandas və SQL ilə real datadan mənalı nəticələr çıxarmağı öyrən.',
  'Python sintaksisindən başlayaraq Pandas, NumPy, Matplotlib və SQL sorğuları ilə data təmizləmə, analiz və vizuallaşdırma bacarıqları qazandırılır. Sonda real dataset üzərində tam analiz layihəsi təqdim olunur.',
  'Analitik düşünən, karyerasını dataya yönəltmək istəyən hər kəs.',
  'beginner',16,'online',NULL,750,'AZN','total',true,true,false,
  '{"modules":["Python əsasları","Pandas və NumPy","SQL ilə data sorğuları","Vizuallaşdırma","Statistika əsasları"],"outcomes":["Dataset təmizləmək","Dashboard qurmaq","SQL ilə analiz"],"projects":2}',
  'a0b7447e-c5ac-4c1f-a309-4caecfbbc061', now()-interval '190 days'),

 ('c0000000-0000-4000-8000-000000000003','etik-hacker-esaslari',9,'Etik Hacker Əsasları',
  'Sistemləri qorumaq üçün əvvəlcə necə sındırıldığını öyrən.',
  'Kali Linux mühiti, kəşfiyyat (reconnaissance), zəiflik skan etmə, veb tətbiq zəiflikləri (OWASP Top 10) və hesabat yazma bacarıqları öyrədilir. Bütün məşğələlər izolyasiya olunmuş laboratoriya mühitində aparılır.',
  'Kibertəhlükəsizlik sahəsinə giriş etmək istəyən IT mütəxəssisləri və tələbələr.',
  'intermediate',20,'offline','Bakı, Nəsimi — Nexora lab',980,'AZN','total',true,true,false,
  '{"modules":["Kali Linux","Reconnaissance","Zəiflik skanı","OWASP Top 10","Hesabat yazma"],"outcomes":["Pentest metodologiyası","Zəiflik hesabatı hazırlamaq"],"projects":2}',
  'a0b7447e-c5ac-4c1f-a309-4caecfbbc061', now()-interval '180 days'),

 ('c0000000-0000-4000-8000-000000000004','sebeke-tehlukesizliyi-ve-pentesting',9,'Şəbəkə Təhlükəsizliyi və Pentesting',
  'Şəbəkə səviyyəsində hücum və müdafiə texnikalarının dərin təhlili.',
  'TCP/IP dərinliyi, şəbəkə skan etmə, MITM, firewall/IDS konfiqurasiyası, Active Directory hücumları və müdafiə strategiyaları. Kurs Etik Hacker Əsasları kursunun davamıdır.',
  'Etik Hacker Əsasları kursunu bitirənlər və şəbəkə administratorları.',
  'advanced',28,'offline','Bakı, Nəsimi — Nexora lab',1200,'AZN','total',true,true,false,
  '{"modules":["TCP/IP dərinliyi","Nmap və şəbəkə kəşfi","MITM hücumları","Firewall və IDS","Active Directory təhlükəsizliyi"],"outcomes":["Şəbəkə pentest aparmaq","AD mühitini qorumaq"],"projects":3}',
  'a0b7447e-c5ac-4c1f-a309-4caecfbbc061', now()-interval '175 days'),

 ('c0000000-0000-4000-8000-000000000005','mobil-tetbiq-flutter',7,'Mobil Tətbiq Hazırlanması (Flutter)',
  'Bir kod bazası ilə həm Android, həm iOS üçün tətbiq yaz.',
  'Dart dili, Flutter widget sistemi, state idarəsi (Provider/Riverpod), REST API inteqrasiyası, lokal saxlama və mağazaya yükləmə prosesi əhatə olunur.',
  'Mobil tərtibatçı olmaq istəyən, proqramlaşdırma əsasları olan şəxslər.',
  'intermediate',18,'online',NULL,850,'AZN','total',true,true,false,
  '{"modules":["Dart dili","Flutter widget-ləri","State idarəsi","API inteqrasiyası","Store-a yükləmə"],"outcomes":["Cross-platform tətbiq buraxmaq"],"projects":2}',
  'a0b7447e-c5ac-4c1f-a309-4caecfbbc061', now()-interval '160 days'),

 ('c0000000-0000-4000-8000-000000000006','devops-ve-bulud-aws',11,'DevOps və Bulud Texnologiyaları (AWS)',
  'Docker, Kubernetes və AWS ilə müasir infrastruktur qur.',
  'Linux əsasları, Docker konteynerləri, Kubernetes orkestrasiyası, CI/CD boru xətləri (GitHub Actions), Terraform ilə infrastruktur və AWS əsas xidmətləri (EC2, S3, RDS, EKS).',
  'Backend tərtibatçılar və sistem administratorları.',
  'advanced',26,'hybrid','Bakı, Nərimanov — Nexora kampus',1100,'AZN','total',true,true,false,
  '{"modules":["Linux və shell","Docker","Kubernetes","CI/CD","Terraform","AWS xidmətləri"],"outcomes":["Prod-a hazır pipeline qurmaq","K8s klaster idarə etmək"],"projects":4}',
  'a0b7447e-c5ac-4c1f-a309-4caecfbbc061', now()-interval '150 days'),

 ('c0000000-0000-4000-8000-000000000007','soc-analitik-hazirligi',10,'SOC Analitik Hazırlığı',
  'Təhlükəsizlik Əməliyyat Mərkəzində işləmək üçün praktik hazırlıq.',
  'SIEM alətləri (Splunk/ELK), log analizi, insident cavabı prosesi, MITRE ATT&CK çərçivəsi və təhdid kəşfiyyatı. Kurs real insident ssenariləri üzərində qurulub.',
  'Kibertəhlükəsizlikdə ilk iş yerini axtaranlar.',
  'beginner',14,'online',NULL,700,'AZN','total',true,true,false,
  '{"modules":["SIEM əsasları","Log analizi","MITRE ATT&CK","İnsident cavabı","Təhdid kəşfiyyatı"],"outcomes":["SOC L1 analitik səviyyəsi"],"projects":2}',
  'a0b7447e-c5ac-4c1f-a309-4caecfbbc061', now()-interval '140 days'),

 ('c0000000-0000-4000-8000-000000000008','ui-ux-dizayn-ve-frontend',12,'UI/UX Dizayn və Frontend Əsasları',
  'İstifadəçi araşdırmasından hazır interfeysə qədər tam proses.',
  'Dizayn təfəkkürü, istifadəçi araşdırması, wireframe, Figma ilə prototip, dizayn sistemləri və hazır dizaynı HTML/CSS-ə çevirmək.',
  'Dizayna maraq göstərən, texniki biliyi olmayan başlanğıclar.',
  'beginner',12,'online',NULL,650,'AZN','total',true,true,false,
  '{"modules":["Dizayn təfəkkürü","İstifadəçi araşdırması","Figma","Dizayn sistemləri","HTML/CSS-ə köçürmə"],"outcomes":["Portfolio üçün 3 dizayn işi"],"projects":3}',
  'a0b7447e-c5ac-4c1f-a309-4caecfbbc061', now()-interval '130 days'),

 ('c0000000-0000-4000-8000-000000000009','java-spring-boot-backend',8,'Java və Spring Boot ilə Backend',
  'Korporativ səviyyədə REST API-lar qurmağı öyrən.',
  'Java əsasları, OOP, Spring Boot, Spring Data JPA, Spring Security və JWT, PostgreSQL, test yazma və Docker ilə paketləmə.',
  'Backend tərtibatçı olmaq istəyənlər və Java bilikləri olan tələbələr.',
  'intermediate',22,'hybrid','Bakı, Nərimanov — Nexora kampus',950,'AZN','total',true,true,false,
  '{"modules":["Java və OOP","Spring Boot","Spring Data JPA","Spring Security və JWT","Test yazma","Docker"],"outcomes":["Tam REST API layihəsi"],"projects":3}',
  'a0b7447e-c5ac-4c1f-a309-4caecfbbc061', now()-interval '120 days'),

 ('c0000000-0000-4000-8000-000000000010','suni-intellekt-ve-ml',5,'Süni İntellekt və Machine Learning',
  'Klassik ML-dən neyron şəbəkələrə qədər praktik yol.',
  'Riyazi əsaslar, scikit-learn ilə klassik alqoritmlər, model qiymətləndirmə, TensorFlow/PyTorch ilə dərin öyrənmə və modeli xidmət kimi yerləşdirmək.',
  'Python və data analitika biliyi olanlar.',
  'advanced',30,'online',NULL,1350,'AZN','total',true,true,false,
  '{"modules":["ML riyaziyyatı","scikit-learn","Model qiymətləndirmə","Dərin öyrənmə","Model deployment"],"outcomes":["Prod-a çıxarılmış ML modeli"],"projects":4}',
  'a0b7447e-c5ac-4c1f-a309-4caecfbbc061', now()-interval '110 days'),

 -- dərc olunmamış (frontend-də filtr testi üçün)
 ('c0000000-0000-4000-8000-000000000011','qrafik-dizayn-esaslari',4,'Qrafik Dizayn Əsasları',
  'Adobe Illustrator və Photoshop ilə vizual dizayn əsasları.',
  'Kompozisiya, rəng nəzəriyyəsi, tipoqrafiya, loqo dizaynı və çap üçün hazırlıq. Kurs hazırda hazırlıq mərhələsindədir.',
  'Vizual sahəyə yeni başlayanlar.',
  'beginner',10,'offline','Bakı, Yasamal',550,'AZN','total',false,true,false,
  '{"modules":["Kompozisiya","Rəng nəzəriyyəsi","Tipoqrafiya","Loqo dizaynı"],"status":"hazırlanır"}',
  'a0b7447e-c5ac-4c1f-a309-4caecfbbc061', now()-interval '30 days'),

 -- arxivlənmiş (köhnə kurs)
 ('c0000000-0000-4000-8000-000000000012','linux-sistem-administrasiyasi',3,'Linux Sistem Administrasiyası',
  'Server idarəçiliyi üçün Linux bacarıqları.',
  'Bu kurs DevOps və Bulud Texnologiyaları kursuna birləşdirilib, artıq ayrıca təklif olunmur.',
  'Sistem administratorları.',
  'intermediate',12,'online',NULL,780,'AZN','total',false,false,true,
  '{"modules":["Linux fayl sistemi","İstifadəçi idarəsi","Shell skriptləri","Xidmət idarəsi"],"replacedBy":"devops-ve-bulud-aws"}',
  'a0b7447e-c5ac-4c1f-a309-4caecfbbc061', now()-interval '400 days');

-- əlaqəli kurslar (uuid[] sahəsinin dolu olduğunu göstərmək üçün)
UPDATE catalog.courses SET related_course_ids = ARRAY['c0000000-0000-4000-8000-000000000009','c0000000-0000-4000-8000-000000000008']::uuid[] WHERE slug='full-stack-veb-inkisafi';
UPDATE catalog.courses SET related_course_ids = ARRAY['c0000000-0000-4000-8000-000000000010']::uuid[] WHERE slug='python-ve-data-analitika';
UPDATE catalog.courses SET related_course_ids = ARRAY['c0000000-0000-4000-8000-000000000004','c0000000-0000-4000-8000-000000000007']::uuid[] WHERE slug='etik-hacker-esaslari';

-- ---------------------------------------------------------------------
-- 4. catalog.instructors — 8 müəllim
--    DİQQƏT: certifications jsonb sahəsi Instructor entity-də
--    List<Map<String,Object>> kimi map olunub (bax Instructor.java:54) —
--    yəni sadə mətn massivi ["OSCP"] YOX, obyekt massivi olmalıdır:
--    [{"title":..,"issuer":..,"issued_on":..,"credential_url":..}]
--    Əks halda GET /api/v1/instructors 500 verir.
-- ---------------------------------------------------------------------
INSERT INTO catalog.instructors (id, user_id, full_name, bio, photo_url, linkedin_url, avg_rating, certifications, is_active, created_at)
VALUES
 ('b0000000-0000-4000-8000-000000000001','a0000000-0000-4000-8000-000000000021','Dr. Elçin Məmmədov','12 illik full-stack təcrübəsi. Əvvəllər beynəlxalq fintech şirkətlərində baş tərtibatçı işləyib.','https://i.pravatar.cc/300?img=12','https://linkedin.com/in/elchin-mammadov',4.8,'[{"title":"AWS Certified Developer","issuer":"Amazon Web Services","issued_on":"2023-05-12","credential_url":"https://cred.example.com/aws-dev-1"},{"title":"MongoDB Professional","issuer":"MongoDB Inc.","issued_on":"2022-09-03","credential_url":"https://cred.example.com/mongo-1"}]',true, now()-interval '300 days'),
 ('b0000000-0000-4000-8000-000000000002','a0000000-0000-4000-8000-000000000022','Aynur Həsənova','Data scientist, 8 il bank sektorunda analitika təcrübəsi. Python və SQL üzrə korporativ təlimçi.','https://i.pravatar.cc/300?img=45','https://linkedin.com/in/aynur-hasanova',4.9,'[{"title":"Google Data Analytics","issuer":"Google","issued_on":"2023-02-18","credential_url":"https://cred.example.com/gda-1"},{"title":"Tableau Desktop Specialist","issuer":"Tableau","issued_on":"2021-11-30","credential_url":"https://cred.example.com/tableau-1"}]',true, now()-interval '280 days'),
 ('b0000000-0000-4000-8000-000000000003',NULL,'Vüqar Səlimov','Penetrasiya test mütəxəssisi, 10 ildən çox red team təcrübəsi. Bir neçə CVE müəllifi.','https://i.pravatar.cc/300?img=33','https://linkedin.com/in/vuqar-salimov',4.7,'[{"title":"OSCP","issuer":"OffSec","issued_on":"2020-07-22","credential_url":"https://cred.example.com/oscp-1"},{"title":"CEH","issuer":"EC-Council","issued_on":"2019-04-10","credential_url":"https://cred.example.com/ceh-1"},{"title":"CompTIA Security+","issuer":"CompTIA","issued_on":"2018-08-05","credential_url":"https://cred.example.com/secplus-1"}]',true, now()-interval '260 days'),
 ('b0000000-0000-4000-8000-000000000004',NULL,'Nərmin Qasımova','Product designer, 7 il startap və korporativ məhsullarda UI/UX təcrübəsi.','https://i.pravatar.cc/300?img=25','https://linkedin.com/in/narmin-qasimova',4.6,'[{"title":"Nielsen Norman UX Certification","issuer":"Nielsen Norman Group","issued_on":"2022-03-14","credential_url":"https://cred.example.com/nng-1"}]',true, now()-interval '240 days'),
 ('b0000000-0000-4000-8000-000000000005',NULL,'Ramil Əliyev','DevOps mühəndisi, Kubernetes və AWS üzrə ixtisaslaşıb. Böyük miqyaslı infrastrukturlar qurub.','https://i.pravatar.cc/300?img=52','https://linkedin.com/in/ramil-aliyev',4.8,'[{"title":"CKA — Certified Kubernetes Administrator","issuer":"CNCF","issued_on":"2023-01-25","credential_url":"https://cred.example.com/cka-1"},{"title":"AWS Solutions Architect","issuer":"Amazon Web Services","issued_on":"2022-06-11","credential_url":"https://cred.example.com/aws-sa-1"},{"title":"Terraform Associate","issuer":"HashiCorp","issued_on":"2023-09-08","credential_url":"https://cred.example.com/tf-1"}]',true, now()-interval '220 days'),
 ('b0000000-0000-4000-8000-000000000006',NULL,'Səbuhi Nərimanov','Mobil tərtibatçı, App Store və Google Play-də 15+ buraxılmış tətbiq.','https://i.pravatar.cc/300?img=68','https://linkedin.com/in/sabuhi-narimanov',4.5,'[{"title":"Google Associate Android Developer","issuer":"Google","issued_on":"2021-10-19","credential_url":"https://cred.example.com/aad-1"}]',true, now()-interval '200 days'),
 ('b0000000-0000-4000-8000-000000000007',NULL,'Türkan Abbasova','ML mühəndisi, PhD namizədi. Computer vision sahəsində elmi məqalələr müəllifi.','https://i.pravatar.cc/300?img=47','https://linkedin.com/in/turkan-abbasova',4.9,'[{"title":"TensorFlow Developer Certificate","issuer":"Google","issued_on":"2023-04-02","credential_url":"https://cred.example.com/tf-dev-1"},{"title":"Deep Learning Specialization","issuer":"DeepLearning.AI","issued_on":"2022-01-16","credential_url":"https://cred.example.com/dls-1"}]',true, now()-interval '180 days'),
 ('b0000000-0000-4000-8000-000000000008',NULL,'Anar Vəliyev','Şəbəkə mühəndisi və SOC lideri. 14 il telekom və bank infrastrukturunda.','https://i.pravatar.cc/300?img=60','https://linkedin.com/in/anar-valiyev',4.4,'[{"title":"CCNP Enterprise","issuer":"Cisco","issued_on":"2020-12-01","credential_url":"https://cred.example.com/ccnp-1"},{"title":"Splunk Core Certified User","issuer":"Splunk","issued_on":"2022-05-27","credential_url":"https://cred.example.com/splunk-1"}]',true, now()-interval '160 days');

-- ---------------------------------------------------------------------
-- 5. catalog.course_instructors
-- ---------------------------------------------------------------------
INSERT INTO catalog.course_instructors (course_id, instructor_id, role) VALUES
 ('c0000000-0000-4000-8000-000000000001','b0000000-0000-4000-8000-000000000001','lead'),
 ('c0000000-0000-4000-8000-000000000001','b0000000-0000-4000-8000-000000000004','assistant'),
 ('c0000000-0000-4000-8000-000000000002','b0000000-0000-4000-8000-000000000002','lead'),
 ('c0000000-0000-4000-8000-000000000003','b0000000-0000-4000-8000-000000000003','lead'),
 ('c0000000-0000-4000-8000-000000000004','b0000000-0000-4000-8000-000000000003','lead'),
 ('c0000000-0000-4000-8000-000000000004','b0000000-0000-4000-8000-000000000008','assistant'),
 ('c0000000-0000-4000-8000-000000000005','b0000000-0000-4000-8000-000000000006','lead'),
 ('c0000000-0000-4000-8000-000000000006','b0000000-0000-4000-8000-000000000005','lead'),
 ('c0000000-0000-4000-8000-000000000007','b0000000-0000-4000-8000-000000000008','lead'),
 ('c0000000-0000-4000-8000-000000000008','b0000000-0000-4000-8000-000000000004','lead'),
 ('c0000000-0000-4000-8000-000000000009','b0000000-0000-4000-8000-000000000001','lead'),
 ('c0000000-0000-4000-8000-000000000010','b0000000-0000-4000-8000-000000000007','lead'),
 ('c0000000-0000-4000-8000-000000000010','b0000000-0000-4000-8000-000000000002','assistant'),
 ('c0000000-0000-4000-8000-000000000011','b0000000-0000-4000-8000-000000000004','lead'),
 ('c0000000-0000-4000-8000-000000000012','b0000000-0000-4000-8000-000000000005','lead');

-- ---------------------------------------------------------------------
-- 6. academics.course_groups — 14 qrup (müxtəlif statuslarda)
-- ---------------------------------------------------------------------
INSERT INTO academics.course_groups
  (id, course_id, group_code, start_date, end_date, registration_deadline, total_seats, reserved_seats, status, schedule, created_at)
VALUES
 ('d0000000-0000-4000-8000-000000000001','c0000000-0000-4000-8000-000000000001','FS-2026-01', current_date-interval '150 days', current_date-interval '10 days', now()-interval '160 days', 20, 20,'completed', '[{"day":"Bazar ertəsi","time":"19:00-21:00"},{"day":"Çərşənbə","time":"19:00-21:00"}]', now()-interval '170 days'),
 ('d0000000-0000-4000-8000-000000000002','c0000000-0000-4000-8000-000000000001','FS-2026-02', current_date-interval '40 days',  current_date+interval '128 days', now()-interval '50 days',  22, 18,'in_progress','[{"day":"Bazar ertəsi","time":"19:00-21:00"},{"day":"Cümə","time":"19:00-21:00"}]', now()-interval '60 days'),
 ('d0000000-0000-4000-8000-000000000003','c0000000-0000-4000-8000-000000000001','FS-2026-03', current_date+interval '25 days',  current_date+interval '193 days', now()+interval '18 days',  22,  6,'open',       '[{"day":"Çərşənbə axşamı","time":"19:00-21:00"},{"day":"Şənbə","time":"11:00-13:00"}]', now()-interval '5 days'),
 ('d0000000-0000-4000-8000-000000000004','c0000000-0000-4000-8000-000000000002','PY-2026-01', current_date-interval '30 days',  current_date+interval '82 days',  now()-interval '40 days',  25, 21,'in_progress','[{"day":"Çərşənbə axşamı","time":"20:00-22:00"},{"day":"Cümə axşamı","time":"20:00-22:00"}]', now()-interval '50 days'),
 ('d0000000-0000-4000-8000-000000000005','c0000000-0000-4000-8000-000000000002','PY-2026-02', current_date+interval '20 days',  current_date+interval '132 days', now()+interval '14 days',  25,  9,'open',       '[{"day":"Şənbə","time":"10:00-13:00"}]', now()-interval '8 days'),
 ('d0000000-0000-4000-8000-000000000006','c0000000-0000-4000-8000-000000000003','EH-2026-01', current_date-interval '20 days',  current_date+interval '120 days', now()-interval '30 days',  16, 16,'full',       '[{"day":"Bazar ertəsi","time":"18:30-21:30"}]', now()-interval '45 days'),
 ('d0000000-0000-4000-8000-000000000007','c0000000-0000-4000-8000-000000000003','EH-2026-02', current_date+interval '35 days',  current_date+interval '175 days', now()+interval '28 days',  16,  4,'open',       '[{"day":"Çərşənbə","time":"18:30-21:30"}]', now()-interval '3 days'),
 ('d0000000-0000-4000-8000-000000000008','c0000000-0000-4000-8000-000000000004','NP-2026-01', current_date+interval '45 days',  current_date+interval '241 days', now()+interval '38 days',  12,  3,'open',       '[{"day":"Şənbə","time":"10:00-14:00"}]', now()-interval '12 days'),
 ('d0000000-0000-4000-8000-000000000009','c0000000-0000-4000-8000-000000000005','FL-2026-01', current_date-interval '15 days',  current_date+interval '111 days', now()-interval '25 days',  20, 14,'in_progress','[{"day":"Çərşənbə axşamı","time":"19:30-21:30"}]', now()-interval '35 days'),
 ('d0000000-0000-4000-8000-000000000010','c0000000-0000-4000-8000-000000000006','DO-2026-01', current_date-interval '5 days',   current_date+interval '177 days', now()-interval '15 days',  18, 15,'in_progress','[{"day":"Bazar ertəsi","time":"19:00-22:00"},{"day":"Cümə axşamı","time":"19:00-22:00"}]', now()-interval '30 days'),
 ('d0000000-0000-4000-8000-000000000011','c0000000-0000-4000-8000-000000000007','SOC-2026-01',current_date+interval '10 days',  current_date+interval '108 days', now()+interval '5 days',   30, 11,'open',       '[{"day":"Cümə axşamı","time":"20:00-22:00"}]', now()-interval '18 days'),
 ('d0000000-0000-4000-8000-000000000012','c0000000-0000-4000-8000-000000000008','UX-2026-01', current_date-interval '60 days',  current_date+interval '24 days',  now()-interval '70 days',  24, 19,'in_progress','[{"day":"Şənbə","time":"14:00-17:00"}]', now()-interval '80 days'),
 ('d0000000-0000-4000-8000-000000000013','c0000000-0000-4000-8000-000000000009','JV-2026-01', current_date+interval '15 days',  current_date+interval '169 days', now()+interval '10 days',  20,  7,'open',       '[{"day":"Çərşənbə","time":"19:00-21:30"},{"day":"Şənbə","time":"11:00-13:30"}]', now()-interval '22 days'),
 ('d0000000-0000-4000-8000-000000000014','c0000000-0000-4000-8000-000000000010','ML-2026-01', current_date+interval '60 days',  current_date+interval '270 days', now()+interval '50 days',  15,  2,'planned',    '[{"day":"Bazar","time":"11:00-15:00"}]', now()-interval '2 days');

-- ---------------------------------------------------------------------
-- 7. academics.enrollments — 30 qeydiyyat
-- ---------------------------------------------------------------------
INSERT INTO academics.enrollments
  (id, user_id, group_id, status, idempotency_key, consent_text_version, consent_given_at, hold_expires_at, enrolled_at, completed_at, cancelled_at, cancel_reason)
VALUES
 ('e0000000-0000-4000-8000-000000000001','a0000000-0000-4000-8000-000000000001','d0000000-0000-4000-8000-000000000001','completed','enr-seed-001','v1.2', now()-interval '155 days', NULL, now()-interval '155 days', now()-interval '10 days', NULL, NULL),
 ('e0000000-0000-4000-8000-000000000002','a0000000-0000-4000-8000-000000000002','d0000000-0000-4000-8000-000000000001','completed','enr-seed-002','v1.2', now()-interval '154 days', NULL, now()-interval '154 days', now()-interval '10 days', NULL, NULL),
 ('e0000000-0000-4000-8000-000000000003','a0000000-0000-4000-8000-000000000003','d0000000-0000-4000-8000-000000000001','completed','enr-seed-003','v1.2', now()-interval '152 days', NULL, now()-interval '152 days', now()-interval '10 days', NULL, NULL),
 ('e0000000-0000-4000-8000-000000000004','a0000000-0000-4000-8000-000000000004','d0000000-0000-4000-8000-000000000001','cancelled','enr-seed-004','v1.2', now()-interval '150 days', NULL, now()-interval '150 days', NULL, now()-interval '140 days','Şəxsi səbəblərə görə ləğv etdi'),
 ('e0000000-0000-4000-8000-000000000005','a0000000-0000-4000-8000-000000000005','d0000000-0000-4000-8000-000000000002','confirmed','enr-seed-005','v1.3', now()-interval '45 days',  NULL, now()-interval '45 days',  NULL, NULL, NULL),
 ('e0000000-0000-4000-8000-000000000006','a0000000-0000-4000-8000-000000000006','d0000000-0000-4000-8000-000000000002','confirmed','enr-seed-006','v1.3', now()-interval '44 days',  NULL, now()-interval '44 days',  NULL, NULL, NULL),
 ('e0000000-0000-4000-8000-000000000007','a0000000-0000-4000-8000-000000000007','d0000000-0000-4000-8000-000000000002','confirmed','enr-seed-007','v1.3', now()-interval '43 days',  NULL, now()-interval '43 days',  NULL, NULL, NULL),
 ('e0000000-0000-4000-8000-000000000008','a0000000-0000-4000-8000-000000000008','d0000000-0000-4000-8000-000000000002','refunded', 'enr-seed-008','v1.3', now()-interval '42 days',  NULL, now()-interval '42 days',  NULL, now()-interval '20 days','Kursdan imtina — məbləğ qaytarıldı'),
 ('e0000000-0000-4000-8000-000000000009','a0000000-0000-4000-8000-000000000009','d0000000-0000-4000-8000-000000000003','pending_payment','enr-seed-009','v1.3', now()-interval '4 days', NULL, now()-interval '4 days', NULL, NULL, NULL),
 ('e0000000-0000-4000-8000-000000000010','a0000000-0000-4000-8000-000000000010','d0000000-0000-4000-8000-000000000003','held','enr-seed-010','v1.3', now()-interval '1 day', now()+interval '2 days', now()-interval '1 day', NULL, NULL, NULL),
 ('e0000000-0000-4000-8000-000000000011','a0000000-0000-4000-8000-000000000011','d0000000-0000-4000-8000-000000000004','confirmed','enr-seed-011','v1.3', now()-interval '35 days', NULL, now()-interval '35 days', NULL, NULL, NULL),
 ('e0000000-0000-4000-8000-000000000012','a0000000-0000-4000-8000-000000000012','d0000000-0000-4000-8000-000000000004','confirmed','enr-seed-012','v1.3', now()-interval '34 days', NULL, now()-interval '34 days', NULL, NULL, NULL),
 ('e0000000-0000-4000-8000-000000000013','a0000000-0000-4000-8000-000000000013','d0000000-0000-4000-8000-000000000004','confirmed','enr-seed-013','v1.3', now()-interval '33 days', NULL, now()-interval '33 days', NULL, NULL, NULL),
 ('e0000000-0000-4000-8000-000000000014','a0000000-0000-4000-8000-000000000014','d0000000-0000-4000-8000-000000000004','waitlisted','enr-seed-014','v1.3', now()-interval '6 days', NULL, now()-interval '6 days', NULL, NULL, NULL),
 ('e0000000-0000-4000-8000-000000000015','a0000000-0000-4000-8000-000000000015','d0000000-0000-4000-8000-000000000005','pending_payment','enr-seed-015','v1.3', now()-interval '3 days', NULL, now()-interval '3 days', NULL, NULL, NULL),
 ('e0000000-0000-4000-8000-000000000016','a0000000-0000-4000-8000-000000000016','d0000000-0000-4000-8000-000000000006','confirmed','enr-seed-016','v1.3', now()-interval '28 days', NULL, now()-interval '28 days', NULL, NULL, NULL),
 ('e0000000-0000-4000-8000-000000000017','a0000000-0000-4000-8000-000000000017','d0000000-0000-4000-8000-000000000006','confirmed','enr-seed-017','v1.3', now()-interval '27 days', NULL, now()-interval '27 days', NULL, NULL, NULL),
 ('e0000000-0000-4000-8000-000000000018','a0000000-0000-4000-8000-000000000018','d0000000-0000-4000-8000-000000000007','pending_payment','enr-seed-018','v1.3', now()-interval '2 days', NULL, now()-interval '2 days', NULL, NULL, NULL),
 ('e0000000-0000-4000-8000-000000000019','a0000000-0000-4000-8000-000000000019','d0000000-0000-4000-8000-000000000008','cancelled','enr-seed-019','v1.3', now()-interval '11 days', NULL, now()-interval '11 days', NULL, now()-interval '9 days','Ödəniş vaxtında edilmədi'),
 ('e0000000-0000-4000-8000-000000000020','a0000000-0000-4000-8000-000000000020','d0000000-0000-4000-8000-000000000008','confirmed','enr-seed-020','v1.3', now()-interval '10 days', NULL, now()-interval '10 days', NULL, NULL, NULL),
 ('e0000000-0000-4000-8000-000000000021','a0000000-0000-4000-8000-000000000001','d0000000-0000-4000-8000-000000000009','confirmed','enr-seed-021','v1.3', now()-interval '20 days', NULL, now()-interval '20 days', NULL, NULL, NULL),
 ('e0000000-0000-4000-8000-000000000022','a0000000-0000-4000-8000-000000000003','d0000000-0000-4000-8000-000000000009','confirmed','enr-seed-022','v1.3', now()-interval '19 days', NULL, now()-interval '19 days', NULL, NULL, NULL),
 ('e0000000-0000-4000-8000-000000000023','a0000000-0000-4000-8000-000000000005','d0000000-0000-4000-8000-000000000010','confirmed','enr-seed-023','v1.3', now()-interval '14 days', NULL, now()-interval '14 days', NULL, NULL, NULL),
 ('e0000000-0000-4000-8000-000000000024','a0000000-0000-4000-8000-000000000007','d0000000-0000-4000-8000-000000000010','confirmed','enr-seed-024','v1.3', now()-interval '13 days', NULL, now()-interval '13 days', NULL, NULL, NULL),
 ('e0000000-0000-4000-8000-000000000025','a0000000-0000-4000-8000-000000000009','d0000000-0000-4000-8000-000000000011','pending_payment','enr-seed-025','v1.3', now()-interval '7 days', NULL, now()-interval '7 days', NULL, NULL, NULL),
 ('e0000000-0000-4000-8000-000000000026','a0000000-0000-4000-8000-000000000011','d0000000-0000-4000-8000-000000000012','completed','enr-seed-026','v1.2', now()-interval '65 days', NULL, now()-interval '65 days', now()-interval '2 days', NULL, NULL),
 ('e0000000-0000-4000-8000-000000000027','a0000000-0000-4000-8000-000000000013','d0000000-0000-4000-8000-000000000012','confirmed','enr-seed-027','v1.2', now()-interval '64 days', NULL, now()-interval '64 days', NULL, NULL, NULL),
 ('e0000000-0000-4000-8000-000000000028','a0000000-0000-4000-8000-000000000015','d0000000-0000-4000-8000-000000000013','confirmed','enr-seed-028','v1.3', now()-interval '18 days', NULL, now()-interval '18 days', NULL, NULL, NULL),
 ('e0000000-0000-4000-8000-000000000029','a0000000-0000-4000-8000-000000000017','d0000000-0000-4000-8000-000000000013','pending_payment','enr-seed-029','v1.3', now()-interval '5 days', NULL, now()-interval '5 days', NULL, NULL, NULL),
 ('e0000000-0000-4000-8000-000000000030','a0000000-0000-4000-8000-000000000020','d0000000-0000-4000-8000-000000000014','held','enr-seed-030','v1.3', now()-interval '1 day', now()+interval '1 day', now()-interval '1 day', NULL, NULL, NULL);

-- ---------------------------------------------------------------------
-- 8. billing.payments — 26 ödəniş (bütün statuslar təmsil olunub)
-- ---------------------------------------------------------------------
INSERT INTO billing.payments
  (id, enrollment_id, method, amount, currency, status, external_txn_id, idempotency_key, installments, refund_amount, refund_reason, initiated_at, captured_at, failure_reason)
VALUES
 ('f0000000-0000-4000-8000-000000000001','e0000000-0000-4000-8000-000000000001','card',890.00,'AZN','captured','TXN-2026-0001','pay-seed-001','[]',0,NULL, now()-interval '155 days', now()-interval '155 days', NULL),
 ('f0000000-0000-4000-8000-000000000002','e0000000-0000-4000-8000-000000000002','installment',890.00,'AZN','captured','TXN-2026-0002','pay-seed-002','[{"no":1,"amount":296.67,"paidAt":"2026-03-01"},{"no":2,"amount":296.67,"paidAt":"2026-04-01"},{"no":3,"amount":296.66,"paidAt":"2026-05-01"}]',0,NULL, now()-interval '154 days', now()-interval '60 days', NULL),
 ('f0000000-0000-4000-8000-000000000003','e0000000-0000-4000-8000-000000000003','bank_transfer',890.00,'AZN','captured','TXN-2026-0003','pay-seed-003','[]',0,NULL, now()-interval '152 days', now()-interval '151 days', NULL),
 ('f0000000-0000-4000-8000-000000000004','e0000000-0000-4000-8000-000000000004','card',890.00,'AZN','cancelled','TXN-2026-0004','pay-seed-004','[]',0,NULL, now()-interval '150 days', NULL,'İstifadəçi ödənişi ləğv etdi'),
 ('f0000000-0000-4000-8000-000000000005','e0000000-0000-4000-8000-000000000005','card',890.00,'AZN','captured','TXN-2026-0005','pay-seed-005','[]',0,NULL, now()-interval '45 days', now()-interval '45 days', NULL),
 ('f0000000-0000-4000-8000-000000000006','e0000000-0000-4000-8000-000000000006','installment',890.00,'AZN','captured','TXN-2026-0006','pay-seed-006','[{"no":1,"amount":445.00,"paidAt":"2026-06-15"},{"no":2,"amount":445.00,"dueAt":"2026-08-15"}]',0,NULL, now()-interval '44 days', now()-interval '44 days', NULL),
 ('f0000000-0000-4000-8000-000000000007','e0000000-0000-4000-8000-000000000007','scholarship_covered',445.00,'AZN','captured','TXN-2026-0007','pay-seed-007','[]',0,NULL, now()-interval '43 days', now()-interval '43 days', NULL),
 ('f0000000-0000-4000-8000-000000000008','e0000000-0000-4000-8000-000000000008','card',890.00,'AZN','refunded','TXN-2026-0008','pay-seed-008','[]',890.00,'Kursdan imtina — tam qaytarma', now()-interval '42 days', now()-interval '42 days', NULL),
 ('f0000000-0000-4000-8000-000000000009','e0000000-0000-4000-8000-000000000009','card',890.00,'AZN','initiated',NULL,'pay-seed-009','[]',0,NULL, now()-interval '4 days', NULL, NULL),
 ('f0000000-0000-4000-8000-000000000010','e0000000-0000-4000-8000-000000000010','card',890.00,'AZN','authorized','TXN-2026-0010','pay-seed-010','[]',0,NULL, now()-interval '1 day', NULL, NULL),
 ('f0000000-0000-4000-8000-000000000011','e0000000-0000-4000-8000-000000000011','card',750.00,'AZN','captured','TXN-2026-0011','pay-seed-011','[]',0,NULL, now()-interval '35 days', now()-interval '35 days', NULL),
 ('f0000000-0000-4000-8000-000000000012','e0000000-0000-4000-8000-000000000012','card',750.00,'AZN','captured','TXN-2026-0012','pay-seed-012','[]',0,NULL, now()-interval '34 days', now()-interval '34 days', NULL),
 ('f0000000-0000-4000-8000-000000000013','e0000000-0000-4000-8000-000000000013','bank_transfer',750.00,'AZN','captured','TXN-2026-0013','pay-seed-013','[]',0,NULL, now()-interval '33 days', now()-interval '32 days', NULL),
 ('f0000000-0000-4000-8000-000000000014','e0000000-0000-4000-8000-000000000015','card',750.00,'AZN','failed',NULL,'pay-seed-014','[]',0,NULL, now()-interval '3 days', NULL,'Kartda kifayət qədər vəsait yoxdur'),
 ('f0000000-0000-4000-8000-000000000015','e0000000-0000-4000-8000-000000000016','card',980.00,'AZN','captured','TXN-2026-0015','pay-seed-015','[]',0,NULL, now()-interval '28 days', now()-interval '28 days', NULL),
 ('f0000000-0000-4000-8000-000000000016','e0000000-0000-4000-8000-000000000017','installment',980.00,'AZN','captured','TXN-2026-0016','pay-seed-016','[{"no":1,"amount":490.00,"paidAt":"2026-07-01"},{"no":2,"amount":490.00,"dueAt":"2026-09-01"}]',0,NULL, now()-interval '27 days', now()-interval '27 days', NULL),
 ('f0000000-0000-4000-8000-000000000017','e0000000-0000-4000-8000-000000000018','card',980.00,'AZN','initiated',NULL,'pay-seed-017','[]',0,NULL, now()-interval '2 days', NULL, NULL),
 ('f0000000-0000-4000-8000-000000000018','e0000000-0000-4000-8000-000000000019','card',1200.00,'AZN','failed',NULL,'pay-seed-018','[]',0,NULL, now()-interval '11 days', NULL,'3D Secure təsdiqi alınmadı'),
 ('f0000000-0000-4000-8000-000000000019','e0000000-0000-4000-8000-000000000020','bank_transfer',1200.00,'AZN','captured','TXN-2026-0019','pay-seed-019','[]',0,NULL, now()-interval '10 days', now()-interval '9 days', NULL),
 ('f0000000-0000-4000-8000-000000000020','e0000000-0000-4000-8000-000000000021','card',850.00,'AZN','captured','TXN-2026-0020','pay-seed-020','[]',0,NULL, now()-interval '20 days', now()-interval '20 days', NULL),
 ('f0000000-0000-4000-8000-000000000021','e0000000-0000-4000-8000-000000000022','card',850.00,'AZN','partially_refunded','TXN-2026-0021','pay-seed-021','[]',255.00,'Qismən qaytarma — 3 dərs buraxıldı', now()-interval '19 days', now()-interval '19 days', NULL),
 ('f0000000-0000-4000-8000-000000000022','e0000000-0000-4000-8000-000000000023','installment',1100.00,'AZN','captured','TXN-2026-0022','pay-seed-022','[{"no":1,"amount":550.00,"paidAt":"2026-07-14"},{"no":2,"amount":550.00,"dueAt":"2026-09-14"}]',0,NULL, now()-interval '14 days', now()-interval '14 days', NULL),
 ('f0000000-0000-4000-8000-000000000023','e0000000-0000-4000-8000-000000000024','card',1100.00,'AZN','captured','TXN-2026-0023','pay-seed-023','[]',0,NULL, now()-interval '13 days', now()-interval '13 days', NULL),
 ('f0000000-0000-4000-8000-000000000024','e0000000-0000-4000-8000-000000000026','scholarship_covered',390.00,'AZN','captured','TXN-2026-0024','pay-seed-024','[]',0,NULL, now()-interval '65 days', now()-interval '65 days', NULL),
 ('f0000000-0000-4000-8000-000000000025','e0000000-0000-4000-8000-000000000027','card',650.00,'AZN','captured','TXN-2026-0025','pay-seed-025','[]',0,NULL, now()-interval '64 days', now()-interval '64 days', NULL),
 ('f0000000-0000-4000-8000-000000000026','e0000000-0000-4000-8000-000000000028','card',950.00,'AZN','captured','TXN-2026-0026','pay-seed-026','[]',0,NULL, now()-interval '18 days', now()-interval '18 days', NULL);

-- ---------------------------------------------------------------------
-- 9. billing.scholarships — 5 təqaüd proqramı
-- ---------------------------------------------------------------------
INSERT INTO billing.scholarships (id, name, description, discount_pct, max_recipients, valid_from, valid_until, is_active, applications)
OVERRIDING SYSTEM VALUE VALUES
 (1,'Şəhid ailəsi üzvləri üçün tam təqaüd','Şəhid ailəsi üzvlərinə bütün kurslarda 100% endirim.',100,10, current_date-interval '200 days', current_date+interval '165 days', true,'[{"userId":"a0000000-0000-4000-8000-000000000007","status":"approved","appliedAt":"2026-06-10"}]'),
 (2,'Qadınlar IT-də','Texnologiya sahəsində qadın iştirakını artırmaq üçün 50% endirim.',50,25, current_date-interval '150 days', current_date+interval '215 days', true,'[{"userId":"a0000000-0000-4000-8000-000000000011","status":"approved","appliedAt":"2026-05-20"},{"userId":"a0000000-0000-4000-8000-000000000015","status":"pending","appliedAt":"2026-07-20"}]'),
 (3,'Tələbə endirimi','Aktiv bakalavr/magistr tələbələri üçün 30% endirim.',30,50, current_date-interval '300 days', current_date+interval '65 days', true,'[]'),
 (4,'Erkən qeydiyyat','Qeydiyyat müddəti bitməzdən 30 gün əvvəl qeydiyyatdan keçənlərə 15% endirim.',15,NULL, current_date-interval '90 days', current_date+interval '90 days', true,'[]'),
 (5,'Yay kampaniyası 2025','Keçmiş yay kampaniyası — artıq aktiv deyil.',20,40, current_date-interval '400 days', current_date-interval '280 days', false,'[]');
SELECT setval(pg_get_serial_sequence('billing.scholarships','id'), 5, true);

-- ---------------------------------------------------------------------
-- 10. outcomes.course_reviews — 24 rəy
-- ---------------------------------------------------------------------
INSERT INTO outcomes.course_reviews (id, course_id, user_id, enrollment_id, rating, comment, is_published, moderated_by, ai_sentiment, created_at)
OVERRIDING SYSTEM VALUE VALUES
 (1,'c0000000-0000-4000-8000-000000000001','a0000000-0000-4000-8000-000000000001','e0000000-0000-4000-8000-000000000001',5,'Kursun strukturu çox aydın idi. Müəllim hər mövzunu real layihə üzərində izah edirdi — nəzəriyyə havada qalmırdı.',true,'87c8de10-2e79-44db-8693-92062e67cadf','{"label":"positive","score":0.94}', now()-interval '8 days'),
 (2,'c0000000-0000-4000-8000-000000000001','a0000000-0000-4000-8000-000000000002','e0000000-0000-4000-8000-000000000002',4,'Məzmun əla, amma tempi bir az sürətli idi. Ev tapşırıqlarına daha çox vaxt lazımdır.',true,'87c8de10-2e79-44db-8693-92062e67cadf','{"label":"positive","score":0.71}', now()-interval '7 days'),
 (3,'c0000000-0000-4000-8000-000000000001','a0000000-0000-4000-8000-000000000003','e0000000-0000-4000-8000-000000000003',5,'Kursdan sonra 2 ay ərzində junior frontend vəzifəsinə qəbul olundum. Portfolio layihələri müsahibədə çox kömək etdi.',true,'87c8de10-2e79-44db-8693-92062e67cadf','{"label":"positive","score":0.98}', now()-interval '6 days'),
 (4,'c0000000-0000-4000-8000-000000000001','a0000000-0000-4000-8000-000000000005',NULL,4,'Hibrid format işləyənlər üçün çox rahatdır. Offline günlər daha faydalı idi.',true,'a0b7447e-c5ac-4c1f-a309-4caecfbbc061','{"label":"positive","score":0.80}', now()-interval '20 days'),
 (5,'c0000000-0000-4000-8000-000000000001','a0000000-0000-4000-8000-000000000006',NULL,3,'Yaxşıdır, amma qrup çox böyük idi, fərdi diqqət azdır.',true,'a0b7447e-c5ac-4c1f-a309-4caecfbbc061','{"label":"neutral","score":0.52}', now()-interval '15 days'),
 (6,'c0000000-0000-4000-8000-000000000002','a0000000-0000-4000-8000-000000000011',NULL,5,'Pandas hissəsi xüsusilə güclü idi. İndi işdə gündəlik istifadə edirəm.',true,'87c8de10-2e79-44db-8693-92062e67cadf','{"label":"positive","score":0.96}', now()-interval '12 days'),
 (7,'c0000000-0000-4000-8000-000000000002','a0000000-0000-4000-8000-000000000012',NULL,5,'Aynur müəllimə mürəkkəb mövzuları sadə dildə izah edir. Tövsiyə edirəm.',true,'87c8de10-2e79-44db-8693-92062e67cadf','{"label":"positive","score":0.95}', now()-interval '11 days'),
 (8,'c0000000-0000-4000-8000-000000000002','a0000000-0000-4000-8000-000000000013',NULL,4,'SQL bölməsi gözlədiyimdən qısa idi, amma ümumi keyfiyyət yüksəkdir.',true,'87c8de10-2e79-44db-8693-92062e67cadf','{"label":"positive","score":0.68}', now()-interval '10 days'),
 (9,'c0000000-0000-4000-8000-000000000002','a0000000-0000-4000-8000-000000000014',NULL,2,'Online format mənə uyğun gəlmədi, sual vermək çətindir. Məzmun pis deyil.',true,'a0b7447e-c5ac-4c1f-a309-4caecfbbc061','{"label":"negative","score":0.63}', now()-interval '9 days'),
 (10,'c0000000-0000-4000-8000-000000000003','a0000000-0000-4000-8000-000000000016',NULL,5,'Laboratoriya mühiti əla qurulub. Real alətlərlə işləmək çox fərq yaradır.',true,'87c8de10-2e79-44db-8693-92062e67cadf','{"label":"positive","score":0.93}', now()-interval '14 days'),
 (11,'c0000000-0000-4000-8000-000000000003','a0000000-0000-4000-8000-000000000017',NULL,5,'Vüqar müəllimin sahə təcrübəsi hiss olunur. Hekayələr dərsi canlandırır.',true,'87c8de10-2e79-44db-8693-92062e67cadf','{"label":"positive","score":0.97}', now()-interval '13 days'),
 (12,'c0000000-0000-4000-8000-000000000003','a0000000-0000-4000-8000-000000000004',NULL,4,'Giriş üçün ideal. Davamı olan pentesting kursunu da götürəcəyəm.',true,'87c8de10-2e79-44db-8693-92062e67cadf','{"label":"positive","score":0.85}', now()-interval '25 days'),
 (13,'c0000000-0000-4000-8000-000000000004','a0000000-0000-4000-8000-000000000020',NULL,5,'Active Directory bölməsi tək başına kursun qiymətinə dəyər.',true,'87c8de10-2e79-44db-8693-92062e67cadf','{"label":"positive","score":0.92}', now()-interval '5 days'),
 (14,'c0000000-0000-4000-8000-000000000005','a0000000-0000-4000-8000-000000000001',NULL,4,'Flutter üçün yaxşı başlanğıc. iOS deploy hissəsi daha detallı ola bilərdi.',true,'a0b7447e-c5ac-4c1f-a309-4caecfbbc061','{"label":"positive","score":0.74}', now()-interval '4 days'),
 (15,'c0000000-0000-4000-8000-000000000005','a0000000-0000-4000-8000-000000000003',NULL,5,'İki ay içində ilk tətbiqimi Play Store-a yüklədim.',true,'a0b7447e-c5ac-4c1f-a309-4caecfbbc061','{"label":"positive","score":0.99}', now()-interval '3 days'),
 (16,'c0000000-0000-4000-8000-000000000006','a0000000-0000-4000-8000-000000000005',NULL,5,'Kubernetes hissəsi çox güclüdür. Terraform bölməsi də praktikdir.',true,'87c8de10-2e79-44db-8693-92062e67cadf','{"label":"positive","score":0.91}', now()-interval '6 days'),
 (17,'c0000000-0000-4000-8000-000000000006','a0000000-0000-4000-8000-000000000007',NULL,4,'Çətin kursdur, əvvəlcədən Linux bilmək lazımdır. Xəbərdar edilsəydi yaxşı olardı.',true,'87c8de10-2e79-44db-8693-92062e67cadf','{"label":"neutral","score":0.58}', now()-interval '5 days'),
 (18,'c0000000-0000-4000-8000-000000000007','a0000000-0000-4000-8000-000000000009',NULL,4,'SOC-a giriş üçün münasib qiymət və yaxşı məzmun.',true,'a0b7447e-c5ac-4c1f-a309-4caecfbbc061','{"label":"positive","score":0.79}', now()-interval '2 days'),
 (19,'c0000000-0000-4000-8000-000000000008','a0000000-0000-4000-8000-000000000011','e0000000-0000-4000-8000-000000000026',5,'Figma-nı sıfırdan öyrəndim. İndi freelance sifarişlər qəbul edirəm.',true,'87c8de10-2e79-44db-8693-92062e67cadf','{"label":"positive","score":0.97}', now()-interval '1 day'),
 (20,'c0000000-0000-4000-8000-000000000008','a0000000-0000-4000-8000-000000000013',NULL,4,'Dizayn sistemləri mövzusu gözəl izah olunub.',true,'87c8de10-2e79-44db-8693-92062e67cadf','{"label":"positive","score":0.83}', now()-interval '18 days'),
 (21,'c0000000-0000-4000-8000-000000000009','a0000000-0000-4000-8000-000000000015',NULL,5,'Spring Security və JWT bölməsi çox aydın idi — məhz bunu axtarırdım.',true,'87c8de10-2e79-44db-8693-92062e67cadf','{"label":"positive","score":0.94}', now()-interval '7 days'),
 -- moderasiya gözləyən (is_published=false) — admin panel testi üçün
 (22,'c0000000-0000-4000-8000-000000000009','a0000000-0000-4000-8000-000000000017',NULL,1,'Kurs gözlədiyim kimi deyildi, çox nəzəri gəldi.',false,NULL,'{"label":"negative","score":0.88}', now()-interval '2 days'),
 (23,'c0000000-0000-4000-8000-000000000002','a0000000-0000-4000-8000-000000000019',NULL,2,'Dərs saatları tez-tez dəyişdirildi.',false,NULL,'{"label":"negative","score":0.72}', now()-interval '1 day'),
 (24,'c0000000-0000-4000-8000-000000000006','a0000000-0000-4000-8000-000000000010',NULL,5,'Ən yaxşı investisiya oldu.',false,NULL,NULL, now()-interval '5 hours');
SELECT setval(pg_get_serial_sequence('outcomes.course_reviews','id'), 24, true);

-- ---------------------------------------------------------------------
-- 11. outcomes.graduate_outcomes — 10 məzun nəticəsi
-- ---------------------------------------------------------------------
INSERT INTO outcomes.graduate_outcomes (id, user_id, course_id, company_name, job_title, employed_at, salary_band, is_public_story, story_text, created_at)
OVERRIDING SYSTEM VALUE VALUES
 (1,'a0000000-0000-4000-8000-000000000001','c0000000-0000-4000-8000-000000000001','Kapital Bank','Junior Frontend Developer', current_date-interval '5 days','1500-2500 AZN', true,'Kursa başlayanda HTML-dən başqa heç nə bilmirdim. 6 ay sonra bankda ilk işimə başladım. Ən çox portfolio layihələri kömək etdi — müsahibədə məhz onlardan danışdıq.', now()-interval '5 days'),
 (2,'a0000000-0000-4000-8000-000000000002','c0000000-0000-4000-8000-000000000001','PASHA Holding','Frontend Developer', current_date-interval '20 days','2500-4000 AZN', true,'Əvvəllər dizayner işləyirdim, kod yazmağa keçmək istəyirdim. Kurs bu keçidi çox rahat etdi.', now()-interval '20 days'),
 (3,'a0000000-0000-4000-8000-000000000003','c0000000-0000-4000-8000-000000000001','Azercell','Web Developer', current_date-interval '12 days','2500-4000 AZN', true,'Regiondan onlayn iştirak edirdim, heç bir problem yaşamadım. İndi Bakıda işləyirəm.', now()-interval '12 days'),
 (4,'a0000000-0000-4000-8000-000000000011','c0000000-0000-4000-8000-000000000008','Freelance','UI/UX Designer', current_date-interval '3 days','1000-2000 AZN', true,'Təqaüd proqramı sayəsində kursa yazıla bildim. İndi beynəlxalq platformalarda sifariş qəbul edirəm.', now()-interval '3 days'),
 (5,'a0000000-0000-4000-8000-000000000012','c0000000-0000-4000-8000-000000000002','Unibank','Data Analyst', current_date-interval '30 days','2000-3000 AZN', true,'Bank sektorunda analitik vəzifəsinə keçdim. SQL və Pandas hər gün lazım olur.', now()-interval '30 days'),
 (6,'a0000000-0000-4000-8000-000000000016','c0000000-0000-4000-8000-000000000003','ATL Tech','Security Analyst', current_date-interval '8 days','2000-3500 AZN', true,'Kibertəhlükəsizlik sahəsinə giriş etmək çətin görünürdü, amma laboratoriya təcrübəsi hər şeyi dəyişdi.', now()-interval '8 days'),
 (7,'a0000000-0000-4000-8000-000000000017','c0000000-0000-4000-8000-000000000003','Bakcell','SOC Analyst L1', current_date-interval '15 days','1800-2800 AZN', false,NULL, now()-interval '15 days'),
 (8,'a0000000-0000-4000-8000-000000000005','c0000000-0000-4000-8000-000000000006','Nexora Academy','DevOps Engineer', current_date-interval '2 days','3000-5000 AZN', true,'Kursdan sonra öz akademiyamızda işə başladım — indi infrastrukturu mən idarə edirəm.', now()-interval '2 days'),
 (9,'a0000000-0000-4000-8000-000000000013','c0000000-0000-4000-8000-000000000002','SOCAR','Business Analyst', current_date-interval '40 days','2500-4000 AZN', false,NULL, now()-interval '40 days'),
 (10,'a0000000-0000-4000-8000-000000000020','c0000000-0000-4000-8000-000000000004','Beynəlxalq Bank','Penetration Tester', current_date-interval '1 day','3500-6000 AZN', true,'İki kursu ardıcıl bitirdim. İndi red team komandasında işləyirəm.', now()-interval '1 day');
SELECT setval(pg_get_serial_sequence('outcomes.graduate_outcomes','id'), 10, true);

-- ---------------------------------------------------------------------
-- 12. crm.leads — 20 lid
-- ---------------------------------------------------------------------
INSERT INTO crm.leads (id, full_name, email, phone, course_id, source, status, assigned_to, consent_text_version, consent_given_at, activity_log, created_at, updated_at)
VALUES
 ('10000000-0000-4000-8000-000000000001','Səid Quliyev','said.quliyev@mail.az','+994551110001','c0000000-0000-4000-8000-000000000001','contact_form','new','c5085010-6686-4cbc-8534-79df92e7e6f8','v1.3', now()-interval '2 days','[{"at":"2026-07-26","by":"system","note":"Forma doldurulub"}]', now()-interval '2 days', now()-interval '2 days'),
 ('10000000-0000-4000-8000-000000000002','Aygün Məmmədli','aygun.mammadli@mail.az','+994551110002','c0000000-0000-4000-8000-000000000002','demo_request','contacted','c5085010-6686-4cbc-8534-79df92e7e6f8','v1.3', now()-interval '5 days','[{"at":"2026-07-23","by":"sales","note":"Zəng edildi, maraqlıdır"}]', now()-interval '5 days', now()-interval '3 days'),
 ('10000000-0000-4000-8000-000000000003','Elnur Hacıyev','elnur.haciyev@mail.az','+994551110003','c0000000-0000-4000-8000-000000000003','chatbot','qualified','c5085010-6686-4cbc-8534-79df92e7e6f8','v1.3', now()-interval '8 days','[{"at":"2026-07-20","by":"chatbot","note":"Bot vasitəsilə maraq bildirdi"},{"at":"2026-07-22","by":"sales","note":"Büdcəsi uyğundur"}]', now()-interval '8 days', now()-interval '6 days'),
 ('10000000-0000-4000-8000-000000000004','Nərmin Əliyeva','narmin.aliyeva@mail.az','+994551110004','c0000000-0000-4000-8000-000000000008','syllabus_download','converted','c5085010-6686-4cbc-8534-79df92e7e6f8','v1.3', now()-interval '70 days','[{"at":"2026-05-19","by":"sales","note":"Qeydiyyatdan keçdi"}]', now()-interval '70 days', now()-interval '65 days'),
 ('10000000-0000-4000-8000-000000000005','Rauf Babayev','rauf.babayev@mail.az','+994551110005','c0000000-0000-4000-8000-000000000006','contact_form','lost','c5085010-6686-4cbc-8534-79df92e7e6f8','v1.3', now()-interval '45 days','[{"at":"2026-06-20","by":"sales","note":"Başqa akademiyanı seçdi"}]', now()-interval '45 days', now()-interval '30 days'),
 ('10000000-0000-4000-8000-000000000006','Günay Rzayeva','gunay.rzayeva@mail.az','+994551110006','c0000000-0000-4000-8000-000000000005','newsletter','new',NULL,'v1.3', now()-interval '1 day','[]', now()-interval '1 day', now()-interval '1 day'),
 ('10000000-0000-4000-8000-000000000007','Toğrul Səfərli','togrul.safarli@mail.az','+994551110007','c0000000-0000-4000-8000-000000000009','referral','contacted','c5085010-6686-4cbc-8534-79df92e7e6f8','v1.3', now()-interval '6 days','[{"at":"2026-07-24","by":"sales","note":"Dostu tövsiyə edib"}]', now()-interval '6 days', now()-interval '4 days'),
 ('10000000-0000-4000-8000-000000000008','Şəbnəm Kazımova','shabnam.kazimova@mail.az','+994551110008','c0000000-0000-4000-8000-000000000010','demo_request','qualified','c5085010-6686-4cbc-8534-79df92e7e6f8','v1.3', now()-interval '10 days','[{"at":"2026-07-19","by":"sales","note":"ML təcrübəsi var, uyğundur"}]', now()-interval '10 days', now()-interval '8 days'),
 ('10000000-0000-4000-8000-000000000009','Vüsal Nərimanlı','vusal.narimanli@mail.az','+994551110009',NULL,'chatbot','new',NULL,'v1.3', now()-interval '3 hours','[{"at":"2026-07-28","by":"chatbot","note":"Hansı kursun uyğun olduğunu soruşdu"}]', now()-interval '3 hours', now()-interval '3 hours'),
 ('10000000-0000-4000-8000-000000000010','Aysu Mehdiyeva','aysu.mehdiyeva@mail.az','+994551110010','c0000000-0000-4000-8000-000000000001','contact_form','disqualified','c5085010-6686-4cbc-8534-79df92e7e6f8','v1.3', now()-interval '20 days','[{"at":"2026-07-10","by":"sales","note":"Yaş həddi uyğun deyil"}]', now()-interval '20 days', now()-interval '18 days'),
 ('10000000-0000-4000-8000-000000000011','Kənan Abbaslı','kanan.abbasli@mail.az','+994551110011','c0000000-0000-4000-8000-000000000004','syllabus_download','contacted','c5085010-6686-4cbc-8534-79df92e7e6f8','v1.3', now()-interval '12 days','[]', now()-interval '12 days', now()-interval '11 days'),
 ('10000000-0000-4000-8000-000000000012','Zümrüd Əsgərova','zumrud.asgarova@mail.az','+994551110012','c0000000-0000-4000-8000-000000000007','contact_form','new',NULL,'v1.3', now()-interval '4 days','[]', now()-interval '4 days', now()-interval '4 days'),
 ('10000000-0000-4000-8000-000000000013','Ceyhun Muradov','ceyhun.muradov@mail.az','+994551110013','c0000000-0000-4000-8000-000000000002','referral','converted','c5085010-6686-4cbc-8534-79df92e7e6f8','v1.3', now()-interval '40 days','[{"at":"2026-06-25","by":"sales","note":"Qeydiyyat tamamlandı"}]', now()-interval '40 days', now()-interval '35 days'),
 ('10000000-0000-4000-8000-000000000014','Xəyalə Sadıqova','xayala.sadiqova@mail.az','+994551110014','c0000000-0000-4000-8000-000000000008','newsletter','new',NULL,'v1.3', now()-interval '7 days','[]', now()-interval '7 days', now()-interval '7 days'),
 ('10000000-0000-4000-8000-000000000015','Fərid Həsənli','farid.hasanli@mail.az','+994551110015','c0000000-0000-4000-8000-000000000006','demo_request','contacted','c5085010-6686-4cbc-8534-79df92e7e6f8','v1.3', now()-interval '9 days','[]', now()-interval '9 days', now()-interval '7 days'),
 ('10000000-0000-4000-8000-000000000016','Nurlan Qədirov','nurlan.qadirov@mail.az','+994551110016','c0000000-0000-4000-8000-000000000003','chatbot','qualified',NULL,'v1.3', now()-interval '15 days','[]', now()-interval '15 days', now()-interval '13 days'),
 ('10000000-0000-4000-8000-000000000017','Türkan Vəlizadə','turkan.valizada@mail.az','+994551110017','c0000000-0000-4000-8000-000000000005','contact_form','new',NULL,'v1.3', now()-interval '6 hours','[]', now()-interval '6 hours', now()-interval '6 hours'),
 ('10000000-0000-4000-8000-000000000018','Samir Orucov','samir.orucov@mail.az','+994551110018','c0000000-0000-4000-8000-000000000009','syllabus_download','contacted','c5085010-6686-4cbc-8534-79df92e7e6f8','v1.3', now()-interval '18 days','[]', now()-interval '18 days', now()-interval '16 days'),
 ('10000000-0000-4000-8000-000000000019','Mələk Cavadova','malak.javadova@mail.az','+994551110019','c0000000-0000-4000-8000-000000000010','referral','new',NULL,'v1.3', now()-interval '11 days','[]', now()-interval '11 days', now()-interval '11 days'),
 -- dublikat lid (duplicate_of_lead_id testi üçün)
 ('10000000-0000-4000-8000-000000000020','Səid Quliyev','said.quliyev2@mail.az','+994551110001','c0000000-0000-4000-8000-000000000001','contact_form','new',NULL,'v1.3', now()-interval '1 day','[]', now()-interval '1 day', now()-interval '1 day');

UPDATE crm.leads SET duplicate_of_lead_id='10000000-0000-4000-8000-000000000001'
 WHERE id='10000000-0000-4000-8000-000000000020';

-- ---------------------------------------------------------------------
-- 13. crm.contact_submissions — 15 müraciət
-- ---------------------------------------------------------------------
INSERT INTO crm.contact_submissions (id, lead_id, type, course_id, full_name, email, phone, message, preferred_time, status, submitted_at)
VALUES
 ('20000000-0000-4000-8000-000000000001','10000000-0000-4000-8000-000000000001','contact','c0000000-0000-4000-8000-000000000001','Səid Quliyev','said.quliyev@mail.az','+994551110001','Salam, Full-Stack kursunun növbəti qrupu nə vaxt başlayır? İş qrafikim səhərlərdir.', now()+interval '2 days','pending', now()-interval '2 days'),
 ('20000000-0000-4000-8000-000000000002','10000000-0000-4000-8000-000000000002','demo','c0000000-0000-4000-8000-000000000002','Aygün Məmmədli','aygun.mammadli@mail.az','+994551110002','Demo dərsdə iştirak etmək istəyirəm.', now()+interval '1 day','contacted', now()-interval '5 days'),
 ('20000000-0000-4000-8000-000000000003','10000000-0000-4000-8000-000000000003','contact','c0000000-0000-4000-8000-000000000003','Elnur Hacıyev','elnur.haciyev@mail.az','+994551110003','Etik Hacker kursu üçün əvvəlcədən hansı biliklər lazımdır?',NULL,'resolved', now()-interval '8 days'),
 ('20000000-0000-4000-8000-000000000004','10000000-0000-4000-8000-000000000004','syllabus_download','c0000000-0000-4000-8000-000000000008','Nərmin Əliyeva','narmin.aliyeva@mail.az','+994551110004','Kurs proqramını göndərə bilərsinizmi?',NULL,'resolved', now()-interval '70 days'),
 ('20000000-0000-4000-8000-000000000005','10000000-0000-4000-8000-000000000005','contact','c0000000-0000-4000-8000-000000000006','Rauf Babayev','rauf.babayev@mail.az','+994551110005','Taksit imkanı varmı?',NULL,'closed', now()-interval '45 days'),
 ('20000000-0000-4000-8000-000000000006','10000000-0000-4000-8000-000000000006','newsletter',NULL,'Günay Rzayeva','gunay.rzayeva@mail.az',NULL,'Yeniliklərdən xəbərdar olmaq istəyirəm.',NULL,'pending', now()-interval '1 day'),
 ('20000000-0000-4000-8000-000000000007','10000000-0000-4000-8000-000000000007','demo','c0000000-0000-4000-8000-000000000009','Toğrul Səfərli','togrul.safarli@mail.az','+994551110007','Java kursunun demo dərsi olurmu?', now()+interval '4 days','contacted', now()-interval '6 days'),
 ('20000000-0000-4000-8000-000000000008','10000000-0000-4000-8000-000000000008','demo','c0000000-0000-4000-8000-000000000010','Şəbnəm Kazımova','shabnam.kazimova@mail.az','+994551110008','ML kursu üçün Python səviyyəm kifayətdirmi, öyrənmək istəyirəm.', now()+interval '3 days','contacted', now()-interval '10 days'),
 ('20000000-0000-4000-8000-000000000009',NULL,'contact',NULL,'Anonim Ziyarətçi','anonim.ziyaretci@mail.az',NULL,'Ünvanınız haradadır? Offline dərslər üçün soruşuram.',NULL,'pending', now()-interval '2 hours'),
 ('20000000-0000-4000-8000-000000000010','10000000-0000-4000-8000-000000000011','syllabus_download','c0000000-0000-4000-8000-000000000004','Kənan Abbaslı','kanan.abbasli@mail.az','+994551110011','Pentesting kursunun tam siillabusunu istəyirəm.',NULL,'resolved', now()-interval '12 days'),
 ('20000000-0000-4000-8000-000000000011','10000000-0000-4000-8000-000000000012','contact','c0000000-0000-4000-8000-000000000007','Zümrüd Əsgərova','zumrud.asgarova@mail.az','+994551110012','SOC kursundan sonra işə düzəlmə dəstəyi varmı?',NULL,'pending', now()-interval '4 days'),
 ('20000000-0000-4000-8000-000000000012','10000000-0000-4000-8000-000000000014','newsletter',NULL,'Xəyalə Sadıqova','xayala.sadiqova@mail.az',NULL,'Dizayn kursları haqqında məlumat almaq istəyirəm.',NULL,'pending', now()-interval '7 days'),
 ('20000000-0000-4000-8000-000000000013','10000000-0000-4000-8000-000000000015','demo','c0000000-0000-4000-8000-000000000006','Fərid Həsənli','farid.hasanli@mail.az','+994551110015','DevOps demo dərsinə qoşulmaq istəyirəm.', now()+interval '5 days','contacted', now()-interval '9 days'),
 ('20000000-0000-4000-8000-000000000014','10000000-0000-4000-8000-000000000018','syllabus_download','c0000000-0000-4000-8000-000000000009','Samir Orucov','samir.orucov@mail.az','+994551110018','Java kursunun proqramını göndərin.',NULL,'resolved', now()-interval '18 days'),
 ('20000000-0000-4000-8000-000000000015','10000000-0000-4000-8000-000000000017','contact','c0000000-0000-4000-8000-000000000005','Türkan Vəlizadə','turkan.valizada@mail.az','+994551110017','Flutter kursu üçün MacBook lazımdırmı?',NULL,'pending', now()-interval '6 hours');

-- ---------------------------------------------------------------------
-- 14. crm.campaigns — 5 kampaniya
-- ---------------------------------------------------------------------
INSERT INTO crm.campaigns (id, name, banner_image_url, cta_url, discount_pct, starts_at, ends_at, is_active, priority, course_ids)
VALUES
 ('30000000-0000-4000-8000-000000000001','Yay Endirimi 2026','https://picsum.photos/seed/summer2026/1200/400','/kurslar?kampaniya=yay-2026',20, now()-interval '20 days', now()+interval '25 days', true, 100, ARRAY['c0000000-0000-4000-8000-000000000001','c0000000-0000-4000-8000-000000000002','c0000000-0000-4000-8000-000000000008']::uuid[]),
 ('30000000-0000-4000-8000-000000000002','Kibertəhlükəsizlik Həftəsi','https://picsum.photos/seed/cyberweek/1200/400','/kurslar?kateqoriya=kibertehlukesizlik',15, now()-interval '5 days', now()+interval '9 days', true, 90, ARRAY['c0000000-0000-4000-8000-000000000003','c0000000-0000-4000-8000-000000000004','c0000000-0000-4000-8000-000000000007']::uuid[]),
 ('30000000-0000-4000-8000-000000000003','Erkən Qeydiyyat — Payız Qrupları','https://picsum.photos/seed/autumn2026/1200/400','/kurslar?kampaniya=erken-qeydiyyat',10, now()+interval '5 days', now()+interval '50 days', true, 70, ARRAY['c0000000-0000-4000-8000-000000000006','c0000000-0000-4000-8000-000000000009','c0000000-0000-4000-8000-000000000010']::uuid[]),
 ('30000000-0000-4000-8000-000000000004','Qara Cümə 2025','https://picsum.photos/seed/blackfriday25/1200/400','/kurslar',40, now()-interval '250 days', now()-interval '243 days', false, 50, ARRAY['c0000000-0000-4000-8000-000000000001']::uuid[]),
 ('30000000-0000-4000-8000-000000000005','Dost Gətir — 2 nəfər 1 qiymətə','https://picsum.photos/seed/referral/1200/400','/referal',50, now()-interval '60 days', now()+interval '120 days', true, 60, ARRAY[]::uuid[]);

-- ---------------------------------------------------------------------
-- 15. crm.chat_sessions — 8 söhbət (chat-bot widget-i üçün)
-- ---------------------------------------------------------------------
INSERT INTO crm.chat_sessions (id, user_id, lead_id, channel, messages, started_at, ended_at)
VALUES
 ('40000000-0000-4000-8000-000000000001','a0000000-0000-4000-8000-000000000001',NULL,'web_widget','[{"role":"user","text":"Salam, hansı kurslar var?","at":"2026-07-27T10:00:00Z"},{"role":"bot","text":"Salam! Proqramlaşdırma, kibertəhlükəsizlik və dizayn istiqamətlərində kurslarımız var.","at":"2026-07-27T10:00:04Z"},{"role":"user","text":"Proqramlaşdırma","at":"2026-07-27T10:00:30Z"},{"role":"bot","text":"Full-Stack Veb İnkişafı (890 AZN) və Java Backend (950 AZN) kurslarımız var.","at":"2026-07-27T10:00:35Z"}]', now()-interval '1 day', now()-interval '1 day'+interval '12 minutes'),
 ('40000000-0000-4000-8000-000000000002',NULL,'10000000-0000-4000-8000-000000000003','web_widget','[{"role":"user","text":"Etik hacker kursu neçəyədir?","at":"2026-07-20T14:20:00Z"},{"role":"bot","text":"Etik Hacker Əsasları kursu 980 AZN-dir, 20 həftə davam edir.","at":"2026-07-20T14:20:03Z"},{"role":"user","text":"Əlaqə saxlaya bilərsiniz","at":"2026-07-20T14:21:00Z"}]', now()-interval '8 days', now()-interval '8 days'+interval '5 minutes'),
 ('40000000-0000-4000-8000-000000000003',NULL,'10000000-0000-4000-8000-000000000009','web_widget','[{"role":"user","text":"Mənə hansı kurs uyğundur?","at":"2026-07-28T07:00:00Z"},{"role":"bot","text":"Hansı sahə maraqlıdır — proqramlaşdırma, kibertəhlükəsizlik, yoxsa data?","at":"2026-07-28T07:00:02Z"}]', now()-interval '3 hours', NULL),
 ('40000000-0000-4000-8000-000000000004','a0000000-0000-4000-8000-000000000005',NULL,'web_widget','[{"role":"user","text":"DevOps kursunun qrafiki necədir?","at":"2026-07-25T18:00:00Z"},{"role":"bot","text":"Bazar ertəsi və cümə axşamı 19:00-22:00.","at":"2026-07-25T18:00:03Z"}]', now()-interval '3 days', now()-interval '3 days'+interval '4 minutes'),
 ('40000000-0000-4000-8000-000000000005','a0000000-0000-4000-8000-000000000012',NULL,'web_widget','[{"role":"user","text":"Sertifikat verilirmi?","at":"2026-07-22T12:00:00Z"},{"role":"bot","text":"Bəli, kursu uğurla bitirən hər kəsə sertifikat verilir.","at":"2026-07-22T12:00:02Z"}]', now()-interval '6 days', now()-interval '6 days'+interval '3 minutes'),
 ('40000000-0000-4000-8000-000000000006',NULL,'10000000-0000-4000-8000-000000000016','web_widget','[{"role":"user","text":"Offline dərslər haradadır?","at":"2026-07-13T09:00:00Z"},{"role":"bot","text":"Bakı, Nəsimi rayonu — Nexora laboratoriyası.","at":"2026-07-13T09:00:02Z"}]', now()-interval '15 days', now()-interval '15 days'+interval '2 minutes'),
 ('40000000-0000-4000-8000-000000000007','a0000000-0000-4000-8000-000000000003',NULL,'mobile_app','[{"role":"user","text":"Növbəti dərsim nə vaxtdır?","at":"2026-07-27T20:00:00Z"},{"role":"bot","text":"Flutter qrupunuzun növbəti dərsi çərşənbə axşamı 19:30-dadır.","at":"2026-07-27T20:00:03Z"}]', now()-interval '1 day', now()-interval '1 day'+interval '2 minutes'),
 ('40000000-0000-4000-8000-000000000008',NULL,NULL,'web_widget','[{"role":"user","text":"salam","at":"2026-07-28T09:30:00Z"},{"role":"bot","text":"Salam! Sizə necə kömək edə bilərəm?","at":"2026-07-28T09:30:01Z"}]', now()-interval '30 minutes', NULL);

-- ---------------------------------------------------------------------
-- 16. cms.cms_content — 14 məzmun (səhifə, FAQ, sosial link, banner)
-- ---------------------------------------------------------------------
INSERT INTO cms.cms_content (id, key, type, title, body, data, is_published, sort_order, updated_by, updated_at)
OVERRIDING SYSTEM VALUE VALUES
 (1,'page.about','page','Haqqımızda','Nexora Academy 2021-ci ildən bəri Azərbaycanda texnologiya təhsili sahəsində fəaliyyət göstərir. Məqsədimiz sənayenin real tələblərinə uyğun mütəxəssislər hazırlamaqdır. Bu günə qədər 1200-dən çox məzunumuz olub, onların 78%-i ilk 6 ay ərzində sahə üzrə işə düzəlib.','{"heroImage":"https://picsum.photos/seed/about/1200/500","stats":{"graduates":1200,"employmentRate":78,"instructors":24}}',true,10,'a0b7447e-c5ac-4c1f-a309-4caecfbbc061', now()-interval '30 days'),
 (2,'page.contact','page','Əlaqə','Ünvan: Bakı, Nərimanov rayonu, Atatürk prospekti 45. Telefon: +994 12 555 00 11. E-poçt: info@nexoraacademy.az','{"phone":"+994125550011","email":"info@nexoraacademy.az","address":"Bakı, Nərimanov r., Atatürk pr. 45","mapLat":40.4093,"mapLng":49.8671,"workingHours":"B.e-Cümə 09:00-18:00, Şənbə 10:00-15:00"}',true,20,'a0b7447e-c5ac-4c1f-a309-4caecfbbc061', now()-interval '25 days'),
 (3,'page.privacy','page','Məxfilik Siyasəti','Bu sənəd Nexora Academy-nin şəxsi məlumatların toplanması, saxlanması və istifadəsi qaydalarını izah edir. Məlumatlarınız yalnız təhsil xidmətinin göstərilməsi məqsədilə istifadə olunur və üçüncü tərəflərə ötürülmür.','{"version":"v1.3","effectiveFrom":"2026-01-15"}',true,30,'a0b7447e-c5ac-4c1f-a309-4caecfbbc061', now()-interval '60 days'),
 (4,'page.terms','page','İstifadə Şərtləri','Saytdan və təhsil xidmətlərindən istifadə edərkən qüvvədə olan şərtlər.','{"version":"v1.3","effectiveFrom":"2026-01-15"}',true,40,'a0b7447e-c5ac-4c1f-a309-4caecfbbc061', now()-interval '60 days'),
 (5,'faq.payment','faq','Ödənişi hissə-hissə edə bilərəmmi?','Bəli. Bütün kurslar üçün 2 və ya 3 aylıq taksit imkanı mövcuddur. Taksit üçün əlavə komissiya tutulmur. Ətraflı məlumat üçün satış komandamızla əlaqə saxlayın.','{"category":"Ödəniş"}',true,10,'a0b7447e-c5ac-4c1f-a309-4caecfbbc061', now()-interval '20 days'),
 (6,'faq.certificate','faq','Kursu bitirdikdən sonra sertifikat verilirmi?','Bəli. Kursu uğurla (davamiyyət ≥80% və yekun layihə təhvil verilmiş) bitirən hər tələbəyə QR kodu ilə doğrulana bilən rəqəmsal sertifikat verilir.','{"category":"Sertifikat"}',true,20,'a0b7447e-c5ac-4c1f-a309-4caecfbbc061', now()-interval '20 days'),
 (7,'faq.prerequisites','faq','Kurslar üçün əvvəlcədən bilik tələb olunurmu?','Başlanğıc səviyyəli kurslar (UI/UX, Python və Data Analitika, SOC Analitik) heç bir ilkin bilik tələb etmir. Orta və yüksək səviyyəli kurslar üçün müvafiq bölmədə tələblər göstərilib.','{"category":"Ümumi"}',true,30,'a0b7447e-c5ac-4c1f-a309-4caecfbbc061', now()-interval '18 days'),
 (8,'faq.job-support','faq','İşə düzəlmə dəstəyi varmı?','Bəli. Karyera mərkəzimiz CV hazırlanması, LinkedIn profilinin optimallaşdırılması və texniki müsahibəyə hazırlıq üzrə dəstək göstərir. Tərəfdaş şirkətlərə birbaşa tövsiyə də daxildir.','{"category":"Karyera"}',true,40,'a0b7447e-c5ac-4c1f-a309-4caecfbbc061', now()-interval '15 days'),
 (9,'faq.missed-lesson','faq','Dərsi buraxsam nə olur?','Bütün onlayn dərslər yazılır və tələbə panelində 12 ay ərzində əlçatan olur. Offline dərslər üçün müəllim ilə əlavə məsləhət saatı təyin edilə bilər.','{"category":"Dərs prosesi"}',true,50,'a0b7447e-c5ac-4c1f-a309-4caecfbbc061', now()-interval '12 days'),
 (10,'social.facebook','social_link','Facebook',NULL,'{"url":"https://facebook.com/nexoraacademy","icon":"facebook","followers":8400}',true,10,'a0b7447e-c5ac-4c1f-a309-4caecfbbc061', now()-interval '90 days'),
 (11,'social.instagram','social_link','Instagram',NULL,'{"url":"https://instagram.com/nexoraacademy","icon":"instagram","followers":15200}',true,20,'a0b7447e-c5ac-4c1f-a309-4caecfbbc061', now()-interval '90 days'),
 (12,'social.linkedin','social_link','LinkedIn',NULL,'{"url":"https://linkedin.com/company/nexoraacademy","icon":"linkedin","followers":5100}',true,30,'a0b7447e-c5ac-4c1f-a309-4caecfbbc061', now()-interval '90 days'),
 (13,'banner.home-hero','banner','Karyeranı texnologiya ilə qur','Nexora Academy-də praktik kurslar, sənaye mütəxəssisləri və işə düzəlmə dəstəyi bir arada.','{"image":"https://picsum.photos/seed/hero/1600/600","ctaText":"Kurslara bax","ctaUrl":"/kurslar"}',true,10,'a0b7447e-c5ac-4c1f-a309-4caecfbbc061', now()-interval '10 days'),
 -- dərc olunmamış (admin panel testi üçün)
 (14,'banner.autumn-promo','banner','Payız qrupları açıldı','Payız qruplarına erkən qeydiyyat 10% endirimlə davam edir.','{"image":"https://picsum.photos/seed/autumnpromo/1600/600","ctaText":"Qeydiyyat","ctaUrl":"/kurslar?kampaniya=erken-qeydiyyat"}',false,20,'a0b7447e-c5ac-4c1f-a309-4caecfbbc061', now()-interval '2 days');
SELECT setval(pg_get_serial_sequence('cms.cms_content','id'), 14, true);

-- ---------------------------------------------------------------------
-- 17. ai.kb_articles — 10 bilik bazası məqaləsi (chat-bot üçün mənbə)
--     embedding sütunu NULL saxlanılır (vector generasiyası ayrı prosesdir)
-- ---------------------------------------------------------------------
INSERT INTO ai.kb_articles (id, source_type, source_ref_id, title, content, embedding, is_active, updated_at)
VALUES
 ('60000000-0000-4000-8000-000000000001','course','c0000000-0000-4000-8000-000000000001','Full-Stack Veb İnkişafı — ümumi məlumat','Full-Stack Veb İnkişafı kursu 24 həftə davam edir, qiyməti 890 AZN-dir və hibrid formatda keçirilir. HTML/CSS, JavaScript, React, Node.js, Express və PostgreSQL əhatə olunur. Kurs ərzində 3 real layihə hazırlanır.',NULL,true, now()-interval '30 days'),
 ('60000000-0000-4000-8000-000000000002','course','c0000000-0000-4000-8000-000000000002','Python və Data Analitika — ümumi məlumat','Python və Data Analitika kursu 16 həftə, 750 AZN, tam onlayn formatdadır. Pandas, NumPy, Matplotlib və SQL öyrədilir. Başlanğıc səviyyə üçün uyğundur, ilkin bilik tələb olunmur.',NULL,true, now()-interval '28 days'),
 ('60000000-0000-4000-8000-000000000003','course','c0000000-0000-4000-8000-000000000003','Etik Hacker Əsasları — ümumi məlumat','Etik Hacker Əsasları kursu 20 həftə, 980 AZN, offline formatda Bakı Nəsimi laboratoriyasında keçirilir. Kali Linux, reconnaissance, OWASP Top 10 və hesabat yazma əhatə olunur.',NULL,true, now()-interval '26 days'),
 ('60000000-0000-4000-8000-000000000004','course','c0000000-0000-4000-8000-000000000006','DevOps və Bulud Texnologiyaları — ümumi məlumat','DevOps kursu 26 həftə, 1100 AZN, hibrid formatdadır. Docker, Kubernetes, CI/CD, Terraform və AWS xidmətləri öyrədilir. Yüksək səviyyəlidir, Linux biliyi tələb olunur.',NULL,true, now()-interval '24 days'),
 ('60000000-0000-4000-8000-000000000005','faq','5','Taksit imkanı','Bütün kurslar üçün 2 və ya 3 aylıq taksit mövcuddur, əlavə komissiya tutulmur.',NULL,true, now()-interval '20 days'),
 ('60000000-0000-4000-8000-000000000006','faq','6','Sertifikat','Kursu davamiyyət ≥80% və yekun layihə ilə bitirən tələbələrə QR kodlu rəqəmsal sertifikat verilir.',NULL,true, now()-interval '20 days'),
 ('60000000-0000-4000-8000-000000000007','faq','8','İşə düzəlmə dəstəyi','Karyera mərkəzi CV, LinkedIn və texniki müsahibə hazırlığı üzrə dəstək göstərir, tərəfdaş şirkətlərə tövsiyə edir.',NULL,true, now()-interval '15 days'),
 ('60000000-0000-4000-8000-000000000008','page','2','Əlaqə məlumatları','Ünvan: Bakı, Nərimanov rayonu, Atatürk prospekti 45. Telefon: +994 12 555 00 11. İş saatları: B.e-Cümə 09:00-18:00, Şənbə 10:00-15:00.',NULL,true, now()-interval '25 days'),
 ('60000000-0000-4000-8000-000000000009','scholarship','2','Qadınlar IT-də təqaüdü','Texnologiya sahəsində qadın iştirakını artırmaq üçün 50% endirim, 25 nəfər üçün nəzərdə tutulub.',NULL,true, now()-interval '18 days'),
 ('60000000-0000-4000-8000-000000000010','policy','privacy','Məxfilik siyasəti xülasəsi','Şəxsi məlumatlar yalnız təhsil xidmətinin göstərilməsi üçün istifadə olunur, üçüncü tərəflərə ötürülmür. Cari versiya: v1.3.',NULL,false, now()-interval '60 days');

-- ---------------------------------------------------------------------
-- 18. identity.sessions — 6 sessiya (token_hash saxta dəyərdir)
-- ---------------------------------------------------------------------
INSERT INTO identity.sessions (id, user_id, type, token_hash, ip_address, user_agent, issued_at, expires_at, used_at, revoked_at, attempts)
VALUES
 ('70000000-0000-4000-8000-000000000001','a0000000-0000-4000-8000-000000000001','session','seedhash-session-0000000000000000000001','192.168.0.45','Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/126.0', now()-interval '2 days', now()+interval '28 days', NULL, NULL, 0),
 ('70000000-0000-4000-8000-000000000002','a0000000-0000-4000-8000-000000000003','session','seedhash-session-0000000000000000000002','10.0.0.14','Mozilla/5.0 (Macintosh; Intel Mac OS X 14_5) Safari/17.5', now()-interval '1 day', now()+interval '29 days', NULL, NULL, 0),
 ('70000000-0000-4000-8000-000000000003','a0000000-0000-4000-8000-000000000005','session','seedhash-session-0000000000000000000003','192.168.0.77','Mozilla/5.0 (iPhone; CPU iPhone OS 17_5) Mobile Safari', now()-interval '3 days', now()+interval '27 days', NULL, now()-interval '1 day', 0),
 ('70000000-0000-4000-8000-000000000004','a0000000-0000-4000-8000-000000000002','password_reset','seedhash-reset-000000000000000000000004','192.168.0.90','Mozilla/5.0 (Windows NT 10.0) Firefox/127.0', now()-interval '4 hours', now()+interval '20 hours', NULL, NULL, 1),
 ('70000000-0000-4000-8000-000000000005','a0000000-0000-4000-8000-000000000018','email_verify','seedhash-verify-00000000000000000000005','172.16.0.5','Mozilla/5.0 (Linux; Android 14) Chrome/126.0', now()-interval '6 days', now()-interval '5 days', NULL, NULL, 0),
 ('70000000-0000-4000-8000-000000000006','a0000000-0000-4000-8000-000000000012','session','seedhash-session-0000000000000000000006','192.168.0.101','Mozilla/5.0 (Windows NT 10.0; Win64; x64) Edge/126.0', now()-interval '12 hours', now()+interval '29 days', now()-interval '11 hours', NULL, 0);

-- ---------------------------------------------------------------------
-- 19. identity.oauth_accounts — 4 bağlı hesab
-- ---------------------------------------------------------------------
INSERT INTO identity.oauth_accounts (id, user_id, provider, provider_user_id, access_token_enc, refresh_token_enc, linked_at)
OVERRIDING SYSTEM VALUE VALUES
 (1,'a0000000-0000-4000-8000-000000000001','google','google-uid-110045522','enc:AAAA-seed-access-1','enc:AAAA-seed-refresh-1', now()-interval '110 days'),
 (2,'a0000000-0000-4000-8000-000000000004','github','github-uid-88213','enc:AAAA-seed-access-2',NULL, now()-interval '95 days'),
 (3,'a0000000-0000-4000-8000-000000000012','google','google-uid-110099871','enc:AAAA-seed-access-3','enc:AAAA-seed-refresh-3', now()-interval '55 days'),
 (4,'a0000000-0000-4000-8000-000000000016','linkedin','linkedin-uid-45120','enc:AAAA-seed-access-4',NULL, now()-interval '38 days');
SELECT setval(pg_get_serial_sequence('identity.oauth_accounts','id'), 4, true);

-- ---------------------------------------------------------------------
-- 20. notify.notifications — 20 bildiriş
-- ---------------------------------------------------------------------
INSERT INTO notify.notifications (id, user_id, type, channel, payload, status, sent_at, read_at, created_at)
VALUES
 ('50000000-0000-4000-8000-000000000001','a0000000-0000-4000-8000-000000000001','enrollment_confirmed','email','{"courseTitle":"Full-Stack Veb İnkişafı","groupCode":"FS-2026-01"}','sent', now()-interval '155 days', now()-interval '155 days', now()-interval '155 days'),
 ('50000000-0000-4000-8000-000000000002','a0000000-0000-4000-8000-000000000001','course_completed','email','{"courseTitle":"Full-Stack Veb İnkişafı","certificateUrl":"/sertifikat/FS-2026-01-001"}','sent', now()-interval '10 days', now()-interval '9 days', now()-interval '10 days'),
 ('50000000-0000-4000-8000-000000000003','a0000000-0000-4000-8000-000000000001','lesson_reminder','in_app','{"groupCode":"FL-2026-01","startsAt":"2026-07-29T19:30:00Z"}','read', now()-interval '1 day', now()-interval '20 hours', now()-interval '1 day'),
 ('50000000-0000-4000-8000-000000000004','a0000000-0000-4000-8000-000000000002','payment_received','email','{"amount":296.67,"installmentNo":3,"currency":"AZN"}','sent', now()-interval '60 days', NULL, now()-interval '60 days'),
 ('50000000-0000-4000-8000-000000000005','a0000000-0000-4000-8000-000000000005','enrollment_confirmed','email','{"courseTitle":"Full-Stack Veb İnkişafı","groupCode":"FS-2026-02"}','sent', now()-interval '45 days', now()-interval '45 days', now()-interval '45 days'),
 ('50000000-0000-4000-8000-000000000006','a0000000-0000-4000-8000-000000000005','lesson_reminder','push','{"groupCode":"DO-2026-01","startsAt":"2026-07-29T19:00:00Z"}','sent', now()-interval '2 hours', NULL, now()-interval '2 hours'),
 ('50000000-0000-4000-8000-000000000007','a0000000-0000-4000-8000-000000000008','refund_processed','email','{"amount":890.00,"currency":"AZN","reason":"Kursdan imtina"}','sent', now()-interval '20 days', now()-interval '19 days', now()-interval '20 days'),
 ('50000000-0000-4000-8000-000000000008','a0000000-0000-4000-8000-000000000009','payment_pending','email','{"courseTitle":"Full-Stack Veb İnkişafı","dueInDays":3}','sent', now()-interval '4 days', NULL, now()-interval '4 days'),
 ('50000000-0000-4000-8000-000000000009','a0000000-0000-4000-8000-000000000010','seat_hold_expiring','sms','{"groupCode":"FS-2026-03","expiresInHours":48}','queued', NULL, NULL, now()-interval '1 day'),
 ('50000000-0000-4000-8000-000000000010','a0000000-0000-4000-8000-000000000011','review_request','email','{"courseTitle":"UI/UX Dizayn və Frontend Əsasları"}','read', now()-interval '2 days', now()-interval '1 day', now()-interval '2 days'),
 ('50000000-0000-4000-8000-000000000011','a0000000-0000-4000-8000-000000000012','new_campaign','in_app','{"campaignName":"Yay Endirimi 2026","discountPct":20}','read', now()-interval '20 days', now()-interval '19 days', now()-interval '20 days'),
 ('50000000-0000-4000-8000-000000000012','a0000000-0000-4000-8000-000000000014','waitlist_position','email','{"groupCode":"PY-2026-01","position":2}','sent', now()-interval '6 days', NULL, now()-interval '6 days'),
 ('50000000-0000-4000-8000-000000000013','a0000000-0000-4000-8000-000000000015','payment_failed','email','{"amount":750.00,"reason":"Kartda kifayət qədər vəsait yoxdur"}','sent', now()-interval '3 days', now()-interval '3 days', now()-interval '3 days'),
 ('50000000-0000-4000-8000-000000000014','a0000000-0000-4000-8000-000000000016','enrollment_confirmed','email','{"courseTitle":"Etik Hacker Əsasları","groupCode":"EH-2026-01"}','sent', now()-interval '28 days', now()-interval '28 days', now()-interval '28 days'),
 ('50000000-0000-4000-8000-000000000015','a0000000-0000-4000-8000-000000000018','email_verification','email','{"expiresInHours":24}','failed', NULL, NULL, now()-interval '6 days'),
 ('50000000-0000-4000-8000-000000000016','a0000000-0000-4000-8000-000000000019','account_suspended','email','{"reason":"Ödəniş gecikməsi"}','sent', now()-interval '22 days', NULL, now()-interval '22 days'),
 ('50000000-0000-4000-8000-000000000017','a0000000-0000-4000-8000-000000000020','enrollment_confirmed','email','{"courseTitle":"Şəbəkə Təhlükəsizliyi və Pentesting","groupCode":"NP-2026-01"}','sent', now()-interval '10 days', now()-interval '10 days', now()-interval '10 days'),
 ('50000000-0000-4000-8000-000000000018','a0000000-0000-4000-8000-000000000020','certificate_ready','in_app','{"certificateUrl":"/sertifikat/EH-2026-01-004"}','queued', NULL, NULL, now()-interval '5 hours'),
 ('50000000-0000-4000-8000-000000000019','a0000000-0000-4000-8000-000000000003','lesson_reminder','push','{"groupCode":"FL-2026-01","startsAt":"2026-07-29T19:30:00Z"}','sent', now()-interval '3 hours', NULL, now()-interval '3 hours'),
 ('50000000-0000-4000-8000-000000000020','a0000000-0000-4000-8000-000000000007','scholarship_approved','email','{"scholarshipName":"Şəhid ailəsi üzvləri üçün tam təqaüd","discountPct":100}','read', now()-interval '43 days', now()-interval '42 days', now()-interval '43 days');

-- ---------------------------------------------------------------------
-- 21. platform.audit_logs — 16 audit qeydi
-- ---------------------------------------------------------------------
INSERT INTO platform.audit_logs (id, actor_id, action, entity_type, entity_id, before_state, after_state, ip_address, created_at)
OVERRIDING SYSTEM VALUE VALUES
 (1,'a0b7447e-c5ac-4c1f-a309-4caecfbbc061','CREATE','Course','c0000000-0000-4000-8000-000000000001',NULL,'{"title":"Full-Stack Veb İnkişafı","basePrice":890,"isPublished":false}','192.168.0.10', now()-interval '200 days'),
 (2,'a0b7447e-c5ac-4c1f-a309-4caecfbbc061','UPDATE','Course','c0000000-0000-4000-8000-000000000001','{"isPublished":false}','{"isPublished":true}','192.168.0.10', now()-interval '198 days'),
 (3,'a0b7447e-c5ac-4c1f-a309-4caecfbbc061','CREATE','Course','c0000000-0000-4000-8000-000000000010',NULL,'{"title":"Süni İntellekt və Machine Learning","basePrice":1350}','192.168.0.10', now()-interval '110 days'),
 (4,'87c8de10-2e79-44db-8693-92062e67cadf','UPDATE','Course','c0000000-0000-4000-8000-000000000012','{"isArchived":false,"isActive":true}','{"isArchived":true,"isActive":false}','192.168.0.11', now()-interval '90 days'),
 (5,'87c8de10-2e79-44db-8693-92062e67cadf','UPDATE','User','a0000000-0000-4000-8000-000000000019','{"status":"active"}','{"status":"suspended"}','192.168.0.11', now()-interval '22 days'),
 (6,'c5085010-6686-4cbc-8534-79df92e7e6f8','UPDATE','Lead','10000000-0000-4000-8000-000000000003','{"status":"contacted"}','{"status":"qualified"}','192.168.0.12', now()-interval '6 days'),
 (7,'c5085010-6686-4cbc-8534-79df92e7e6f8','UPDATE','Lead','10000000-0000-4000-8000-000000000004','{"status":"qualified"}','{"status":"converted"}','192.168.0.12', now()-interval '65 days'),
 (8,'87c8de10-2e79-44db-8693-92062e67cadf','UPDATE','CourseReview','1','{"isPublished":false}','{"isPublished":true}','192.168.0.11', now()-interval '8 days'),
 (9,'87c8de10-2e79-44db-8693-92062e67cadf','DELETE','CourseReview','99','{"rating":1,"comment":"spam məzmun"}',NULL,'192.168.0.11', now()-interval '30 days'),
 (10,'65202a51-f28c-42e2-8e90-0e70b3c12632','UPDATE','User','a0000000-0000-4000-8000-000000000021','{"role":"student"}','{"role":"content_manager"}','192.168.0.13', now()-interval '300 days'),
 (11,'a0b7447e-c5ac-4c1f-a309-4caecfbbc061','CREATE','CourseGroup','d0000000-0000-4000-8000-000000000003',NULL,'{"groupCode":"FS-2026-03","totalSeats":22}','192.168.0.10', now()-interval '5 days'),
 (12,'87c8de10-2e79-44db-8693-92062e67cadf','UPDATE','Payment','f0000000-0000-4000-8000-000000000008','{"status":"captured"}','{"status":"refunded","refundAmount":890}','192.168.0.11', now()-interval '20 days'),
 (13,'87c8de10-2e79-44db-8693-92062e67cadf','CREATE','Scholarship','2',NULL,'{"name":"Qadınlar IT-də","discountPct":50}','192.168.0.11', now()-interval '150 days'),
 (14,'a0b7447e-c5ac-4c1f-a309-4caecfbbc061','UPDATE','CmsContent','13','{"isPublished":false}','{"isPublished":true}','192.168.0.10', now()-interval '10 days'),
 (15,'c5085010-6686-4cbc-8534-79df92e7e6f8','CREATE','Campaign','30000000-0000-4000-8000-000000000002',NULL,'{"name":"Kibertəhlükəsizlik Həftəsi","discountPct":15}','192.168.0.12', now()-interval '5 days'),
 (16,'65202a51-f28c-42e2-8e90-0e70b3c12632','LOGIN','User','65202a51-f28c-42e2-8e90-0e70b3c12632',NULL,'{"result":"success"}','127.0.0.1', now()-interval '1 hour');
SELECT setval(pg_get_serial_sequence('platform.audit_logs','id'), 16, true);

COMMIT;

-- ---------------------------------------------------------------------
-- Yekun hesabat
-- ---------------------------------------------------------------------
SELECT 'identity.users'            AS cedvel, count(*) FROM identity.users
UNION ALL SELECT 'catalog.categories',        count(*) FROM catalog.categories
UNION ALL SELECT 'catalog.courses',           count(*) FROM catalog.courses
UNION ALL SELECT 'catalog.instructors',       count(*) FROM catalog.instructors
UNION ALL SELECT 'catalog.course_instructors',count(*) FROM catalog.course_instructors
UNION ALL SELECT 'academics.course_groups',   count(*) FROM academics.course_groups
UNION ALL SELECT 'academics.enrollments',     count(*) FROM academics.enrollments
UNION ALL SELECT 'billing.payments',          count(*) FROM billing.payments
UNION ALL SELECT 'billing.scholarships',      count(*) FROM billing.scholarships
UNION ALL SELECT 'outcomes.course_reviews',   count(*) FROM outcomes.course_reviews
UNION ALL SELECT 'outcomes.graduate_outcomes',count(*) FROM outcomes.graduate_outcomes
UNION ALL SELECT 'crm.leads',                 count(*) FROM crm.leads
UNION ALL SELECT 'crm.contact_submissions',   count(*) FROM crm.contact_submissions
UNION ALL SELECT 'crm.campaigns',             count(*) FROM crm.campaigns
UNION ALL SELECT 'crm.chat_sessions',         count(*) FROM crm.chat_sessions
UNION ALL SELECT 'cms.cms_content',           count(*) FROM cms.cms_content
UNION ALL SELECT 'ai.kb_articles',            count(*) FROM ai.kb_articles
UNION ALL SELECT 'identity.sessions',         count(*) FROM identity.sessions
UNION ALL SELECT 'identity.oauth_accounts',   count(*) FROM identity.oauth_accounts
UNION ALL SELECT 'notify.notifications',      count(*) FROM notify.notifications
UNION ALL SELECT 'platform.audit_logs',       count(*) FROM platform.audit_logs
ORDER BY 1;
