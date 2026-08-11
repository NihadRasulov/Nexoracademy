import { Link } from "react-router-dom";
import { BrandLogo } from "@/components/brand-logo";

export function BrandMark() {
  return (
    <Link
      to="/dashboard"
      aria-label="Nexora admin panel — ana səhifə"
      className="block border-b border-sidebar-border px-4 py-4"
    >
      <BrandLogo className="max-h-11 dark:brightness-110" />
      <span className="mt-2 block text-center text-[0.65rem] font-semibold tracking-[0.18em] text-muted-foreground uppercase">
        Admin Panel
      </span>
    </Link>
  );
}
