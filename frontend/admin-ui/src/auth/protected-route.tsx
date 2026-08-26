import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "@/auth/auth-context";
import { AlertTriangle, Loader2, RefreshCw } from "lucide-react";
import { Button } from "@/components/ui/button";

export function RequireAuth() {
  const { user, loading, initializationError } = useAuth();
  const location = useLocation();

  if (loading) {
    return (
      <div className="flex h-screen items-center justify-center">
        <Loader2 className="size-6 animate-spin text-muted-foreground" />
      </div>
    );
  }

  if (initializationError) {
    return (
      <main className="flex min-h-screen flex-col items-center justify-center gap-4 p-6 text-center">
        <div className="flex size-14 items-center justify-center rounded-full bg-destructive/10">
          <AlertTriangle className="size-7 text-destructive" />
        </div>
        <div>
          <h1 className="text-lg font-semibold">Panelə qoşulmaq mümkün olmadı</h1>
          <p className="mt-1 max-w-md text-sm text-muted-foreground">{initializationError.message}</p>
        </div>
        <Button type="button" variant="outline" onClick={() => window.location.reload()}>
          <RefreshCw className="size-4" />
          Yenidən yoxla
        </Button>
      </main>
    );
  }

  if (!user) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  return <Outlet />;
}
