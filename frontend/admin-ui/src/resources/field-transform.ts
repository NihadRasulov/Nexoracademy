import type { FieldConfig } from "@/resources/types";
import { isNumericReference } from "@/resources/reference-registry";

export function toFormValue(field: FieldConfig, value: unknown): string | boolean {
  if (value === null || value === undefined) {
    return field.type === "boolean" ? false : "";
  }

  switch (field.type) {
    case "boolean":
      return Boolean(value);
    case "datetime":
      return typeof value === "string" ? value.slice(0, 16) : "";
    case "keyValue":
    case "json":
    case "jsonArray":
      return JSON.stringify(value, null, 2);
    case "stringList":
      return Array.isArray(value) ? JSON.stringify(value.map((v) => String(v))) : "";
    case "referenceArray":
    case "guidArray":
      return Array.isArray(value) ? value.join(", ") : "";
    default:
      return String(value);
  }
}

export function toApiValue(field: FieldConfig, raw: string | boolean): unknown {
  if (field.type === "boolean") {
    return raw as boolean;
  }

  const text = (raw as string).trim();

  switch (field.type) {
    case "number":
      return text === "" ? undefined : Number(text);
    case "datetime":
      return text === "" ? undefined : new Date(text).toISOString();
    case "date":
      return text === "" ? undefined : text;
    case "reference":
      if (text === "") return undefined;
      return field.refKind && isNumericReference(field.refKind) ? Number(text) : text;
    case "keyValue":
    case "json":
    case "jsonArray":
      return text === "" ? undefined : JSON.parse(text);
    case "stringList":
      return text === "" ? undefined : JSON.parse(text);
    case "referenceArray":
    case "guidArray":
      return text === ""
        ? undefined
        : text
            .split(",")
            .map((s) => s.trim())
            .filter(Boolean);
    default:
      return text === "" ? undefined : text;
  }
}
