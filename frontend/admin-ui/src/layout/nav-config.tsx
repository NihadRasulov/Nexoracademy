import {
  BookOpen,
  BriefcaseBusiness,
  CircleHelp,
  FileText,
  Gauge,
  GraduationCap,
  Inbox,
  Newspaper,
  PanelsTopLeft,
  type LucideIcon,
} from "lucide-react";

export interface NavItem {
  label: string;
  to: string;
  icon: LucideIcon;
  description?: string;
  keywords?: string[];
}

export interface NavGroup {
  label: string;
  items: NavItem[];
}

export const NAV_GROUPS: NavGroup[] = [
  {
    label: "Ümumi",
    items: [{ label: "Ana panel", to: "/dashboard", icon: Gauge, description: "Canlı göstəricilər və son fəaliyyət", keywords: ["dashboard", "statistika", "hesabat"] }],
  },
  {
    label: "Sayt və kontent",
    items: [
      { label: "Ana səhifə", to: "/homepage", icon: PanelsTopLeft, description: "Hero, statistika və sayt parametrləri", keywords: ["homepage", "hero", "esas sehife"] },
      { label: "Akademiya", to: "/academy", icon: GraduationCap, description: "Akademiya səhifəsinin məzmunu", keywords: ["about", "haqqimizda", "akademiya"] },
      { label: "Karyera", to: "/career", icon: BriefcaseBusiness, description: "Karyera səhifəsinin məzmunu", keywords: ["career", "karyera", "hero", "yol xeritesi"] },
      { label: "Xəbərlər", to: "/news", icon: Newspaper, description: "Xəbər yarat və yayımla", keywords: ["xeber", "blog", "məqalə", "yazi"] },
      { label: "FAQ", to: "/faq", icon: CircleHelp, description: "Tez-tez verilən suallar", keywords: ["sual", "cavab", "faq"] },
      { label: "Vakansiyalar", to: "/vacancies", icon: FileText, description: "Açıq iş elanları", keywords: ["vakansiya", "is elani", "karyera"] },
    ],
  },
  {
    label: "Tədris",
    items: [{ label: "Kurs kataloqu", to: "/catalog", icon: BookOpen, description: "Kurs, kateqoriya və təlimçilər", keywords: ["kurs", "kateqoriya", "muellim", "telimci"] }],
  },
  {
    label: "Müraciətlər",
    items: [{ label: "Gələnlər", to: "/inbox", icon: Inbox, description: "Başvuru, mesaj və abunəçilər", keywords: ["inbox", "muraciet", "mesaj", "cv", "newsletter", "abune"] }],
  },
];
