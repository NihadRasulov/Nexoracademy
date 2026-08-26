import { useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { ExternalLink, Menu, LogOut, UserRound } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Sheet, SheetContent, SheetHeader, SheetTitle, SheetTrigger } from "@/components/ui/sheet";
import { ThemeToggle } from "@/components/theme-toggle";
import { useAuth } from "@/auth/auth-context";
import { ROLE_LABELS } from "@/auth/roles";
import { SidebarNav } from "@/layout/sidebar-nav";
import { BrandMark } from "@/layout/brand-mark";
import { NAV_GROUPS } from "@/layout/nav-config";
import { AdminSearch } from "@/layout/admin-search";
import { toast } from "sonner";

function initialsOf(name: string) {
  return name
    .split(" ")
    .filter(Boolean)
    .slice(0, 2)
    .map((p) => p[0]?.toUpperCase())
    .join("");
}

export function AppTopbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [navOpen, setNavOpen] = useState(false);
  const currentPage = NAV_GROUPS.flatMap((group) => group.items).find((item) => item.to === location.pathname)?.label ?? "İdarəetmə";

  async function handleLogout() {
    await logout();
    toast.success("Çıxış edildi.");
    navigate("/login", { replace: true });
  }

  return (
    <header className="sticky top-0 z-30 flex h-[5rem] items-center gap-3 border-b border-border/70 bg-background/88 px-4 backdrop-blur-xl sm:px-6 lg:px-8">
      <Sheet open={navOpen} onOpenChange={setNavOpen}>
        <SheetTrigger
          render={<Button variant="ghost" size="icon" className="lg:hidden" aria-label="Naviqasiya menyusunu aç" />}
        >
          <Menu className="size-5" />
        </SheetTrigger>
        <SheetContent side="left" className="w-72 p-0">
          <SheetHeader className="p-0">
            <SheetTitle className="sr-only">Naviqasiya</SheetTitle>
          </SheetHeader>
          <BrandMark />
          <SidebarNav onNavigate={() => setNavOpen(false)} />
        </SheetContent>
      </Sheet>

      <div className="min-w-0 flex-1 lg:max-w-xs">
        <p className="truncate text-sm font-semibold">{currentPage}</p>
        <p className="hidden text-xs text-muted-foreground sm:block">Nexora Academy idarəetmə mərkəzi</p>
      </div>

      <AdminSearch />

      <Button variant="ghost" size="icon" render={<a href="/" target="_blank" rel="noreferrer" />} aria-label="İctimai saytı aç">
        <ExternalLink className="size-4" />
      </Button>

      <ThemeToggle />

      <DropdownMenu>
        <DropdownMenuTrigger render={<Button variant="ghost" className="gap-2 px-2" />}>
          <Avatar className="size-7">
            <AvatarFallback className="text-xs">{initialsOf(user?.fullName ?? "?")}</AvatarFallback>
          </Avatar>
          <span className="hidden text-sm font-medium sm:inline">{user?.fullName}</span>
        </DropdownMenuTrigger>
        <DropdownMenuContent align="end" className="w-56">
          <DropdownMenuLabel>
            <p className="truncate text-sm font-medium">{user?.fullName}</p>
            <p className="truncate text-xs font-normal text-muted-foreground">{user?.email}</p>
            <p className="mt-1 text-xs font-normal text-muted-foreground">
              {user ? (ROLE_LABELS[user.role] ?? user.role) : ""}
            </p>
          </DropdownMenuLabel>
          <DropdownMenuSeparator />
          <DropdownMenuItem onClick={() => navigate("/profile")}>
            <UserRound className="size-4" />
            Profilim
          </DropdownMenuItem>
          <DropdownMenuItem variant="destructive" onClick={handleLogout}>
            <LogOut className="size-4" />
            Çıxış
          </DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>
    </header>
  );
}
