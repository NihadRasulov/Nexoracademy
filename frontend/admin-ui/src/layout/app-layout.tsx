import { Outlet } from "react-router-dom";
import { ScrollArea } from "@/components/ui/scroll-area";
import { BrandMark } from "@/layout/brand-mark";
import { SidebarNav } from "@/layout/sidebar-nav";
import { AppTopbar } from "@/layout/app-topbar";

export function AppLayout() {
  return (
    <div className="admin-shell flex min-h-screen">
      <aside className="admin-sidebar fixed inset-y-0 left-0 z-20 hidden w-64 flex-col border-r lg:flex">
        <BrandMark />
        <ScrollArea className="flex-1">
          <SidebarNav />
        </ScrollArea>
      </aside>

      <div className="flex min-h-screen min-w-0 flex-1 flex-col lg:ml-64">
        <AppTopbar />
        <main className="flex-1 p-4 sm:p-6 lg:p-8">
          <div className="mx-auto w-full max-w-[1500px]"><Outlet /></div>
        </main>
      </div>
    </div>
  );
}
