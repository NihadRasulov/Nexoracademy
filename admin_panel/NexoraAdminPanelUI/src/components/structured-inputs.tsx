import { useState } from "react";
import { Plus, X } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";

/** Parse a JSON-string value into a string array, tolerating empty/garbage. */
function parseStringArray(value: string): string[] {
  if (!value.trim()) return [];
  try {
    const parsed = JSON.parse(value);
    if (Array.isArray(parsed)) return parsed.map((v) => String(v));
  } catch {
    /* fall through */
  }
  return [];
}

export function StringListInput({
  id,
  value,
  onChange,
  placeholder,
}: {
  id?: string;
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
}) {
  const tags = parseStringArray(value);
  const [draft, setDraft] = useState("");

  function commit(next: string[]) {
    onChange(next.length ? JSON.stringify(next) : "");
  }

  function addTag() {
    const t = draft.trim();
    if (!t) return;
    if (!tags.includes(t)) commit([...tags, t]);
    setDraft("");
  }

  return (
    <div className="space-y-2">
      {tags.length > 0 && (
        <div className="flex flex-wrap gap-1.5">
          {tags.map((tag) => (
            <Badge key={tag} variant="secondary" className="gap-1 pr-1">
              <span className="truncate max-w-48">{tag}</span>
              <button
                type="button"
                onClick={() => commit(tags.filter((x) => x !== tag))}
                className="rounded-sm hover:bg-foreground/10"
                aria-label="Sil"
              >
                <X className="size-3" />
              </button>
            </Badge>
          ))}
        </div>
      )}
      <div className="flex gap-2">
        <Input
          id={id}
          value={draft}
          placeholder={placeholder ?? "Yazın və Enter'a basın..."}
          onChange={(e: React.ChangeEvent<HTMLInputElement>) => setDraft(e.target.value)}
          onKeyDown={(e: React.KeyboardEvent<HTMLInputElement>) => {
            if (e.key === "Enter" || e.key === ",") {
              e.preventDefault();
              addTag();
            }
          }}
        />
        <Button type="button" variant="outline" size="icon" onClick={addTag} aria-label="Əlavə et">
          <Plus className="size-4" />
        </Button>
      </div>
    </div>
  );
}

interface Row {
  k: string;
  v: string;
}

/** Parse a JSON-string object into key/value rows. */
function parseRows(value: string): Row[] {
  if (!value.trim()) return [];
  try {
    const parsed = JSON.parse(value);
    if (parsed && typeof parsed === "object" && !Array.isArray(parsed)) {
      return Object.entries(parsed).map(([k, v]) => ({
        k,
        v: typeof v === "string" ? v : JSON.stringify(v),
      }));
    }
  } catch {
    /* fall through */
  }
  return [];
}

export function KeyValueEditor({
  value,
  onChange,
}: {
  value: string;
  onChange: (value: string) => void;
}) {
  const rows = parseRows(value);

  function commit(next: Row[]) {
    const obj: Record<string, string> = {};
    for (const row of next) {
      if (row.k.trim()) obj[row.k.trim()] = row.v;
    }
    onChange(Object.keys(obj).length ? JSON.stringify(obj) : "");
  }

  function update(index: number, patch: Partial<Row>) {
    commit(rows.map((r, i) => (i === index ? { ...r, ...patch } : r)));
  }

  return (
    <div className="space-y-2 rounded-lg border border-dashed border-border p-2.5">
      {rows.length === 0 && (
        <p className="px-1 text-xs text-muted-foreground">Sahə yoxdur. Aşağıdan əlavə edin.</p>
      )}
      {rows.map((row, i) => (
        <div key={i} className="flex gap-2">
          <Input
            value={row.k}
            placeholder="açar"
            className="h-8 flex-1"
            onChange={(e: React.ChangeEvent<HTMLInputElement>) => update(i, { k: e.target.value })}
          />
          <Input
            value={row.v}
            placeholder="dəyər"
            className="h-8 flex-[2]"
            onChange={(e: React.ChangeEvent<HTMLInputElement>) => update(i, { v: e.target.value })}
          />
          <Button
            type="button"
            variant="ghost"
            size="icon"
            className="size-8 shrink-0"
            onClick={() => commit(rows.filter((_, idx) => idx !== i))}
            aria-label="Sil"
          >
            <X className="size-4" />
          </Button>
        </div>
      ))}
      <Button
        type="button"
        variant="outline"
        size="sm"
        className="w-full"
        onClick={() => commit([...rows, { k: "", v: "" }])}
      >
        <Plus className="size-4" />
        Sahə əlavə et
      </Button>
    </div>
  );
}
