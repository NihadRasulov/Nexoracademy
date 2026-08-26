import type { ResourceConfig } from "@/resources/types";
import { BoolBadge, formatDate } from "@/lib/format";

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
  description: "Kursların saytda qruplaşdırıldığı kateqoriyalar.",
  apiPath: "/api/v1/categories",
  idField: "id",
  roleGroup: "admin",
  searchKeys: ["slug", "name"],
  columns: [
    { key: "name", label: "Ad" },
    { key: "slug", label: "Slug" },
    { key: "sortOrder", label: "Sıra" },
    { key: "active", label: "Aktiv", render: (row) => <BoolBadge value={row.active} /> },
  ],
  fields: [
    { name: "name", label: "Ad", type: "text", required: true },
    { name: "slug", label: "Slug", type: "text", required: true, placeholder: "proqramlasdirma" },
    { name: "parentId", label: "Ana kateqoriya", type: "reference", refKind: "category", placeholder: "İstəyə bağlı" },
    { name: "sortOrder", label: "Sıra", type: "number" },
    { name: "active", label: "Aktiv", type: "boolean" },
  ],
};

export interface InstructorRow extends Record<string, unknown> {
  id: string;
  fullName: string;
  bio?: string | null;
  photoUrl?: string | null;
  linkedinUrl?: string | null;
  active: boolean;
  createdAt: string;
}

export const instructorConfig: ResourceConfig<InstructorRow> = {
  key: "instructors",
  title: "Təlimçilər",
  description: "Kurs kartlarında və detallarında görünən təlimçi profilləri.",
  apiPath: "/api/v1/instructors",
  idField: "id",
  roleGroup: "admin",
  searchKeys: ["fullName"],
  columns: [
    { key: "fullName", label: "Ad soyad" },
    { key: "linkedinUrl", label: "LinkedIn" },
    { key: "active", label: "Aktiv", render: (row) => <BoolBadge value={row.active} /> },
    { key: "createdAt", label: "Yaradılıb", render: (row) => formatDate(row.createdAt) },
  ],
  fields: [
    { name: "fullName", label: "Ad soyad", type: "text", required: true },
    { name: "bio", label: "Qısa bio", type: "textarea" },
    { name: "photoUrl", label: "Profil şəkli", type: "image" },
    { name: "linkedinUrl", label: "LinkedIn URL", type: "text" },
    { name: "active", label: "Aktiv", type: "boolean" },
  ],
};

export const allResourceConfigs = [categoryConfig, instructorConfig];
