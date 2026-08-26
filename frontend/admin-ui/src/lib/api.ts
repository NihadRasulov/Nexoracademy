import { ApiError, type ErrorResponse } from "@/lib/api-error";

const configuredApiOrigin = (import.meta.env.VITE_API_BASE_URL as string | undefined)?.replace(/\/+$/, "");
export const API_BASE_URL = configuredApiOrigin || "";
const REQUEST_TIMEOUT_MS = 30_000;

function adminBasePath(): string {
  if (typeof document === "undefined") return "";
  const pathname = new URL(document.baseURI).pathname.replace(/\/+$/, "");
  return pathname === "/" ? "" : pathname;
}

export type QueryParams = Record<string, string | number | boolean | undefined | null>;

export function assetUrl(value: string | null | undefined): string {
  const raw = String(value ?? "").trim();
  if (!raw || !raw.startsWith("/")) return raw;
  return API_BASE_URL ? `${API_BASE_URL}${raw}` : raw;
}

function buildUrl(path: string, query?: QueryParams): string {
  const origin = API_BASE_URL || window.location.origin;
  const normalizedPath = path.startsWith("/api/v1/")
    ? `${adminBasePath()}${path}`
    : path;
  const url = new URL(normalizedPath.replace(/^\//, ""), `${origin}/`);
  if (query) {
    for (const [key, value] of Object.entries(query)) {
      if (value !== undefined && value !== null && value !== "") {
        url.searchParams.set(key, String(value));
      }
    }
  }
  return url.toString();
}

export function adminApiUrl(path: string): string {
  return buildUrl(path);
}

async function request<T>(
  method: string,
  path: string,
  options?: { body?: unknown; query?: QueryParams; isFormData?: boolean; timeoutMs?: number },
): Promise<T> {
  let response: Response;
  try {
    const headers: Record<string, string> = {};
    if (options?.body !== undefined && !options?.isFormData) {
      headers["Content-Type"] = "application/json";
    }
    response = await fetch(buildUrl(path, options?.query), {
      method,
      credentials: "include",
      headers,
      body: options?.body !== undefined ? (options.isFormData ? (options.body as FormData) : JSON.stringify(options.body)) : undefined,
      signal: AbortSignal.timeout(options?.timeoutMs ?? REQUEST_TIMEOUT_MS),
    });
  } catch (error) {
    if (error instanceof DOMException && error.name === "TimeoutError") {
      throw new ApiError(0, { code: "REQUEST_TIMEOUT", message: "Server cavabı üçün vaxt bitdi." });
    }

    throw new ApiError(0, { code: "NETWORK_ERROR", message: "Serverlə əlaqə qurulmadı." });
  }

  if (response.status === 204) {
    return undefined as T;
  }

  const text = await response.text();
  const data = parseJson(text, response.status);

  if (!response.ok) {
    const errorBody: ErrorResponse = isErrorResponse(data) ? data : {
      code: "UNKNOWN_ERROR",
      message: `Server ${response.status} statusunu qaytardı.`,
    };
    throw new ApiError(response.status, errorBody);
  }

  return data as T;
}

function parseJson(text: string, status: number): unknown {
  if (!text) return null;

  try {
    return JSON.parse(text) as unknown;
  } catch {
    throw new ApiError(status, {
      code: "INVALID_RESPONSE",
      message: "Server gözlənilməyən formatda cavab qaytardı.",
    });
  }
}

function isErrorResponse(value: unknown): value is ErrorResponse {
  if (!value || typeof value !== "object") return false;
  const candidate = value as Partial<ErrorResponse>;
  return (
    (typeof candidate.code === "string" || typeof candidate.error === "string") &&
    typeof candidate.message === "string"
  );
}

export const api = {
  get: <T>(path: string, query?: QueryParams) => request<T>("GET", path, { query }),
  post: <T>(path: string, body?: unknown) => request<T>("POST", path, { body }),
  postForm: <T>(path: string, formData: FormData, timeoutMs?: number) =>
    request<T>("POST", path, { body: formData, isFormData: true, timeoutMs }),
  put: <T>(path: string, body?: unknown) => request<T>("PUT", path, { body }),
  patch: <T>(path: string, body?: unknown) => request<T>("PATCH", path, { body }),
  delete: <T>(path: string) => request<T>("DELETE", path),
};
