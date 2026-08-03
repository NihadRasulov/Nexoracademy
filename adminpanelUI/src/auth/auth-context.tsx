import { createContext, useCallback, useContext, useEffect, useState, type ReactNode } from "react";
import { api } from "@/lib/api";
import { ApiError } from "@/lib/api-error";
import type { MeResponse } from "@/types/common";

interface AuthContextValue {
  user: MeResponse | null;
  loading: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  refreshMe: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<MeResponse | null>(null);
  const [loading, setLoading] = useState(true);

  const refreshMe = useCallback(async () => {
    try {
      const me = await api.get<MeResponse>("/api/auth/me");
      setUser(me);
    } catch (error) {
      if (error instanceof ApiError && (error.status === 401 || error.status === 403)) {
        setUser(null);
      } else {
        setUser(null);
      }
    }
  }, []);

  useEffect(() => {
    void (async () => {
      await refreshMe();
      setLoading(false);
    })();
  }, [refreshMe]);

  const login = useCallback(async (email: string, password: string) => {
    const me = await api.post<MeResponse>("/api/auth/login", { email, password });
    setUser(me);
  }, []);

  const logout = useCallback(async () => {
    try {
      await api.post("/api/auth/logout");
    } finally {
      setUser(null);
    }
  }, []);

  return (
    <AuthContext.Provider value={{ user, loading, login, logout, refreshMe }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within an AuthProvider");
  return ctx;
}
