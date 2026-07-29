import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "@/auth/auth-context";
import { hasRole, type RoleGroup } from "@/auth/roles";
import { Loader2 } from "lucide-react";

export function RequireAuth() {
  const { user, loading } = useAuth();
  const location = useLocation();

  if (loading) {
    return (
      <div className="flex h-screen items-center justify-center">
        <Loader2 className="size-6 animate-spin text-muted-foreground" />
      </div>
    );
  }

  if (!user) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  return <Outlet />;
}

export function RequireRole({ group }: { group: RoleGroup }) {
  const { user } = useAuth();

  if (!hasRole(user?.role, group)) {
    return <Navigate to="/403" replace />;
  }

  return <Outlet />;
}
