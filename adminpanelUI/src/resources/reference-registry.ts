import type { PagedResult } from "@/types/common";

/**
 * Central registry of "reference" entities the admin can pick from a dropdown
 * instead of pasting raw GUIDs/IDs. Each entry knows how to load its list from
 * the backend and how to turn a record into a human-friendly label.
 */
export type ReferenceKind =
  | "course"
  | "user"
  | "category"
  | "courseGroup"
  | "instructor"
  | "enrollment"
  | "lead";

export interface ReferenceItem {
  id: string;
  label: string;
  sublabel?: string;
}

interface ReferenceDef {
  /** Backend list endpoint. */
  apiPath: string;
  /** True when the endpoint returns a `PagedResult`, false when a plain array. */
  paged: boolean;
  /** Query param the endpoint accepts for server-side search (if any). */
  searchParam?: string;
  /** IDs are numeric (so they must be sent back to the API as numbers). */
  numericId?: boolean;
  /** Build the primary label shown in the dropdown. */
  label: (item: Record<string, unknown>) => string;
  /** Optional secondary line (e.g. email, slug). */
  sublabel?: (item: Record<string, unknown>) => string | undefined;
}

const str = (v: unknown): string => (v == null ? "" : String(v));

export const REFERENCES: Record<ReferenceKind, ReferenceDef> = {
  course: {
    apiPath: "/api/courses",
    paged: true,
    searchParam: "q",
    label: (c) => str(c.title) || str(c.slug) || str(c.id),
    sublabel: (c) => str(c.slug) || undefined,
  },
  user: {
    apiPath: "/api/users",
    paged: true,
    searchParam: "q",
    label: (u) => str(u.fullName) || str(u.email) || str(u.id),
    sublabel: (u) => str(u.email) || undefined,
  },
  category: {
    apiPath: "/api/categories",
    paged: false,
    numericId: true,
    label: (c) => str(c.name) || str(c.slug) || str(c.id),
    sublabel: (c) => str(c.slug) || undefined,
  },
  courseGroup: {
    apiPath: "/api/course-groups",
    paged: false,
    label: (g) => str(g.groupCode) || str(g.id),
    sublabel: (g) => (g.status ? str(g.status) : undefined),
  },
  instructor: {
    apiPath: "/api/instructors",
    paged: true,
    label: (i) => str(i.fullName) || str(i.id),
  },
  enrollment: {
    apiPath: "/api/enrollments",
    paged: false,
    label: (e) => `${str(e.id).slice(0, 8)}… · ${str(e.status)}`,
    sublabel: (e) => (e.userId ? `user ${str(e.userId).slice(0, 8)}…` : undefined),
  },
  lead: {
    apiPath: "/api/sales/leads",
    paged: false,
    label: (l) => str(l.fullName) || str(l.email) || str(l.id),
    sublabel: (l) => str(l.email) || undefined,
  },
};

export function isNumericReference(kind: ReferenceKind): boolean {
  return REFERENCES[kind].numericId === true;
}

/** Normalize either a paged result or a bare array into `ReferenceItem`s. */
export function toReferenceItems(
  kind: ReferenceKind,
  data: PagedResult<Record<string, unknown>> | Record<string, unknown>[] | undefined,
): ReferenceItem[] {
  if (!data) return [];
  const def = REFERENCES[kind];
  const rows = Array.isArray(data) ? data : data.items;
  return (rows ?? []).map((row) => ({
    id: str(row.id),
    label: def.label(row),
    sublabel: def.sublabel?.(row),
  }));
}
