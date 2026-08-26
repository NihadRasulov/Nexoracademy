import type { RoleGroup } from "@/auth/roles";
import type { ReferenceKind } from "@/resources/reference-registry";

export type FieldType =
  | "text"
  | "textarea"
  | "number"
  | "boolean"
  | "date"
  | "datetime"
  | "select"
  | "reference"
  | "referenceArray"
  | "stringList"
  | "keyValue"
  | "json"
  | "jsonArray"
  | "guidArray"
  /** File upload; stores the returned public URL string (see ImageUploadField). */
  | "image";

export interface FieldOption {
  value: string;
  label: string;
}

export interface FieldConfig {
  name: string;
  label: string;
  type: FieldType;
  options?: FieldOption[];
  /** For `reference` / `referenceArray`: which backend entity to pick from. */
  refKind?: ReferenceKind;
  placeholder?: string;
  helpText?: string;
  required?: boolean;
  /** Collapse the field under an "Ətraflı / Advanced" section. */
  advanced?: boolean;
  /** Auto-fill on create and hide from the UI (e.g. idempotency keys). */
  autoGenerate?: "uuid";
}

export interface ColumnConfig<T = Record<string, unknown>> {
  key: string;
  label: string;
  render?: (row: T) => React.ReactNode;
  className?: string;
}

export interface ResourceConfig<T extends Record<string, unknown> = Record<string, unknown>> {
  key: string;
  title: string;
  description: string;
  apiPath: string;
  idField: keyof T & string;
  roleGroup: RoleGroup;
  columns: ColumnConfig<T>[];
  /** Form fields for create/edit. Omit for read-only resources. */
  fields?: FieldConfig[];
  searchKeys?: (keyof T & string)[];
  /**
   * When true the page renders list + search only: create/edit/delete actions
   * are hidden (server-side writes are rejected anyway).
   */
  readOnly?: boolean;
}
