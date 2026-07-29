import { NavLink } from "react-router-dom";
import { cn } from "@/lib/utils";
import { useAuth } from "@/auth/auth-context";
import { hasRole } from "@/auth/roles";
import { NAV_GROUPS } from "@/layout/nav-config";

export function SidebarNav({ onNavigate }: { onNavigate?: () => void }) {
  const { user } = useAuth();

  return (
    <nav className="flex flex-col gap-6 px-3 py-2">
      {NAV_GROUPS.map((group) => {
        const visibleItems = group.items.filter((item) => hasRole(user?.role, item.roleGroup));
        if (visibleItems.length === 0) return null;

        return (
          <div key={group.label}>
            <p className="mb-1.5 px-3 text-xs font-medium tracking-wide text-muted-foreground uppercase">
              {group.label}
            </p>
            <div className="flex flex-col gap-0.5">
              {visibleItems.map((item) => (
                <NavLink
                  key={item.to}
                  to={item.to}
                  onClick={onNavigate}
                  className={({ isActive }) =>
                    cn(
                      "flex items-center gap-2.5 rounded-md px-3 py-2 text-sm font-medium transition-colors",
                      isActive
                        ? "bg-primary text-primary-foreground"
                        : "text-muted-foreground hover:bg-accent hover:text-accent-foreground",
                    )
                  }
                >
                  <item.icon className="size-4 shrink-0" />
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
