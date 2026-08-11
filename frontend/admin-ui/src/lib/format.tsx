import { Badge } from "@/components/ui/badge";

export function formatDateTime(value: unknown): string {
  if (!value || typeof value !== "string") return "-";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "-";
  return date.toLocaleString("az-AZ", { dateStyle: "medium", timeStyle: "short" });
}

export function formatDate(value: unknown): string {
  if (!value || typeof value !== "string") return "-";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "-";
  return date.toLocaleDateString("az-AZ", { dateStyle: "medium" });
}

export function BoolBadge({ value }: { value: unknown }) {
  const on = Boolean(value);
  return (
    <Badge variant={on ? "default" : "secondary"}>{on ? "Bəli" : "Xeyr"}</Badge>
  );
}

export function StatusBadge({ value }: { value: unknown }) {
  const text = String(value ?? "-");
  return <Badge variant="outline">{text}</Badge>;
}

export function MonoId({ value }: { value: unknown }) {
  const text = String(value ?? "");
  return <span className="font-mono text-xs text-muted-foreground">{text.slice(0, 8)}</span>;
}
