import { useQuery } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import {
  ArrowUpRight,
  BookOpenCheck,
  BriefcaseBusiness,
  Mail,
  MessageSquareText,
  Newspaper,
  UsersRound,
} from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { ErrorBanner } from "@/components/error-banner";
import { useAuth } from "@/auth/auth-context";
import { api } from "@/lib/api";

interface ActivityItem {
  type: "application" | "contact";
  title: string | null;
  detail: string | null;
  createdAt: string;
}

interface DashboardSummary {
  publishedCourses: number;
  publishedNews: number;
  pendingApplications: number;
  pendingContacts: number;
  activeSubscribers: number;
  recentActivity: ActivityItem[];
}

const formatter = new Intl.DateTimeFormat("az-AZ", { dateStyle: "medium", timeStyle: "short" });

export function DashboardPage() {
  const { user } = useAuth();
  const query = useQuery({
    queryKey: ["dashboard-summary"],
    queryFn: () => api.get<DashboardSummary>("/api/v1/admin/dashboard/summary"),
  });

  const firstName = user?.fullName?.split(" ")[0] || "Admin";

  return (
    <div className="space-y-7">
      <section className="flex flex-col gap-5 rounded-[1.75rem] border bg-card px-6 py-7 shadow-sm sm:flex-row sm:items-end sm:justify-between lg:px-8">
        <div>
          <p className="text-sm font-semibold text-primary">Nexora Academy CMS</p>
          <h1 className="mt-2 text-3xl font-bold tracking-tight sm:text-4xl">Salam, {firstName}!</h1>
          <p className="mt-2 text-sm text-muted-foreground">Sayt məzmununun bugünkü vəziyyəti bir baxışda.</p>
        </div>
        <Button render={<Link to="/homepage" />} className="rounded-xl">
          Ana səhifəni redaktə et <ArrowUpRight className="size-4" />
        </Button>
      </section>

      <ErrorBanner error={query.error} />

      <section>
        <div className="mb-4 flex items-center justify-between">
          <h2 className="text-xl font-bold tracking-tight">Ümumi görünüş</h2>
          <p className="text-xs text-muted-foreground">Canlı CMS göstəriciləri</p>
        </div>
        {query.isLoading ? (
          <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-5">{Array.from({ length: 5 }).map((_, index) => <Skeleton key={index} className="h-36 rounded-2xl" />)}</div>
        ) : (
          <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-5">
            <Metric icon={BookOpenCheck} label="Yayımda kurs" value={query.data?.publishedCourses ?? 0} tone="blue" />
            <Metric icon={Newspaper} label="Yayımda xəbər" value={query.data?.publishedNews ?? 0} tone="violet" />
            <Metric icon={BriefcaseBusiness} label="Yeni başvuru" value={query.data?.pendingApplications ?? 0} tone="amber" />
            <Metric icon={MessageSquareText} label="Yeni mesaj" value={query.data?.pendingContacts ?? 0} tone="rose" />
            <Metric icon={Mail} label="Abunəçi" value={query.data?.activeSubscribers ?? 0} tone="emerald" />
          </div>
        )}
      </section>

      <div className="grid gap-5 xl:grid-cols-[1.35fr_.65fr]">
        <Card className="rounded-2xl">
          <CardHeader className="flex-row items-center justify-between">
            <div>
              <CardTitle>Son fəaliyyət</CardTitle>
              <p className="mt-1 text-sm text-muted-foreground">Saytdan gələn ən son müraciətlər</p>
            </div>
            <Button variant="ghost" size="sm" render={<Link to="/inbox" />}>Hamısına bax</Button>
          </CardHeader>
          <CardContent className="space-y-1">
            {(query.data?.recentActivity ?? []).length === 0 ? (
              <p className="rounded-xl border border-dashed p-8 text-center text-sm text-muted-foreground">Hələ fəaliyyət yoxdur.</p>
            ) : query.data?.recentActivity.map((item, index) => (
              <div key={`${item.type}-${item.createdAt}-${index}`} className="flex items-center gap-3 border-b py-3 last:border-0">
                <div className="flex size-10 shrink-0 items-center justify-center rounded-xl bg-primary/8 text-primary">
                  {item.type === "application" ? <BriefcaseBusiness className="size-4" /> : <MessageSquareText className="size-4" />}
                </div>
                <div className="min-w-0 flex-1">
                  <p className="truncate text-sm font-semibold">{item.title || "Adsız müraciət"}</p>
                  <p className="truncate text-xs text-muted-foreground">{item.detail || "Əlavə məlumat yoxdur"}</p>
                </div>
                <time className="hidden text-xs text-muted-foreground sm:block">{formatter.format(new Date(item.createdAt))}</time>
              </div>
            ))}
          </CardContent>
        </Card>

        <Card className="rounded-2xl bg-[#10233f] text-white dark:bg-card">
          <CardHeader>
            <CardTitle className="flex items-center gap-2"><UsersRound className="size-5 text-sky-300" /> Sürətli keçidlər</CardTitle>
          </CardHeader>
          <CardContent className="grid gap-2">
            <QuickLink to="/catalog" label="Kurs kataloqu" />
            <QuickLink to="/news" label="Yeni xəbər yarat" />
            <QuickLink to="/vacancies" label="Vakansiyaları idarə et" />
            <QuickLink to="/about" label="Haqqımızda səhifəsi" />
          </CardContent>
        </Card>
      </div>
    </div>
  );
}

function Metric({ icon: Icon, label, value, tone }: { icon: typeof BookOpenCheck; label: string; value: number; tone: string }) {
  const tones: Record<string, string> = {
    blue: "bg-blue-50 text-blue-600 dark:bg-blue-500/10",
    violet: "bg-violet-50 text-violet-600 dark:bg-violet-500/10",
    amber: "bg-amber-50 text-amber-600 dark:bg-amber-500/10",
    rose: "bg-rose-50 text-rose-600 dark:bg-rose-500/10",
    emerald: "bg-emerald-50 text-emerald-600 dark:bg-emerald-500/10",
  };
  return (
    <Card className="rounded-2xl">
      <CardContent className="p-5">
        <div className={`flex size-10 items-center justify-center rounded-xl ${tones[tone]}`}><Icon className="size-5" /></div>
        <p className="mt-5 text-3xl font-bold tracking-tight">{value}</p>
        <div className="mt-1 flex items-center justify-between"><p className="text-xs text-muted-foreground">{label}</p><Badge variant="outline" className="text-[10px]">Canlı</Badge></div>
      </CardContent>
    </Card>
  );
}

function QuickLink({ to, label }: { to: string; label: string }) {
  return <Link to={to} className="flex items-center justify-between rounded-xl bg-white/8 px-4 py-3 text-sm font-medium transition hover:bg-white/14">{label}<ArrowUpRight className="size-4 text-sky-300" /></Link>;
}
