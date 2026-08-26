import { NavLink } from "react-router-dom";
import { cn } from "@/lib/utils";
import { NAV_GROUPS } from "@/layout/nav-config";

export function SidebarNav({ onNavigate }: { onNavigate?: () => void }) {
  return (
    <nav className="flex flex-col gap-6 px-3 py-5" aria-label="Əsas naviqasiya">
      {NAV_GROUPS.map((group) => {
        return (
          <div key={group.label}>
            <p className="mb-2 px-3 text-[0.64rem] font-semibold tracking-[0.16em] text-muted-foreground/80 uppercase">
              {group.label}
            </p>
            <div className="flex flex-col gap-0.5">
              {group.items.map((item) => (
                <NavLink
                  key={item.to}
                  to={item.to}
                  onClick={onNavigate}
                  className={({ isActive }) =>
                    cn(
                      "group relative flex items-center gap-3 rounded-xl px-3.5 py-3 text-sm font-medium transition-all",
                      isActive
                        ? "bg-[#1473e6] text-white shadow-md shadow-blue-500/20"
                        : "text-muted-foreground hover:bg-sidebar-accent hover:text-sidebar-accent-foreground",
                    )
                  }
                >
                  <item.icon className="size-4 shrink-0 transition-transform group-hover:scale-105" />
                  <span className="truncate">{item.label}</span>
                </NavLink>
              ))}
            </div>
          </div>
        );
      })}
    </nav>
  );
}
