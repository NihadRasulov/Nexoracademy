import { useEffect, useMemo, useRef, useState } from "react";
import { ArrowUpRight, Search, UserRound } from "lucide-react";
import { useLocation, useNavigate } from "react-router-dom";
import { Input } from "@/components/ui/input";
import { NAV_GROUPS, type NavItem } from "@/layout/nav-config";

const SEARCH_ITEMS: NavItem[] = [
  ...NAV_GROUPS.flatMap((group) => group.items),
  {
    label: "Profilim",
    to: "/profile",
    icon: UserRound,
    description: "Admin profili və parol",
    keywords: ["profil", "parol", "password", "hesab"],
  },
];

function normalize(value: string) {
  return value
    .toLocaleLowerCase("az")
    .replaceAll("ə", "e")
    .replaceAll("ı", "i")
    .replaceAll("ö", "o")
    .replaceAll("ü", "u")
    .replaceAll("ç", "c")
    .replaceAll("ş", "s")
    .replaceAll("ğ", "g")
    .trim();
}

export function AdminSearch() {
  const navigate = useNavigate();
  const location = useLocation();
  const inputRef = useRef<HTMLInputElement>(null);
  const [query, setQuery] = useState("");
  const [open, setOpen] = useState(false);
  const [activeIndex, setActiveIndex] = useState(0);

  const results = useMemo(() => {
    const needle = normalize(query);
    if (!needle) return SEARCH_ITEMS;
    return SEARCH_ITEMS.filter((item) => normalize([
      item.label,
      item.description,
      ...(item.keywords ?? []),
    ].filter(Boolean).join(" ")).includes(needle));
  }, [query]);

  useEffect(() => {
    const onShortcut = (event: KeyboardEvent) => {
      const target = event.target as HTMLElement | null;
      const isTyping = target?.matches("input, textarea, select, [contenteditable='true']");
      if ((event.key === "/" && !isTyping) || ((event.metaKey || event.ctrlKey) && event.key.toLowerCase() === "k")) {
        event.preventDefault();
        inputRef.current?.focus();
        setOpen(true);
      }
    };
    window.addEventListener("keydown", onShortcut);
    return () => window.removeEventListener("keydown", onShortcut);
  }, []);

  useEffect(() => {
    setOpen(false);
    setQuery("");
    setActiveIndex(0);
  }, [location.pathname]);

  useEffect(() => {
    setActiveIndex((current) => Math.min(current, Math.max(results.length - 1, 0)));
  }, [results.length]);

  function goTo(item: NavItem | undefined) {
    if (!item) return;
    navigate(item.to);
    setOpen(false);
    setQuery("");
  }

  return (
    <div className="relative hidden flex-1 lg:block lg:max-w-md">
      <Search className="pointer-events-none absolute top-2.5 left-3 z-10 size-4 text-muted-foreground" />
      <Input
        ref={inputRef}
        role="combobox"
        aria-label="Paneldə axtar"
        aria-expanded={open}
        aria-controls="admin-search-results"
        aria-autocomplete="list"
        className="rounded-xl border-0 bg-muted/75 pr-16 pl-9 shadow-none"
        placeholder="Səhifə və əməliyyat axtar..."
        value={query}
        onFocus={() => setOpen(true)}
        onBlur={() => window.setTimeout(() => setOpen(false), 120)}
        onChange={(event) => {
          setQuery(event.target.value);
          setOpen(true);
          setActiveIndex(0);
        }}
        onKeyDown={(event) => {
          if (event.key === "ArrowDown") {
            event.preventDefault();
            setActiveIndex((current) => Math.min(current + 1, results.length - 1));
          } else if (event.key === "ArrowUp") {
            event.preventDefault();
            setActiveIndex((current) => Math.max(current - 1, 0));
          } else if (event.key === "Enter") {
            event.preventDefault();
            goTo(results[activeIndex]);
          } else if (event.key === "Escape") {
            setOpen(false);
            inputRef.current?.blur();
          }
        }}
      />
      <kbd className="pointer-events-none absolute top-2 right-2 hidden rounded-md border bg-background/80 px-1.5 py-0.5 text-[10px] text-muted-foreground xl:block">⌘ K</kbd>

      {open && (
        <div id="admin-search-results" role="listbox" className="absolute top-[calc(100%+0.6rem)] right-0 left-0 z-50 overflow-hidden rounded-2xl border bg-popover p-1.5 text-popover-foreground shadow-xl shadow-black/10">
          {results.length === 0 ? (
            <p className="px-4 py-6 text-center text-sm text-muted-foreground">Uyğun bölmə tapılmadı.</p>
          ) : results.slice(0, 7).map((item, index) => {
            const Icon = item.icon;
            const active = index === activeIndex;
            return (
              <button
                key={item.to}
                type="button"
                role="option"
                aria-selected={active}
                className={`flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-left transition ${active ? "bg-accent text-accent-foreground" : "hover:bg-muted/70"}`}
                onMouseDown={(event) => event.preventDefault()}
                onMouseEnter={() => setActiveIndex(index)}
                onClick={() => goTo(item)}
              >
                <span className="flex size-9 shrink-0 items-center justify-center rounded-lg bg-primary/10 text-primary"><Icon className="size-4" /></span>
                <span className="min-w-0 flex-1">
                  <span className="block text-sm font-semibold">{item.label}</span>
                  <span className="block truncate text-xs text-muted-foreground">{item.description}</span>
                </span>
                <ArrowUpRight className="size-3.5 text-muted-foreground" />
              </button>
            );
          })}
        </div>
      )}
    </div>
  );
}
