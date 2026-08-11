import { Outlet } from "react-router-dom";
import { ScrollArea } from "@/components/ui/scroll-area";
import { BrandMark } from "@/layout/brand-mark";
import { SidebarNav } from "@/layout/sidebar-nav";
import { AppTopbar } from "@/layout/app-topbar";

export function AppLayout() {
  return (
    <div className="flex min-h-screen bg-muted/30">
      <aside className="fixed inset-y-0 left-0 z-20 hidden w-64 flex-col border-r bg-background lg:flex">
        <BrandMark />
        <ScrollArea className="flex-1">
          <SidebarNav />
        </ScrollArea>
      </aside>

      <div className="flex min-h-screen flex-1 flex-col lg:ml-64">
        <AppTopbar />
        <main className="flex-1 p-4 sm:p-6">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
