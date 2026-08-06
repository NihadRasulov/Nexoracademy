import { ApiError, type ErrorResponse } from "@/lib/api-error";
import { DEMO_MODE } from "@/lib/demo/demo-mode";
import { demoRequest } from "@/lib/demo/demo-router";

const configuredApiOrigin = (import.meta.env.VITE_API_BASE_URL as string | undefined)?.replace(/\/+$/, "");
export const API_BASE_URL = configuredApiOrigin || "";
const REQUEST_TIMEOUT_MS = 30_000;

export type QueryParams = Record<string, string | number | boolean | undefined | null>;

function buildUrl(path: string, query?: QueryParams): string {
  const url = new URL(path.replace(/^\//, ""), API_BASE_URL + "/");
  if (query) {
    for (const [key, value] of Object.entries(query)) {
      if (value !== undefined && value !== null && value !== "") {
        url.searchParams.set(key, String(value));
      }
    }
  }
  return url.toString();
}

async function request<T>(
  method: string,
  path: string,
  options?: { body?: unknown; query?: QueryParams },
): Promise<T> {
  if (DEMO_MODE) {
    return demoRequest<T>(method, path, options?.body, options?.query);
  }

  let response: Response;
  try {
    response = await fetch(buildUrl(path, options?.query), {
      method,
      credentials: "include",
      headers: options?.body !== undefined ? { "Content-Type": "application/json" } : undefined,
      body: options?.body !== undefined ? JSON.stringify(options.body) : undefined,
      signal: AbortSignal.timeout(REQUEST_TIMEOUT_MS),
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
  return typeof candidate.code === "string" && typeof candidate.message === "string";
}

export const api = {
  get: <T>(path: string, query?: QueryParams) => request<T>("GET", path, { query }),
  post: <T>(path: string, body?: unknown) => request<T>("POST", path, { body }),
  put: <T>(path: string, body?: unknown) => request<T>("PUT", path, { body }),
  patch: <T>(path: string, body?: unknown) => request<T>("PATCH", path, { body }),
  delete: <T>(path: string) => request<T>("DELETE", path),
};
