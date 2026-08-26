import type { PagedResult } from "@/types/common";

/**
 * Central registry of "reference" entities the admin can pick from a dropdown
 * instead of pasting raw GUIDs/IDs. Each entry knows how to load its list from
 * the backend and how to turn a record into a human-friendly label.
 */
export type ReferenceKind =
  | "course"
  | "category"
  | "instructor";

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
    apiPath: "/api/v1/courses",
    paged: true,
    searchParam: "q",
    label: (c) => str(c.title) || str(c.slug) || str(c.id),
    sublabel: (c) => str(c.slug) || undefined,
  },
  category: {
    apiPath: "/api/v1/categories",
    paged: false,
    numericId: true,
    label: (c) => str(c.name) || str(c.slug) || str(c.id),
    sublabel: (c) => str(c.slug) || undefined,
  },
  instructor: {
    apiPath: "/api/v1/instructors",
    paged: false,
    label: (i) => str(i.fullName) || str(i.id),
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
  const rows = Array.isArray(data) ? data : data.content;
  return (rows ?? []).map((row) => ({
    id: str(row.id),
    label: def.label(row),
    sublabel: def.sublabel?.(row),
  }));
}
