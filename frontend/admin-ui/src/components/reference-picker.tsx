import { useMemo, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { Check, ChevronsUpDown, Loader2, Search, X } from "lucide-react";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { cn } from "@/lib/utils";
import { api } from "@/lib/api";
import {
  REFERENCES,
  toReferenceItems,
  type ReferenceItem,
  type ReferenceKind,
} from "@/resources/reference-registry";
import type { PagedResult } from "@/types/common";

function useReferenceItems(kind: ReferenceKind) {
  const def = REFERENCES[kind];
  return useQuery({
    queryKey: ["reference", kind],
    staleTime: 60_000,
    retry: false,
    queryFn: async () => {
      const data = await api.get<PagedResult<Record<string, unknown>> | Record<string, unknown>[]>(
        def.apiPath,
        def.paged ? { size: 100, sort: "createdAt,desc" } : undefined,
      );
      return toReferenceItems(kind, data);
    },
  });
}

/** Read-only display: resolves an id to its human label, falling back to a short id. */
export function ReferenceLabel({ kind, value }: { kind: ReferenceKind; value: unknown }) {
  const { data: items = [] } = useReferenceItems(kind);
  const id = value == null ? "" : String(value);
  if (!id) return <span className="text-muted-foreground">-</span>;
  const match = items.find((i) => i.id === id);
  if (match) return <span>{match.label}</span>;
  return <span className="font-mono text-xs text-muted-foreground">{id.slice(0, 8)}…</span>;
}

function filterItems(items: ReferenceItem[], search: string): ReferenceItem[] {
  const q = search.trim().toLowerCase();
  if (!q) return items;
  return items.filter(
    (i) =>
      i.label.toLowerCase().includes(q) ||
      i.sublabel?.toLowerCase().includes(q) ||
      i.id.toLowerCase().includes(q),
  );
}

function SearchList({
  items,
  loading,
  selectedIds,
  onPick,
}: {
  items: ReferenceItem[];
  loading: boolean;
  selectedIds: Set<string>;
  onPick: (item: ReferenceItem) => void;
}) {
  const [search, setSearch] = useState("");
  const filtered = useMemo(() => filterItems(items, search), [items, search]);

  return (
    <div className="flex flex-col gap-2">
      <div className="relative">
        <Search className="pointer-events-none absolute left-2.5 top-2.5 size-4 text-muted-foreground" />
        <Input
          autoFocus
          placeholder="Axtar..."
          className="h-9 pl-8"
          value={search}
          onChange={(e: React.ChangeEvent<HTMLInputElement>) => setSearch(e.target.value)}
        />
      </div>
      <div className="max-h-64 overflow-y-auto">
        {loading ? (
          <div className="flex items-center justify-center gap-2 py-6 text-sm text-muted-foreground">
            <Loader2 className="size-4 animate-spin" /> Yüklənir...
          </div>
        ) : filtered.length === 0 ? (
          <p className="py-6 text-center text-sm text-muted-foreground">Nəticə tapılmadı.</p>
        ) : (
          <div className="flex flex-col gap-0.5">
            {filtered.map((item) => {
              const selected = selectedIds.has(item.id);
              return (
                <button
                  key={item.id}
                  type="button"
                  onClick={() => onPick(item)}
                  className={cn(
                    "flex items-center gap-2 rounded-md px-2 py-1.5 text-left text-sm transition-colors hover:bg-accent hover:text-accent-foreground",
                    selected && "bg-accent/60",
                  )}
                >
                  <Check className={cn("size-4 shrink-0", selected ? "opacity-100 text-primary" : "opacity-0")} />
                  <span className="flex min-w-0 flex-col">
                    <span className="truncate font-medium">{item.label}</span>
                    {item.sublabel && (
                      <span className="truncate text-xs text-muted-foreground">{item.sublabel}</span>
                    )}
                  </span>
                </button>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}

export function ReferencePicker({
  id,
  kind,
  value,
  onChange,
  placeholder,
}: {
  id?: string;
  kind: ReferenceKind;
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
}) {
  const [open, setOpen] = useState(false);
  const { data: items = [], isLoading, isError } = useReferenceItems(kind);
  const selected = items.find((i) => i.id === value);
  const selectedIds = useMemo(() => new Set(value ? [value] : []), [value]);

  // If the list can't be loaded (e.g. this role lacks access), degrade to manual entry.
  if (isError) {
    return (
      <Input
        id={id}
        value={value}
        placeholder={placeholder ?? "ID daxil edin"}
        onChange={(e: React.ChangeEvent<HTMLInputElement>) => onChange(e.target.value)}
      />
    );
  }

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger
        render={
          <button
            id={id}
            type="button"
            className="flex h-9 w-full items-center justify-between gap-2 rounded-lg border border-input bg-transparent px-3 text-sm outline-none transition-colors focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50 dark:bg-input/30"
          />
        }
      >
        <span className={cn("truncate", !selected && "text-muted-foreground")}>
          {selected ? selected.label : (placeholder ?? "Seçin...")}
        </span>
        <span className="flex items-center gap-1">
          {selected && (
            <X
              className="size-4 shrink-0 text-muted-foreground hover:text-foreground"
              onClick={(e) => {
                e.stopPropagation();
                onChange("");
              }}
            />
          )}
          <ChevronsUpDown className="size-4 shrink-0 text-muted-foreground" />
        </span>
      </PopoverTrigger>
      <PopoverContent align="start" className="w-(--anchor-width) min-w-72 gap-0 p-2">
        <SearchList
          items={items}
          loading={isLoading}
          selectedIds={selectedIds}
          onPick={(item) => {
            onChange(item.id);
            setOpen(false);
          }}
        />
      </PopoverContent>
    </Popover>
  );
}

export function MultiReferencePicker({
  id,
  kind,
  value,
  onChange,
  placeholder,
}: {
  id?: string;
  kind: ReferenceKind;
  /** Comma-joined list of ids. */
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
}) {
  const [open, setOpen] = useState(false);
  const { data: items = [], isLoading } = useReferenceItems(kind);

  const ids = useMemo(
    () =>
      value
        .split(",")
        .map((s) => s.trim())
        .filter(Boolean),
    [value],
  );
  const selectedIds = useMemo(() => new Set(ids), [ids]);
  const selectedItems = ids.map((idv) => items.find((i) => i.id === idv) ?? { id: idv, label: idv });

  function toggle(itemId: string) {
    const next = selectedIds.has(itemId) ? ids.filter((i) => i !== itemId) : [...ids, itemId];
    onChange(next.join(", "));
  }

  return (
    <div className="space-y-2">
      {selectedItems.length > 0 && (
        <div className="flex flex-wrap gap-1.5">
          {selectedItems.map((item) => (
            <Badge key={item.id} variant="secondary" className="gap-1 pr-1">
              <span className="truncate max-w-40">{item.label}</span>
              <button
                type="button"
                onClick={() => toggle(item.id)}
                className="rounded-sm hover:bg-foreground/10"
                aria-label="Sil"
              >
                <X className="size-3" />
              </button>
            </Badge>
          ))}
        </div>
      )}
      <Popover open={open} onOpenChange={setOpen}>
        <PopoverTrigger
          render={
            <button
              id={id}
              type="button"
              className="flex h-9 w-full items-center justify-between gap-2 rounded-lg border border-input bg-transparent px-3 text-sm text-muted-foreground outline-none transition-colors focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50 dark:bg-input/30"
            />
          }
        >
          <span className="truncate">
            {selectedItems.length > 0 ? "Daha əlavə et..." : (placeholder ?? "Seçin...")}
          </span>
          <ChevronsUpDown className="size-4 shrink-0" />
        </PopoverTrigger>
        <PopoverContent align="start" className="w-(--anchor-width) min-w-72 gap-0 p-2">
          <SearchList items={items} loading={isLoading} selectedIds={selectedIds} onPick={(item) => toggle(item.id)} />
        </PopoverContent>
      </Popover>
    </div>
  );
}
