import { useQuery } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { PageHeader } from "@/components/page-header";
import { useAuth } from "@/auth/auth-context";
import { hasRole, ROLE_LABELS } from "@/auth/roles";
import { NAV_GROUPS } from "@/layout/nav-config";
import { API_BASE_URL, api } from "@/lib/api";
import { ApiError } from "@/lib/api-error";
import { CheckCircle2, Loader2, XCircle } from "lucide-react";

interface BackendHealth {
  status: string;
}

export function DashboardPage() {
  const { user } = useAuth();

  const healthQuery = useQuery({
    queryKey: ["backend-health"],
    queryFn: () => api.get<BackendHealth>("/sys-control-9912/api/health/backend"),
    retry: false,
    refetchInterval: 30_000,
  });

  const quickLinks = NAV_GROUPS.flatMap((g) => g.items).filter(
    (item) => item.to !== "/dashboard" && hasRole(user?.role, item.roleGroup),
  );

  return (
    <div>
      <PageHeader
        title={`Xoş gəldiniz, ${user?.fullName ?? ""}`}
        description={`Rolunuz: ${user ? (ROLE_LABELS[user.role] ?? user.role) : "-"}`}
      />

      <Card className="mb-6">
        <CardHeader className="flex-row items-center justify-between space-y-0">
          <div>
            <CardTitle className="text-base">Backend Bağlantısı</CardTitle>
            <CardDescription>{API_BASE_URL} əlçatanlığı</CardDescription>
          </div>
          <HealthBadge
            loading={healthQuery.isLoading}
            healthy={healthQuery.data?.status === "UP"}
            error={healthQuery.error}
          />
        </CardHeader>
      </Card>

      <h2 className="mb-3 text-sm font-medium text-muted-foreground">Modullar</h2>
      <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
        {quickLinks.map((item) => (
          <Link key={item.to} to={item.to}>
            <Card className="h-full transition-colors hover:border-primary/50 hover:bg-accent/40">
              <CardContent className="flex items-center gap-3 py-4">
                <div className="flex size-9 shrink-0 items-center justify-center rounded-lg bg-primary/10 text-primary">
                  <item.icon className="size-4.5" />
                </div>
                <span className="text-sm font-medium">{item.label}</span>
              </CardContent>
            </Card>
          </Link>
        ))}
      </div>
    </div>
  );
}

function HealthBadge({ loading, healthy, error }: { loading: boolean; healthy: boolean; error: unknown }) {
  if (loading) {
    return (
      <Badge variant="secondary" className="gap-1.5">
        <Loader2 className="size-3 animate-spin" />
        Yoxlanılır
      </Badge>
    );
  }
  if (healthy) {
    return (
      <Badge className="gap-1.5 bg-emerald-600 text-white hover:bg-emerald-600">
        <CheckCircle2 className="size-3" />
        Aktiv
      </Badge>
    );
  }
  return (
    <Badge variant="destructive" className="gap-1.5">
      <XCircle className="size-3" />
      {error instanceof ApiError ? error.message : "Əlçatan deyil"}
    </Badge>
  );
}
