import type { ResourceConfig } from "@/resources/types";
import { BoolBadge, formatDate, formatDateTime, MonoId, StatusBadge } from "@/lib/format";
import { ReferenceLabel } from "@/components/reference-picker";

export interface CategoryRow extends Record<string, unknown> {
  id: number;
  slug: string;
  name: string;
  parentId?: number | null;
  sortOrder: number;
  active: boolean;
}

export const categoryConfig: ResourceConfig<CategoryRow> = {
  key: "categories",
  title: "Kateqoriyalar",
  description: "Kurs kateqoriyalarının idarə edilməsi.",
  apiPath: "/api/admin/categories",
  idField: "id",
  roleGroup: "contentManager",
  searchKeys: ["slug", "name"],
  columns: [
    { key: "slug", label: "Slug" },
    { key: "name", label: "Ad" },
    { key: "parentId", label: "Valideyn ID" },
    { key: "sortOrder", label: "Sıra" },
    { key: "active", label: "Aktiv", render: (r) => <BoolBadge value={r.active} /> },
  ],
  fields: [
    { name: "slug", label: "Slug", type: "text", required: true, placeholder: "web-development" },
    { name: "name", label: "Ad", type: "text", required: true },
    { name: "parentId", label: "Valideyn kateqoriya", type: "reference", refKind: "category", placeholder: "Valideyn kateqoriya seçin (istəyə bağlı)" },
    { name: "sortOrder", label: "Sıra", type: "number" },
    { name: "active", label: "Aktiv", type: "boolean" },
  ],
};

export interface InstructorRow extends Record<string, unknown> {
  id: string;
  userId?: string | null;
  fullName: string;
  bio?: string | null;
  photoUrl?: string | null;
  linkedinUrl?: string | null;
  avgRating?: number | null;
  active: boolean;
  createdAt: string;
}

export const instructorConfig: ResourceConfig<InstructorRow> = {
  key: "instructors",
  title: "Müəllimlər",
  description: "Kursları tədris edən müəllimlərin profili.",
  apiPath: "/api/admin/instructors",
  idField: "id",
  roleGroup: "contentManager",
  searchKeys: ["fullName"],
  columns: [
    { key: "fullName", label: "Ad Soyad" },
    { key: "avgRating", label: "Reytinq" },
    { key: "active", label: "Aktiv", render: (r) => <BoolBadge value={r.active} /> },
    { key: "createdAt", label: "Yaradılıb", render: (r) => formatDate(r.createdAt) },
  ],
  fields: [
    { name: "userId", label: "Bağlı istifadəçi hesabı", type: "reference", refKind: "user", placeholder: "İstifadəçi seçin (istəyə bağlı)" },
    { name: "fullName", label: "Ad Soyad", type: "text", required: true },
    { name: "bio", label: "Bio", type: "textarea" },
    { name: "photoUrl", label: "Şəkil URL", type: "text" },
    { name: "linkedinUrl", label: "LinkedIn URL", type: "text" },
    { name: "certifications", label: "Sertifikatlar", type: "stringList", helpText: "Hər sertifikatı yazıb Enter'a basın." },
    { name: "active", label: "Aktiv", type: "boolean" },
  ],
};

export interface CourseGroupRow extends Record<string, unknown> {
  id: string;
  courseId: string;
  groupCode: string;
  startDate: string;
  endDate?: string | null;
  totalSeats: number;
  reservedSeats: number;
  status: string;
  createdAt: string;
}

export const courseGroupConfig: ResourceConfig<CourseGroupRow> = {
  key: "course-groups",
  title: "Kurs Qrupları",
  description: "Konkret başlama tarixi olan kurs axınları.",
  apiPath: "/api/admin/course-groups",
  idField: "id",
  roleGroup: "contentManager",
  searchKeys: ["groupCode"],
  columns: [
    { key: "groupCode", label: "Qrup kodu" },
    { key: "courseId", label: "Kurs", render: (r) => <ReferenceLabel kind="course" value={r.courseId} /> },
    { key: "startDate", label: "Başlama", render: (r) => formatDate(r.startDate) },
    { key: "totalSeats", label: "Yer sayı" },
    { key: "reservedSeats", label: "Dolu yer" },
    { key: "status", label: "Status", render: (r) => <StatusBadge value={r.status} /> },
  ],
  fields: [
    { name: "courseId", label: "Kurs", type: "reference", refKind: "course", required: true, placeholder: "Kurs seçin" },
    { name: "groupCode", label: "Qrup kodu", type: "text", required: true },
    { name: "startDate", label: "Başlama tarixi", type: "date", required: true },
    { name: "endDate", label: "Bitmə tarixi", type: "date" },
    { name: "registrationDeadline", label: "Qeydiyyat sonu", type: "datetime" },
    { name: "totalSeats", label: "Yer sayı", type: "number", required: true },
    {
      name: "status",
      label: "Status",
      type: "select",
      options: [
        { value: "OPEN", label: "Açıq" },
        { value: "CLOSED", label: "Bağlı" },
        { value: "FULL", label: "Dolu" },
        { value: "CANCELLED", label: "Ləğv edilib" },
      ],
    },
    { name: "schedule", label: "Dərs cədvəli (JSON massiv)", type: "jsonArray", advanced: true, helpText: "Texniki sahə — dərslərin cədvəli JSON formatında." },
  ],
};

export interface ScholarshipRow extends Record<string, unknown> {
  id: number;
  name: string;
  discountPct?: number | null;
  maxRecipients?: number | null;
  validFrom?: string | null;
  validUntil?: string | null;
  active: boolean;
}

export const scholarshipConfig: ResourceConfig<ScholarshipRow> = {
  key: "scholarships",
  title: "Təqaüdlər",
  description: "Endirim/təqaüd proqramlarının idarə edilməsi.",
  apiPath: "/api/admin/scholarships",
  idField: "id",
  roleGroup: "adminOnly",
  searchKeys: ["name"],
  columns: [
    { key: "name", label: "Ad" },
    { key: "discountPct", label: "Endirim %" },
    { key: "maxRecipients", label: "Maks. alıcı" },
    { key: "validFrom", label: "Başlama", render: (r) => formatDate(r.validFrom) },
    { key: "validUntil", label: "Bitmə", render: (r) => formatDate(r.validUntil) },
    { key: "active", label: "Aktiv", render: (r) => <BoolBadge value={r.active} /> },
  ],
  fields: [
    { name: "name", label: "Ad", type: "text", required: true },
    { name: "description", label: "Açıqlama", type: "textarea" },
    { name: "discountPct", label: "Endirim (%)", type: "number" },
    { name: "maxRecipients", label: "Maksimum alıcı sayı", type: "number" },
    { name: "validFrom", label: "Başlama tarixi", type: "date" },
    { name: "validUntil", label: "Bitmə tarixi", type: "date" },
    { name: "active", label: "Aktiv", type: "boolean" },
  ],
};

export interface CmsContentRow extends Record<string, unknown> {
  id: number;
  key: string;
  type: string;
  title?: string | null;
  published: boolean;
  sortOrder: number;
  updatedAt: string;
}

export const cmsContentConfig: ResourceConfig<CmsContentRow> = {
  key: "cms-content",
  title: "CMS Məzmun",
  description: "Sayt səhifələri, FAQ, banner və s. üçün məzmun blokları.",
  apiPath: "/api/admin/cms-content",
  idField: "id",
  roleGroup: "contentManager",
  searchKeys: ["key", "title"],
  columns: [
    { key: "key", label: "Açar" },
    { key: "type", label: "Tip", render: (r) => <StatusBadge value={r.type} /> },
    { key: "title", label: "Başlıq" },
    { key: "published", label: "Yayımda", render: (r) => <BoolBadge value={r.published} /> },
    { key: "sortOrder", label: "Sıra" },
    { key: "updatedAt", label: "Yenilənib", render: (r) => formatDateTime(r.updatedAt) },
  ],
  fields: [
    { name: "key", label: "Açar", type: "text", required: true },
    { name: "type", label: "Tip", type: "text", required: true, placeholder: "page / faq / banner" },
    { name: "title", label: "Başlıq", type: "text" },
    { name: "body", label: "Mətn", type: "textarea" },
    { name: "data", label: "Əlavə data", type: "keyValue", helpText: "Açar-dəyər cütləri (məs. buttonText → Daxil ol)." },
    { name: "published", label: "Yayımda", type: "boolean" },
    { name: "sortOrder", label: "Sıra", type: "number" },
  ],
};

export interface CampaignRow extends Record<string, unknown> {
  id: string;
  name: string;
  discountPct?: number | null;
  startsAt: string;
  endsAt: string;
  active: boolean;
  priority: number;
}

export const campaignConfig: ResourceConfig<CampaignRow> = {
  key: "campaigns",
  title: "Kampaniyalar",
  description: "Marketinq kampaniyaları və endirimlər.",
  apiPath: "/api/admin/sales/campaigns",
  idField: "id",
  roleGroup: "salesCrm",
  searchKeys: ["name"],
  columns: [
    { key: "name", label: "Ad" },
    { key: "discountPct", label: "Endirim %" },
    { key: "startsAt", label: "Başlama", render: (r) => formatDateTime(r.startsAt) },
    { key: "endsAt", label: "Bitmə", render: (r) => formatDateTime(r.endsAt) },
    { key: "priority", label: "Prioritet" },
    { key: "active", label: "Aktiv", render: (r) => <BoolBadge value={r.active} /> },
  ],
  fields: [
    { name: "name", label: "Ad", type: "text", required: true },
    { name: "bannerImageUrl", label: "Banner URL", type: "text" },
    { name: "ctaUrl", label: "CTA URL", type: "text" },
    { name: "discountPct", label: "Endirim (%)", type: "number" },
    { name: "startsAt", label: "Başlama", type: "datetime", required: true },
    { name: "endsAt", label: "Bitmə", type: "datetime", required: true },
    { name: "active", label: "Aktiv", type: "boolean" },
    { name: "priority", label: "Prioritet", type: "number" },
    { name: "courseIds", label: "Əlaqəli kurslar", type: "referenceArray", refKind: "course", placeholder: "Kurs seçin" },
  ],
};

export interface ChatSessionRow extends Record<string, unknown> {
  id: string;
  userId?: string | null;
  leadId?: string | null;
  channel?: string | null;
  startedAt: string;
  endedAt?: string | null;
}

export const chatSessionConfig: ResourceConfig<ChatSessionRow> = {
  key: "chat-sessions",
  title: "Çat Sessiyaları",
  description: "Sayt/AI çat söhbətlərinin qeydiyyatı.",
  apiPath: "/api/admin/sales/chat-sessions",
  idField: "id",
  roleGroup: "salesCrm",
  searchKeys: ["channel"],
  columns: [
    { key: "channel", label: "Kanal" },
    { key: "userId", label: "İstifadəçi", render: (r) => <MonoId value={r.userId} /> },
    { key: "leadId", label: "Lead", render: (r) => <MonoId value={r.leadId} /> },
    { key: "startedAt", label: "Başlanıb", render: (r) => formatDateTime(r.startedAt) },
    { key: "endedAt", label: "Bitib", render: (r) => formatDateTime(r.endedAt) },
  ],
  fields: [
    { name: "userId", label: "İstifadəçi", type: "reference", refKind: "user", placeholder: "İstifadəçi seçin (istəyə bağlı)" },
    { name: "leadId", label: "Lead", type: "reference", refKind: "lead", placeholder: "Lead seçin (istəyə bağlı)" },
    { name: "channel", label: "Kanal", type: "text" },
    { name: "messages", label: "Mesajlar (JSON massiv)", type: "jsonArray", advanced: true, helpText: "Texniki sahə — söhbət mesajları JSON formatında." },
  ],
};

export interface ContactSubmissionRow extends Record<string, unknown> {
  id: string;
  fullName?: string | null;
  email?: string | null;
  type: string;
  status: string;
  submittedAt: string;
}

export const contactSubmissionConfig: ResourceConfig<ContactSubmissionRow> = {
  key: "contact-submissions",
  title: "Əlaqə Formaları",
  description: "CRM-daxili əlaqə/müraciət qeydləri.",
  apiPath: "/api/admin/sales/contact-submissions",
  idField: "id",
  roleGroup: "salesCrm",
  searchKeys: ["fullName", "email"],
  columns: [
    { key: "fullName", label: "Ad Soyad" },
    { key: "email", label: "E-poçt" },
    { key: "type", label: "Tip", render: (r) => <StatusBadge value={r.type} /> },
    { key: "status", label: "Status", render: (r) => <StatusBadge value={r.status} /> },
    { key: "submittedAt", label: "Tarix", render: (r) => formatDateTime(r.submittedAt) },
  ],
  fields: [
    { name: "leadId", label: "Lead", type: "reference", refKind: "lead", placeholder: "Lead seçin (istəyə bağlı)" },
    { name: "type", label: "Tip", type: "text", required: true },
    { name: "courseId", label: "Kurs", type: "reference", refKind: "course", placeholder: "Kurs seçin (istəyə bağlı)" },
    { name: "fullName", label: "Ad Soyad", type: "text" },
    { name: "email", label: "E-poçt", type: "text" },
    { name: "phone", label: "Telefon", type: "text" },
    { name: "message", label: "Mesaj", type: "textarea" },
    { name: "preferredTime", label: "Təklif olunan vaxt", type: "datetime" },
  ],
};

export interface LeadRow extends Record<string, unknown> {
  id: string;
  fullName?: string | null;
  email?: string | null;
  source: string;
  status: string;
  createdAt: string;
}

export const leadConfig: ResourceConfig<LeadRow> = {
  key: "leads",
  title: "Lead-lər",
  description: "Potensial tələbə/müştəri əlaqə məlumatları.",
  apiPath: "/api/admin/sales/leads",
  idField: "id",
  roleGroup: "salesCrm",
  searchKeys: ["fullName", "email"],
  columns: [
    { key: "fullName", label: "Ad Soyad" },
    { key: "email", label: "E-poçt" },
    { key: "source", label: "Mənbə", render: (r) => <StatusBadge value={r.source} /> },
    { key: "status", label: "Status", render: (r) => <StatusBadge value={r.status} /> },
    { key: "createdAt", label: "Yaradılıb", render: (r) => formatDateTime(r.createdAt) },
  ],
  fields: [
    { name: "fullName", label: "Ad Soyad", type: "text" },
    { name: "email", label: "E-poçt", type: "text" },
    { name: "phone", label: "Telefon", type: "text" },
    { name: "courseId", label: "Maraqlandığı kurs", type: "reference", refKind: "course", placeholder: "Kurs seçin (istəyə bağlı)" },
    { name: "source", label: "Mənbə", type: "text", required: true },
    { name: "assignedTo", label: "Təhkim olunduğu istifadəçi", type: "reference", refKind: "user", placeholder: "İstifadəçi seçin (istəyə bağlı)" },
    { name: "consentVersion", label: "Razılıq versiyası", type: "text" },
  ],
};

export interface OAuthAccountRow extends Record<string, unknown> {
  id: number;
  userId: string;
  provider: string;
  providerUserId: string;
  linkedAt: string;
}

export const oauthAccountConfig: ResourceConfig<OAuthAccountRow> = {
  key: "oauth-accounts",
  title: "OAuth Hesabları",
  description: "İstifadəçilərin xarici (Google/GitHub/...) hesab bağlantıları.",
  apiPath: "/api/admin/oauth-accounts",
  idField: "id",
  roleGroup: "adminOnly",
  searchKeys: ["providerUserId"],
  columns: [
    { key: "provider", label: "Provayder", render: (r) => <StatusBadge value={r.provider} /> },
    { key: "userId", label: "İstifadəçi", render: (r) => <MonoId value={r.userId} /> },
    { key: "providerUserId", label: "Provayder ID-si" },
    { key: "linkedAt", label: "Bağlanıb", render: (r) => formatDateTime(r.linkedAt) },
  ],
  fields: [
    { name: "userId", label: "İstifadəçi", type: "reference", refKind: "user", required: true, placeholder: "İstifadəçi seçin" },
    {
      name: "provider",
      label: "Provayder",
      type: "select",
      required: true,
      options: [
        { value: "google", label: "Google" },
        { value: "github", label: "GitHub" },
        { value: "facebook", label: "Facebook" },
        { value: "apple", label: "Apple" },
        { value: "microsoft", label: "Microsoft" },
      ],
    },
    { name: "providerUserId", label: "Provayderdəkı ID", type: "text", required: true },
    { name: "accessTokenEnc", label: "Access token (şifrələnmiş)", type: "textarea", advanced: true },
    { name: "refreshTokenEnc", label: "Refresh token (şifrələnmiş)", type: "textarea", advanced: true },
  ],
};

export interface SessionRow extends Record<string, unknown> {
  id: string;
  userId: string;
  type: string;
  ipAddress?: string | null;
  issuedAt: string;
  expiresAt: string;
  revokedAt?: string | null;
}

export const sessionConfig: ResourceConfig<SessionRow> = {
  key: "sessions",
  title: "Sessiyalar",
  description: "Refresh/reset/verify token qeydlərinin əl ilə idarəsi.",
  apiPath: "/api/admin/sessions",
  idField: "id",
  roleGroup: "adminOnly",
  searchKeys: ["ipAddress"],
  columns: [
    { key: "userId", label: "İstifadəçi", render: (r) => <MonoId value={r.userId} /> },
    { key: "type", label: "Tip", render: (r) => <StatusBadge value={r.type} /> },
    { key: "ipAddress", label: "IP" },
    { key: "issuedAt", label: "Verilib", render: (r) => formatDateTime(r.issuedAt) },
  ],
  fields: [
    { name: "userId", label: "İstifadəçi", type: "reference", refKind: "user", required: true, placeholder: "İstifadəçi seçin" },
    { name: "type", label: "Tip", type: "text" },
    { name: "tokenHash", label: "Token hash", type: "text", required: true, helpText: "Texniki dəyər — token-in hash-i.", advanced: true },
    { name: "ipAddress", label: "IP ünvanı", type: "text" },
    { name: "userAgent", label: "User agent", type: "text", advanced: true },
    { name: "expiresAt", label: "Bitmə tarixi", type: "datetime", required: true },
  ],
};

export interface NotificationRow extends Record<string, unknown> {
  id: string;
  userId: string;
  type: string;
  channel: string;
  status: string;
  createdAt: string;
}

export const notificationConfig: ResourceConfig<NotificationRow> = {
  key: "notifications",
  title: "Bildirişlər",
  description: "İstifadəçilərə göndərilən bildirişlərin idarə edilməsi.",
  apiPath: "/api/admin/notifications",
  idField: "id",
  roleGroup: "adminOnly",
  searchKeys: ["type"],
  columns: [
    { key: "userId", label: "İstifadəçi", render: (r) => <MonoId value={r.userId} /> },
    { key: "type", label: "Tip" },
    { key: "channel", label: "Kanal", render: (r) => <StatusBadge value={r.channel} /> },
    { key: "status", label: "Status", render: (r) => <StatusBadge value={r.status} /> },
    { key: "createdAt", label: "Yaradılıb", render: (r) => formatDateTime(r.createdAt) },
  ],
  fields: [
    { name: "userId", label: "İstifadəçi", type: "reference", refKind: "user", required: true, placeholder: "İstifadəçi seçin" },
    { name: "type", label: "Tip", type: "text", required: true, placeholder: "payment_confirmed" },
    {
      name: "channel",
      label: "Kanal",
      type: "select",
      required: true,
      options: [
        { value: "EMAIL", label: "E-poçt" },
        { value: "SMS", label: "SMS" },
        { value: "PUSH", label: "Push bildiriş" },
      ],
    },
    { name: "payload", label: "Əlavə məlumat", type: "keyValue", helpText: "Açar-dəyər cütləri (məs. amount → 100)." },
  ],
};

export interface KbArticleRow extends Record<string, unknown> {
  id: string;
  sourceType: string;
  title?: string | null;
  active: boolean;
  updatedAt: string;
}

export const kbArticleConfig: ResourceConfig<KbArticleRow> = {
  key: "kb-articles",
  title: "Bilgi Bazası",
  description: "AI-chatbot üçün bilgi bazası məqalələri.",
  apiPath: "/api/admin/kb-articles",
  idField: "id",
  roleGroup: "contentManager",
  searchKeys: ["title", "sourceType"],
  columns: [
    { key: "title", label: "Başlıq" },
    { key: "sourceType", label: "Mənbə tipi", render: (r) => <StatusBadge value={r.sourceType} /> },
    { key: "active", label: "Aktiv", render: (r) => <BoolBadge value={r.active} /> },
    { key: "updatedAt", label: "Yenilənib", render: (r) => formatDateTime(r.updatedAt) },
  ],
  fields: [
    { name: "sourceType", label: "Mənbə tipi", type: "text", required: true },
    { name: "sourceRefId", label: "Mənbə reference ID", type: "text" },
    { name: "title", label: "Başlıq", type: "text" },
    { name: "content", label: "Məzmun", type: "textarea", required: true },
    { name: "active", label: "Aktiv", type: "boolean" },
  ],
};

export interface CourseReviewRow extends Record<string, unknown> {
  id: number;
  courseId: string;
  userId: string;
  rating: number;
  published: boolean;
  createdAt: string;
}

export const courseReviewConfig: ResourceConfig<CourseReviewRow> = {
  key: "course-reviews",
  title: "Kurs Rəyləri",
  description: "Tələbə rəyləri - yaradılanda moderasiya gözləyir (published=false).",
  apiPath: "/api/admin/course-reviews",
  idField: "id",
  roleGroup: "contentManager",
  searchKeys: ["comment"],
  columns: [
    { key: "courseId", label: "Kurs", render: (r) => <ReferenceLabel kind="course" value={r.courseId} /> },
    { key: "userId", label: "İstifadəçi", render: (r) => <MonoId value={r.userId} /> },
    { key: "rating", label: "Reytinq" },
    { key: "published", label: "Yayımda", render: (r) => <BoolBadge value={r.published} /> },
    { key: "createdAt", label: "Yaradılıb", render: (r) => formatDateTime(r.createdAt) },
  ],
  fields: [
    { name: "courseId", label: "Kurs", type: "reference", refKind: "course", required: true, placeholder: "Kurs seçin" },
    { name: "userId", label: "İstifadəçi", type: "reference", refKind: "user", required: true, placeholder: "İstifadəçi seçin" },
    { name: "enrollmentId", label: "Qeydiyyat", type: "reference", refKind: "enrollment", placeholder: "Qeydiyyat seçin (istəyə bağlı)" },
    { name: "rating", label: "Reytinq (1-5)", type: "number", required: true },
    { name: "comment", label: "Şərh", type: "textarea" },
  ],
};

export interface GraduateOutcomeRow extends Record<string, unknown> {
  id: number;
  companyName?: string | null;
  jobTitle?: string | null;
  employedAt?: string | null;
  publicStory?: boolean | null;
  createdAt: string;
}

export const graduateOutcomeConfig: ResourceConfig<GraduateOutcomeRow> = {
  key: "graduate-outcomes",
  title: "Məzun Uğur Hekayələri",
  description: "Məzunların iş yerləşmə məlumatları.",
  apiPath: "/api/admin/graduate-outcomes",
  idField: "id",
  roleGroup: "contentManager",
  searchKeys: ["companyName", "jobTitle"],
  columns: [
    { key: "companyName", label: "Şirkət" },
    { key: "jobTitle", label: "Vəzifə" },
    { key: "employedAt", label: "İşə qəbul", render: (r) => formatDate(r.employedAt) },
    { key: "publicStory", label: "Public hekayə", render: (r) => <BoolBadge value={r.publicStory} /> },
    { key: "createdAt", label: "Yaradılıb", render: (r) => formatDateTime(r.createdAt) },
  ],
  fields: [
    { name: "userId", label: "İstifadəçi", type: "reference", refKind: "user", required: true, placeholder: "İstifadəçi seçin" },
    { name: "courseId", label: "Kurs", type: "reference", refKind: "course", required: true, placeholder: "Kurs seçin" },
    { name: "companyName", label: "Şirkət adı", type: "text" },
    { name: "jobTitle", label: "Vəzifə", type: "text" },
    { name: "employedAt", label: "İşə qəbul tarixi", type: "date" },
    { name: "salaryBand", label: "Maaş aralığı", type: "text" },
    { name: "publicStory", label: "Public hekayə kimi göstərsin", type: "boolean" },
    { name: "storyText", label: "Hekayə mətni", type: "textarea" },
  ],
};

export interface AuditLogRow extends Record<string, unknown> {
  id: number;
  actorId?: string | null;
  action: string;
  entityType: string;
  entityId: string;
  createdAt: string;
}

export const auditLogConfig: ResourceConfig<AuditLogRow> = {
  key: "audit-logs",
  title: "Audit Loqları",
  description: "Sistemdə baş verən dəyişikliklərin izlənməsi.",
  apiPath: "/api/admin/audit-logs",
  idField: "id",
  roleGroup: "adminOnly",
  searchKeys: ["action", "entityType", "entityId"],
  columns: [
    { key: "action", label: "Əməliyyat", render: (r) => <StatusBadge value={r.action} /> },
    { key: "entityType", label: "Varlıq tipi" },
    { key: "entityId", label: "Varlıq ID", render: (r) => <MonoId value={r.entityId} /> },
    { key: "actorId", label: "İcra edən", render: (r) => <MonoId value={r.actorId} /> },
    { key: "createdAt", label: "Tarix", render: (r) => formatDateTime(r.createdAt) },
  ],
  fields: [
    { name: "actorId", label: "İcra edən istifadəçi", type: "reference", refKind: "user", placeholder: "İstifadəçi seçin (istəyə bağlı)" },
    { name: "action", label: "Əməliyyat", type: "text", required: true, placeholder: "user.updated" },
    { name: "entityType", label: "Varlıq tipi", type: "text", required: true },
    { name: "entityId", label: "Varlıq ID", type: "text", required: true, helpText: "Dəyişdirilən qeydin ID-si." },
    { name: "beforeState", label: "Əvvəlki vəziyyət", type: "keyValue", advanced: true },
    { name: "afterState", label: "Sonrakı vəziyyət", type: "keyValue", advanced: true },
    { name: "ipAddress", label: "IP ünvanı", type: "text" },
  ],
};

export const allResourceConfigs = [
  categoryConfig,
  instructorConfig,
  courseGroupConfig,
  scholarshipConfig,
  cmsContentConfig,
  campaignConfig,
  chatSessionConfig,
  contactSubmissionConfig,
  leadConfig,
  oauthAccountConfig,
  sessionConfig,
  notificationConfig,
  kbArticleConfig,
  courseReviewConfig,
  graduateOutcomeConfig,
  auditLogConfig,
];
