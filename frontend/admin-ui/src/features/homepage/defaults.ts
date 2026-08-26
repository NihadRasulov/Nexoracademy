import type { HomepageData, SiteSettingsData } from "@/features/homepage/types";

const TEAM = [
  ["Asim Namazov", "Təsisçi, təlimçi", "01"],
  ["Novruz Bəhramov", "Təlimçi", "02"],
  ["Xalidə Həsənova", "Satış meneceri", "03"],
  ["Ayşə Qəhrəmanova", "Mentor", "04"],
  ["Zeynəb Bahramova", "Menecer", "05"],
  ["Ləman Kərimova", "Mentor", "06"],
  ["Nəzrin Şərbətli", "Təcrübəçi", "07"],
  ["Nihad Rəsulov", "Backend mühəndisi", "08"],
  ["Zəhra Eldarova", "Təcrübəçi", "09"],
  ["Yusif İsmayılzadə", "Süni İntellekt mühəndisi", "10"],
  ["Nəcabət Sofiyeva", "Təcrübəçi", "12"],
  ["Murad Şirinov", "UX:UI dizayner", "13"],
  ["Yaqub Abdullayev", "Devops mühəndisi", "14"],
  ["Fikrət Qurbanov", "Frontend mühəndisi", "15"],
  ["Məhəmməd Verdiyev", "Süni İntellekt mühəndisi", "16"],
  ["Qüdrət Bayramov", "Təcrübəçi", "17"],
  ["Ramil Musazadə", "Həllər üzrə arxitektor", "18"],
  ["Emil Rozenberq", "QA mühəndisi", "19"],
  ["Rəşid Əlizadə", "Frontend mühəndisi", "20"],
] as const;

export const DEFAULT_HOMEPAGE: HomepageData = {
  hero: {
    titleLead: "Nexora Academy —",
    titleAccent: "Networking Simplified",
    videoUrl: "assets/heroVideo.mp4",
    posterUrl: "",
  },
  stats: [
    { value: "3+", label: "Təlimçi və mentorlar" },
    { value: "100+", label: "Tələbə və məzunlar" },
    { value: "20+", label: "İşçi heyəti" },
    { value: "3+", label: "Kurslar" },
  ],
  services: {
    titleLead: "Nexora Academy-də",
    titleAccent: "texnologiya təhsilini",
    titleTail: "kurs seçimi, praktik proqramlar, qeydiyyat və tələbə dəstəyi ilə aydın və etibarlı təcrübəyə çeviririk.",
    items: [
      {
        title: "Aydın kurs kəşfi",
        description: "Kursları kateqoriya, çətinlik səviyyəsi və məqsədinizə görə araşdırın; auditoriya, müddət, format və gözlənilən nəticələri müqayisə edin.",
        imageUrl: "assets/home/home-aydin-kurs-kesfi.png",
      },
      {
        title: "Praktik tədris proqramları",
        description: "Real tapşırıqlar, laboratoriyalar və layihələrlə nəzəri biliyi praktik bacarığa çevirin.",
        imageUrl: "assets/home/home-praktik-proqramlar.png",
      },
      {
        title: "Karyera və mentor dəstəyi",
        description: "Təhsil qərarınızı karyera yönümü, mentor dəstəyi, tələbə uğur hekayələri və təsdiqlənmiş məzun nəticələri ilə tamamlayın.",
        imageUrl: "assets/home/home-karyera-mentor.png",
      },
    ],
  },
  about: {
    titleLead: "Biz",
    titleAccent: "kimik?",
    description: "Nexora Academy kursların kəşfi, praktik tədris proqramları, müraciətlər və karyera dəstəyini aydın rəqəmsal təcrübədə birləşdirir.",
    highlights: [
      { value: "Aydın", label: "kurs seçimi" },
      { value: "Şəffaf", label: "qeydiyyat və dəstək" },
    ],
    team: TEAM.map(([name, role, image]) => ({
      name,
      role,
      imageUrl: `assets/nexora-portraits/nexora-team-${image}.jpg`,
    })),
  },
  roadmap: {
    eyebrow: "Tələbənin",
    title: "Nexora yol xəritəsi",
    description: "Kursu kəşf etməkdən müraciətə və praktik tədrisə qədər aydın inkişaf yolu",
    items: [
      { title: "Kəşf", description: "Məqsədinizə uyğun kursları araşdırın" },
      { title: "Məlumat", description: "Kurs detallarını müqayisə edin" },
      { title: "Müraciət", description: "Əlaqə məlumatınızı göndərin" },
      { title: "Görüş", description: "Uyğun proqramı dəqiqləşdirin" },
      { title: "Tədris", description: "Praktik dərslərə başlayın" },
      { title: "Layihə", description: "Real tapşırıqlar hazırlayın" },
      { title: "Mentor dəstəyi", description: "İnkişaf istiqamətinizi qurun" },
      { title: "Nəticə", description: "Yeni bacarıqlarınızı nümayiş etdirin" },
    ],
  },
  featuredCourseIds: [],
  sections: {
    coursesTitleLead: "Seçilmiş",
    coursesTitleAccent: "kurslar",
    newsTitleLead: "Akademiyadan",
    newsTitleAccent: "yeniliklər və elanlar",
    applicationTitleLead: "Növbəti addımın",
    applicationTitleAccent: "Nexora ilə başlayır",
    newsletterTitle: "Nexora Academy kursları, qrupları və elanları barədə yenilikləri alın",
  },
};

export const DEFAULT_SITE_SETTINGS: SiteSettingsData = {
  address: "AF City, Baku, Azerbaijan",
  addressUrl: "https://maps.app.goo.gl/sLNza7qxnoQFUVLr9",
  phone: "+994 50 669 04 52",
  email: "office@nexoracademy.az",
  socials: [
    { label: "Instagram", url: "https://www.instagram.com/_nexoracademy" },
    { label: "LinkedIn", url: "https://www.linkedin.com/company/nexoracademy/" },
    { label: "Facebook", url: "https://www.facebook.com/profile.php?id=61591853181639" },
    { label: "WhatsApp", url: "https://wa.me/994506690452" },
  ],
};

function isObject(value: unknown): value is Record<string, unknown> {
  return Boolean(value) && typeof value === "object" && !Array.isArray(value);
}

function stringValue(value: unknown, fallback: string): string {
  return typeof value === "string" ? value : fallback;
}

function objectList(value: unknown): Record<string, unknown>[] {
  return Array.isArray(value) ? value.filter(isObject) : [];
}

function normalizeMetrics(value: unknown, fallback: HomepageData["stats"]): HomepageData["stats"] {
  if (!Array.isArray(value)) return structuredClone(fallback);
  return objectList(value).map((item) => ({
    value: stringValue(item.value, ""),
    label: stringValue(item.label, ""),
  }));
}

export function normalizeHomepage(value: unknown): HomepageData {
  const source = isObject(value) ? value : {};
  const hero = isObject(source.hero) ? source.hero : {};
  const services = isObject(source.services) ? source.services : {};
  const about = isObject(source.about) ? source.about : {};
  const roadmap = isObject(source.roadmap) ? source.roadmap : {};
  const sections = isObject(source.sections) ? source.sections : {};

  return {
    hero: {
      titleLead: stringValue(hero.titleLead, DEFAULT_HOMEPAGE.hero.titleLead),
      titleAccent: stringValue(hero.titleAccent, DEFAULT_HOMEPAGE.hero.titleAccent),
      videoUrl: stringValue(hero.videoUrl, DEFAULT_HOMEPAGE.hero.videoUrl),
      posterUrl: stringValue(hero.posterUrl, DEFAULT_HOMEPAGE.hero.posterUrl),
    },
    stats: normalizeMetrics(source.stats, DEFAULT_HOMEPAGE.stats),
    services: {
      titleLead: stringValue(services.titleLead, DEFAULT_HOMEPAGE.services.titleLead),
      titleAccent: stringValue(services.titleAccent, DEFAULT_HOMEPAGE.services.titleAccent),
      titleTail: stringValue(services.titleTail, DEFAULT_HOMEPAGE.services.titleTail),
      items: Array.isArray(services.items)
        ? objectList(services.items).map((item) => ({
            title: stringValue(item.title, ""),
            description: stringValue(item.description, ""),
            imageUrl: stringValue(item.imageUrl, ""),
          }))
        : structuredClone(DEFAULT_HOMEPAGE.services.items),
    },
    about: {
      titleLead: stringValue(about.titleLead, DEFAULT_HOMEPAGE.about.titleLead),
      titleAccent: stringValue(about.titleAccent, DEFAULT_HOMEPAGE.about.titleAccent),
      description: stringValue(about.description, DEFAULT_HOMEPAGE.about.description),
      highlights: normalizeMetrics(about.highlights, DEFAULT_HOMEPAGE.about.highlights),
      team: Array.isArray(about.team)
        ? objectList(about.team).map((item) => ({
            name: stringValue(item.name, ""),
            role: stringValue(item.role, ""),
            imageUrl: stringValue(item.imageUrl, ""),
          }))
        : structuredClone(DEFAULT_HOMEPAGE.about.team),
    },
    roadmap: {
      eyebrow: stringValue(roadmap.eyebrow, DEFAULT_HOMEPAGE.roadmap.eyebrow),
      title: stringValue(roadmap.title, DEFAULT_HOMEPAGE.roadmap.title),
      description: stringValue(roadmap.description, DEFAULT_HOMEPAGE.roadmap.description),
      items: Array.isArray(roadmap.items)
        ? objectList(roadmap.items).map((item) => ({
            title: stringValue(item.title, ""),
            description: stringValue(item.description, ""),
          }))
        : structuredClone(DEFAULT_HOMEPAGE.roadmap.items),
    },
    featuredCourseIds: Array.isArray(source.featuredCourseIds)
      ? source.featuredCourseIds
          .filter((item): item is string | number => typeof item === "string" || typeof item === "number")
          .map(String)
          .slice(0, 3)
      : [],
    sections: {
      coursesTitleLead: stringValue(sections.coursesTitleLead, DEFAULT_HOMEPAGE.sections.coursesTitleLead),
      coursesTitleAccent: stringValue(sections.coursesTitleAccent, DEFAULT_HOMEPAGE.sections.coursesTitleAccent),
      newsTitleLead: stringValue(sections.newsTitleLead, DEFAULT_HOMEPAGE.sections.newsTitleLead),
      newsTitleAccent: stringValue(sections.newsTitleAccent, DEFAULT_HOMEPAGE.sections.newsTitleAccent),
      applicationTitleLead: stringValue(sections.applicationTitleLead, DEFAULT_HOMEPAGE.sections.applicationTitleLead),
      applicationTitleAccent: stringValue(sections.applicationTitleAccent, DEFAULT_HOMEPAGE.sections.applicationTitleAccent),
      newsletterTitle: stringValue(sections.newsletterTitle, DEFAULT_HOMEPAGE.sections.newsletterTitle),
    },
  };
}

export function normalizeSiteSettings(value: unknown): SiteSettingsData {
  const source = isObject(value) ? value : {};
  return {
    address: stringValue(source.address, DEFAULT_SITE_SETTINGS.address),
    addressUrl: stringValue(source.addressUrl, DEFAULT_SITE_SETTINGS.addressUrl),
    phone: stringValue(source.phone, DEFAULT_SITE_SETTINGS.phone),
    email: stringValue(source.email, DEFAULT_SITE_SETTINGS.email),
    socials: Array.isArray(source.socials)
      ? objectList(source.socials).map((item) => ({
          label: stringValue(item.label, ""),
          url: stringValue(item.url, ""),
        }))
      : structuredClone(DEFAULT_SITE_SETTINGS.socials),
  };
}
