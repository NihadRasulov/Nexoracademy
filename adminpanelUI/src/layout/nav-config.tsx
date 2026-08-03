import type { RoleGroup } from "@/auth/roles";
import {
  Award,
  BadgePercent,
  BookOpen,
  Bot,
  CalendarRange,
  ClipboardList,
  FileText,
  Gauge,
  GraduationCap,
  KeyRound,
  LayoutGrid,
  Link2,
  MailWarning,
  MessagesSquare,
  Receipt,
  ScrollText,
  Star,
  Users,
  UsersRound,
  type LucideIcon,
} from "lucide-react";

export interface NavItem {
  label: string;
  to: string;
  icon: LucideIcon;
  roleGroup: RoleGroup;
}

export interface NavGroup {
  label: string;
  items: NavItem[];
}

export const NAV_GROUPS: NavGroup[] = [
  {
    label: "Ümumi",
    items: [{ label: "Ana Panel", to: "/dashboard", icon: Gauge, roleGroup: "any" }],
  },
  {
    label: "İstifadəçilər",
    items: [{ label: "İstifadəçilər", to: "/users", icon: Users, roleGroup: "adminOnly" }],
  },
  {
    label: "Akademik Kontent",
    items: [
      { label: "Kateqoriyalar", to: "/categories", icon: LayoutGrid, roleGroup: "contentManager" },
      { label: "Kurslar", to: "/courses", icon: BookOpen, roleGroup: "contentManager" },
      { label: "Müəllimlər", to: "/instructors", icon: UsersRound, roleGroup: "contentManager" },
      { label: "Kurs-Müəllim Əlaqələri", to: "/course-instructors", icon: Link2, roleGroup: "contentManager" },
      { label: "Kurs Qrupları", to: "/course-groups", icon: CalendarRange, roleGroup: "contentManager" },
      { label: "Kurs Rəyləri", to: "/course-reviews", icon: Star, roleGroup: "contentManager" },
      { label: "Məzun Hekayələri", to: "/graduate-outcomes", icon: GraduationCap, roleGroup: "contentManager" },
      { label: "Bilgi Bazası", to: "/kb-articles", icon: Bot, roleGroup: "contentManager" },
      { label: "CMS Məzmun", to: "/cms-content", icon: FileText, roleGroup: "contentManager" },
    ],
  },
  {
    label: "Qeydiyyat & Ödəniş",
    items: [
      { label: "Qeydiyyatlar", to: "/enrollments", icon: ClipboardList, roleGroup: "salesCrm" },
      { label: "Ödənişlər", to: "/payments", icon: Receipt, roleGroup: "adminOnly" },
      { label: "Təqaüdlər", to: "/scholarships", icon: BadgePercent, roleGroup: "adminOnly" },
    ],
  },
  {
    label: "CRM & Satış",
    items: [
      { label: "Kampaniyalar", to: "/campaigns", icon: Award, roleGroup: "salesCrm" },
      { label: "Lead-ler", to: "/leads", icon: MailWarning, roleGroup: "salesCrm" },
      { label: "Əlaqə Formaları", to: "/contact-submissions", icon: MessagesSquare, roleGroup: "salesCrm" },
      { label: "Chat Sessiyaları", to: "/chat-sessions", icon: MessagesSquare, roleGroup: "salesCrm" },
    ],
  },
  {
    label: "Sistem",
    items: [
      { label: "Müraciətlər", to: "/applications", icon: ClipboardList, roleGroup: "adminOnly" },
      { label: "OAuth Hesabları", to: "/oauth-accounts", icon: KeyRound, roleGroup: "adminOnly" },
      { label: "Sessiyalar", to: "/sessions", icon: KeyRound, roleGroup: "adminOnly" },
      { label: "Bildirişlər", to: "/notifications", icon: MailWarning, roleGroup: "adminOnly" },
      { label: "Audit Loqları", to: "/audit-logs", icon: ScrollText, roleGroup: "adminOnly" },
    ],
  },
];
