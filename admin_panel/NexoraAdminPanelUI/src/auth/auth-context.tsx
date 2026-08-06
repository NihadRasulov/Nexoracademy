import { createContext, useCallback, useContext, useEffect, useState, type ReactNode } from "react";
import { api, setAccessToken, setRefreshToken, clearTokens, getRefreshToken } from "@/lib/api";
import { ApiError } from "@/lib/api-error";
import type { MeResponse } from "@/types/common";

interface AuthContextValue {
  user: MeResponse | null;
  loading: boolean;
  initializationError: ApiError | null;
  login: (email: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  refreshMe: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<MeResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [initializationError, setInitializationError] = useState<ApiError | null>(null);

  const refreshMe = useCallback(async () => {
    try {
      const me = await api.get<MeResponse>("/api/v1/users/me");
      setUser(me);
      setInitializationError(null);
    } catch (error) {
      if (error instanceof ApiError && (error.status === 401 || error.status === 403)) {
        setUser(null);
        setInitializationError(null);
      } else {
        setUser(null);
        setInitializationError(
          error instanceof ApiError
            ? error
            : new ApiError(0, { code: "AUTH_INITIALIZATION_FAILED", message: "Sessiya yoxlanılmadı." }),
        );
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
    const tokenRes = await api.post<{ accessToken: string; refreshToken: string; tokenType: string; expiresInSeconds: number }>(
      "/api/v1/auth/login",
      { email, password },
    );
    setAccessToken(tokenRes.accessToken);
    setRefreshToken(tokenRes.refreshToken);
    await refreshMe();
  }, [refreshMe]);

  const logout = useCallback(async () => {
    try {
      const rt = getRefreshToken();
      if (rt) {
        await api.post("/api/v1/auth/logout", { refreshToken: rt });
      }
    } finally {
      clearTokens();
      setUser(null);
      setInitializationError(null);
    }
  }, []);

  return (
    <AuthContext.Provider value={{ user, loading, initializationError, login, logout, refreshMe }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within an AuthProvider");
  return ctx;
}
