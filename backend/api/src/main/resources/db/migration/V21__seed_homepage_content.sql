-- Homepage and shared site settings are stored in the existing CMS table.
-- ON CONFLICT DO NOTHING preserves content already entered by administrators.

INSERT INTO cms.cms_content
    (key, type, title, body, data, is_published, sort_order, updated_by, updated_at)
VALUES
    (
        'page.home',
        'page',
        'Nexora Academy — Ana səhifə',
        'Ana səhifənin idarə olunan məzmunu.',
        $homepage$
        {
          "hero": {
            "titleLead": "Nexora Academy —",
            "titleAccent": "Networking Simplified",
            "videoUrl": "assets/heroVideo.mp4",
            "posterUrl": ""
          },
          "stats": [
            {"value": "3+", "label": "Təlimçi və mentorlar"},
            {"value": "100+", "label": "Tələbə və məzunlar"},
            {"value": "20+", "label": "İşçi heyəti"},
            {"value": "3+", "label": "Kurslar"}
          ],
          "services": {
            "titleLead": "Nexora Academy-də",
            "titleAccent": "texnologiya təhsilini",
            "titleTail": "kurs seçimi, praktik proqramlar, qeydiyyat və tələbə dəstəyi ilə aydın və etibarlı təcrübəyə çeviririk.",
            "items": [
              {
                "title": "Aydın kurs kəşfi",
                "description": "Kursları kateqoriya, çətinlik səviyyəsi və məqsədinizə görə araşdırın; auditoriya, müddət, format və gözlənilən nəticələri müqayisə edin.",
                "imageUrl": "assets/home/home-aydin-kurs-kesfi.png"
              },
              {
                "title": "Praktik tədris proqramları",
                "description": "Real tapşırıqlar, laboratoriyalar və layihələrlə nəzəri biliyi praktik bacarığa çevirin.",
                "imageUrl": "assets/home/home-praktik-proqramlar.png"
              },
              {
                "title": "Karyera və mentor dəstəyi",
                "description": "Təhsil qərarınızı karyera yönümü, mentor dəstəyi, tələbə uğur hekayələri və təsdiqlənmiş məzun nəticələri ilə tamamlayın.",
                "imageUrl": "assets/home/home-karyera-mentor.png"
              }
            ]
          },
          "about": {
            "titleLead": "Biz",
            "titleAccent": "kimik?",
            "description": "Nexora Academy kursların kəşfi, praktik tədris proqramları, müraciətlər və karyera dəstəyini aydın rəqəmsal təcrübədə birləşdirir.",
            "highlights": [
              {"value": "Aydın", "label": "kurs seçimi"},
              {"value": "Şəffaf", "label": "qeydiyyat və dəstək"}
            ],
            "team": [
              {"name": "Asim Namazov", "role": "Təsisçi, təlimçi", "imageUrl": "assets/nexora-portraits/nexora-team-01.jpg"},
              {"name": "Novruz Bəhramov", "role": "Təlimçi", "imageUrl": "assets/nexora-portraits/nexora-team-02.jpg"},
              {"name": "Xalidə Həsənova", "role": "Satış meneceri", "imageUrl": "assets/nexora-portraits/nexora-team-03.jpg"},
              {"name": "Ayşə Qəhrəmanova", "role": "Mentor", "imageUrl": "assets/nexora-portraits/nexora-team-04.jpg"},
              {"name": "Zeynəb Bahramova", "role": "Menecer", "imageUrl": "assets/nexora-portraits/nexora-team-05.jpg"},
              {"name": "Ləman Kərimova", "role": "Mentor", "imageUrl": "assets/nexora-portraits/nexora-team-06.jpg"},
              {"name": "Nəzrin Şərbətli", "role": "Təcrübəçi", "imageUrl": "assets/nexora-portraits/nexora-team-07.jpg"},
              {"name": "Nihad Rəsulov", "role": "Backend mühəndisi", "imageUrl": "assets/nexora-portraits/nexora-team-08.jpg"},
              {"name": "Zəhra Eldarova", "role": "Təcrübəçi", "imageUrl": "assets/nexora-portraits/nexora-team-09.jpg"},
              {"name": "Yusif İsmayılzadə", "role": "Süni İntellekt mühəndisi", "imageUrl": "assets/nexora-portraits/nexora-team-10.jpg"},
              {"name": "Nəcabət Sofiyeva", "role": "Təcrübəçi", "imageUrl": "assets/nexora-portraits/nexora-team-12.jpg"},
              {"name": "Murad Şirinov", "role": "UX:UI dizayner", "imageUrl": "assets/nexora-portraits/nexora-team-13.jpg"},
              {"name": "Yaqub Abdullayev", "role": "Devops mühəndisi", "imageUrl": "assets/nexora-portraits/nexora-team-14.jpg"},
              {"name": "Fikrət Qurbanov", "role": "Frontend mühəndisi", "imageUrl": "assets/nexora-portraits/nexora-team-15.jpg"},
              {"name": "Məhəmməd Verdiyev", "role": "Süni İntellekt mühəndisi", "imageUrl": "assets/nexora-portraits/nexora-team-16.jpg"},
              {"name": "Qüdrət Bayramov", "role": "Təcrübəçi", "imageUrl": "assets/nexora-portraits/nexora-team-17.jpg"},
              {"name": "Ramil Musazadə", "role": "Həllər üzrə arxitektor", "imageUrl": "assets/nexora-portraits/nexora-team-18.jpg"},
              {"name": "Emil Rozenberq", "role": "QA mühəndisi", "imageUrl": "assets/nexora-portraits/nexora-team-19.jpg"},
              {"name": "Rəşid Əlizadə", "role": "Frontend mühəndisi", "imageUrl": "assets/nexora-portraits/nexora-team-20.jpg"}
            ]
          },
          "roadmap": {
            "eyebrow": "Tələbənin",
            "title": "Nexora yol xəritəsi",
            "description": "Kursu kəşf etməkdən müraciətə və praktik tədrisə qədər aydın inkişaf yolu",
            "items": [
              {"title": "Kəşf", "description": "Məqsədinizə uyğun kursları araşdırın"},
              {"title": "Məlumat", "description": "Kurs detallarını müqayisə edin"},
              {"title": "Müraciət", "description": "Əlaqə məlumatınızı göndərin"},
              {"title": "Görüş", "description": "Uyğun proqramı dəqiqləşdirin"},
              {"title": "Tədris", "description": "Praktik dərslərə başlayın"},
              {"title": "Layihə", "description": "Real tapşırıqlar hazırlayın"},
              {"title": "Mentor dəstəyi", "description": "İnkişaf istiqamətinizi qurun"},
              {"title": "Nəticə", "description": "Yeni bacarıqlarınızı nümayiş etdirin"}
            ]
          },
          "featuredCourseIds": [],
          "sections": {
            "coursesTitleLead": "Seçilmiş",
            "coursesTitleAccent": "kurslar",
            "newsTitleLead": "Akademiyadan",
            "newsTitleAccent": "yeniliklər və elanlar",
            "applicationTitleLead": "Növbəti addımın",
            "applicationTitleAccent": "Nexora ilə başlayır",
            "newsletterTitle": "Nexora Academy kursları, qrupları və elanları barədə yenilikləri alın"
          }
        }
        $homepage$::jsonb,
        true,
        0,
        NULL,
        now()
    ),
    (
        'page.site-settings',
        'page',
        'Sayt ayarları',
        'Bütün ictimai səhifələrdə istifadə olunan əlaqə və sosial media məlumatları.',
        $settings$
        {
          "address": "AF City, Baku, Azerbaijan",
          "addressUrl": "https://maps.app.goo.gl/sLNza7qxnoQFUVLr9",
          "phone": "+994 50 669 04 52",
          "email": "office@nexoracademy.az",
          "socials": [
            {"label": "Instagram", "url": "https://www.instagram.com/_nexoracademy"},
            {"label": "LinkedIn", "url": "https://www.linkedin.com/company/nexoracademy/"},
            {"label": "Facebook", "url": "https://www.facebook.com/profile.php?id=61591853181639"},
            {"label": "WhatsApp", "url": "https://wa.me/994506690452"}
          ]
        }
        $settings$::jsonb,
        true,
        1,
        NULL,
        now()
    )
ON CONFLICT (key) DO NOTHING;
