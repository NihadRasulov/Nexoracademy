import { ApiError, type ErrorResponse } from "@/lib/api-error";
import { DEMO_MODE } from "@/lib/demo/demo-mode";
import { demoRequest } from "@/lib/demo/demo-router";

const BASE_URL = import.meta.env.VITE_API_BASE_URL as string;

export type QueryParams = Record<string, string | number | boolean | undefined | null>;

function buildUrl(path: string, query?: QueryParams): string {
  const url = new URL(path.replace(/^\//, ""), BASE_URL + "/");
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
    });
  } catch {
    throw new ApiError(0, { code: "NETWORK_ERROR", message: "Serverlə əlaqə qurulmadı." });
  }

  if (response.status === 204) {
    return undefined as T;
  }

  const text = await response.text();
  const data = text ? JSON.parse(text) : null;

  if (!response.ok) {
    const errorBody: ErrorResponse = data ?? {
      code: "UNKNOWN_ERROR",
      message: `Server ${response.status} statusunu qaytardı.`,
    };
    throw new ApiError(response.status, errorBody);
  }

  return data as T;
}

export const api = {
  get: <T>(path: string, query?: QueryParams) => request<T>("GET", path, { query }),
  post: <T>(path: string, body?: unknown) => request<T>("POST", path, { body }),
  put: <T>(path: string, body?: unknown) => request<T>("PUT", path, { body }),
  patch: <T>(path: string, body?: unknown) => request<T>("PATCH", path, { body }),
  delete: <T>(path: string) => request<T>("DELETE", path),
};
