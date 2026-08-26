import { Link } from "react-router-dom";
import { BrandLogo } from "@/components/brand-logo";

export function BrandMark() {
  return (
    <Link
      to="/dashboard"
      aria-label="Nexora admin panel — ana səhifə"
      className="group block border-b border-sidebar-border/70 px-4 py-5"
    >
      <div className="flex items-center gap-3">
        <div className="relative flex size-10 shrink-0 items-center justify-center overflow-hidden rounded-xl bg-white p-1.5 shadow-sm ring-1 ring-sky-300/20">
          <BrandLogo className="max-h-8" />
        </div>
        <div className="min-w-0">
          <span className="block truncate text-[0.95rem] font-bold tracking-tight text-sidebar-foreground">Nexora Academy</span>
          <span className="mt-0.5 block text-[0.6rem] font-semibold tracking-[0.16em] text-primary uppercase">Content manager</span>
        </div>
      </div>
    </Link>
  );
}
