import { ApiError } from "@/lib/api-error";
import * as seed from "@/lib/demo/demo-data";
import type { QueryParams } from "@/lib/api";

function clone<T>(value: T): T {
  return JSON.parse(JSON.stringify(value));
}

interface Store {
  rows: Record<string, unknown>[];
}

const stores: Record<string, Store> = {
  "/api/categories": { rows: clone(seed.demoCategories) },
  "/api/courses": { rows: clone(seed.demoCourses) },
  "/api/instructors": { rows: clone(seed.demoInstructors) },
  "/api/course-groups": { rows: clone(seed.demoCourseGroups) },
  "/api/scholarships": { rows: clone(seed.demoScholarships) },
  "/api/cms-content": { rows: clone(seed.demoCmsContent) },
  "/api/sales/campaigns": { rows: clone(seed.demoCampaigns) },
  "/api/sales/chat-sessions": { rows: clone(seed.demoChatSessions) },
  "/api/sales/contact-submissions": { rows: clone(seed.demoContactSubmissions) },
  "/api/sales/leads": { rows: clone(seed.demoLeads) },
  "/api/oauth-accounts": { rows: clone(seed.demoOAuthAccounts) },
  "/api/sessions": { rows: clone(seed.demoSessions) },
  "/api/notifications": { rows: clone(seed.demoNotifications) },
  "/api/kb-articles": { rows: clone(seed.demoKbArticles) },
  "/api/course-reviews": { rows: clone(seed.demoCourseReviews) },
  "/api/graduate-outcomes": { rows: clone(seed.demoGraduateOutcomes) },
  "/api/admin/audit-logs": { rows: clone(seed.demoAuditLogs) },
  "/api/users": { rows: clone(seed.demoUsers) },
  "/api/enrollments": { rows: clone(seed.demoEnrollments) },
  "/api/payments": { rows: clone(seed.demoPayments) },
};

let meState: Record<string, unknown> = clone(seed.DEMO_ME);
let courseInstructorRows: Record<string, unknown>[] = clone(seed.demoCourseInstructors);

const PAGED_PATHS = new Set(["/api/users", "/api/courses"]);

function genId(): string {
  return crypto.randomUUID();
}

function notFound(path: string): never {
  throw new ApiError(404, { code: "NOT_FOUND", message: `Demo rejimi: "${path}" üçün qeyd tapılmadı.` });
}

function findStore(basePath: string): Store {
  const store = stores[basePath];
  if (!store) throw new ApiError(404, { code: "NOT_FOUND", message: `Demo rejimi: "${basePath}" tanınmır.` });
  return store;
}

function paginate(rows: Record<string, unknown>[], query?: QueryParams) {
  let filtered = rows;
  const q = query?.q ? String(query.q).toLowerCase() : "";
  if (q) {
    filtered = filtered.filter((r) =>
      Object.values(r).some((v) => typeof v === "string" && v.toLowerCase().includes(q)),
    );
  }
  if (query?.role) filtered = filtered.filter((r) => r.role === query.role);
  if (query?.status) filtered = filtered.filter((r) => r.status === query.status);

  const size = Number(query?.size ?? 20);
  const page = Number(query?.page ?? 0);
  const totalElements = filtered.length;
  const totalPages = Math.max(1, Math.ceil(totalElements / size));
  const items = filtered.slice(page * size, page * size + size);

  return { items, totalElements, totalPages, page, size };
}

export async function demoRequest<T>(
  method: string,
  path: string,
  body: unknown,
  query?: QueryParams,
): Promise<T> {
  await new Promise((resolve) => setTimeout(resolve, 220));

  const segments = path.split("/").filter(Boolean);

  if (path === "/api/v1/auth/login" && method === "POST")
    return { accessToken: "demo-access-token", refreshToken: "demo-refresh-token", tokenType: "Bearer", expiresInSeconds: 3600 } as T;
  if (path === "/api/v1/auth/logout" && method === "POST") return undefined as T;
  if (path === "/api/v1/users/me" && method === "GET") return meState as T;
  if (path === "/api/health/backend" && method === "GET") return seed.DEMO_HEALTH as T;

  if (path === "/api/users/me" && method === "PATCH") {
    meState = { ...meState, ...(body as Record<string, unknown>), updatedAt: new Date().toISOString() };
    const usersStore = stores["/api/users"];
    const idx = usersStore.rows.findIndex((r) => r.id === meState.id);
    if (idx >= 0) usersStore.rows[idx] = { ...usersStore.rows[idx], ...meState };
    return meState as T;
  }
  if (path === "/api/users/me/password" && method === "POST") return undefined as T;

  if (segments[0] === "api" && segments[1] === "course-instructors") {
    if (segments.length === 2) {
      if (method === "GET") return courseInstructorRows as T;
      if (method === "POST") {
        const created = body as Record<string, unknown>;
        courseInstructorRows.push(created);
        return created as T;
      }
    }
    if (segments.length === 4) {
      const [, , courseId, instructorId] = segments;
      const idx = courseInstructorRows.findIndex(
        (r) => r.courseId === courseId && r.instructorId === instructorId,
      );
      if (method === "GET") return (idx >= 0 ? courseInstructorRows[idx] : notFound(path)) as T;
      if (method === "PUT" || method === "PATCH") {
        if (idx < 0) notFound(path);
        courseInstructorRows[idx] = { ...courseInstructorRows[idx], ...(body as Record<string, unknown>) };
        return courseInstructorRows[idx] as T;
      }
      if (method === "DELETE") {
        courseInstructorRows = courseInstructorRows.filter(
          (r) => !(r.courseId === courseId && r.instructorId === instructorId),
        );
        return undefined as T;
      }
    }
    notFound(path);
  }

  if (path.match(/^\/api\/enrollments\/[^/]+\/cancel$/) && method === "POST") {
    const id = segments[2];
    const store = findStore("/api/enrollments");
    const idx = store.rows.findIndex((r) => r.id === id);
    if (idx < 0) notFound(path);
    store.rows[idx] = {
      ...store.rows[idx],
      status: "CANCELLED",
      cancelledAt: new Date().toISOString(),
      cancelReason: (body as { reason?: string } | undefined)?.reason ?? null,
    };
    return store.rows[idx] as T;
  }
  if (path.match(/^\/api\/payments\/[^/]+\/capture$/) && method === "POST") {
    const id = segments[2];
    const store = findStore("/api/payments");
    const idx = store.rows.findIndex((r) => r.id === id);
    if (idx < 0) notFound(path);
    store.rows[idx] = { ...store.rows[idx], status: "CAPTURED", capturedAt: new Date().toISOString() };
    return store.rows[idx] as T;
  }

  const basePath = Object.keys(stores)
    .sort((a, b) => b.length - a.length)
    .find((p) => path === p || path.startsWith(`${p}/`));

  if (basePath) {
    const store = findStore(basePath);
    const rest = path.slice(basePath.length).replace(/^\//, "");

    if (!rest) {
      if (method === "GET") {
        if (PAGED_PATHS.has(basePath)) return paginate(store.rows, query) as T;
        return store.rows as T;
      }
      if (method === "POST") {
        const created: Record<string, unknown> = { id: genId(), ...(body as Record<string, unknown>) };
        if (basePath === "/api/categories" || basePath === "/api/scholarships") {
          created.id = Math.max(0, ...store.rows.map((r) => Number(r.id) || 0)) + 1;
        }
        if (
          basePath === "/api/cms-content" ||
          basePath === "/api/oauth-accounts" ||
          basePath === "/api/course-reviews" ||
          basePath === "/api/graduate-outcomes" ||
          basePath === "/api/admin/audit-logs"
        ) {
          created.id = Math.max(0, ...store.rows.map((r) => Number(r.id) || 0)) + 1;
        }
        created.createdAt = new Date().toISOString();
        store.rows.push(created);
        return created as T;
      }
    } else {
      const id = rest;
      const idx = store.rows.findIndex((r) => String(r.id) === id);
      if (method === "GET") return (idx >= 0 ? store.rows[idx] : notFound(path)) as T;
      if (method === "PUT" || method === "PATCH") {
        if (idx < 0) notFound(path);
        store.rows[idx] = { ...store.rows[idx], ...(body as Record<string, unknown>), updatedAt: new Date().toISOString() };
        return store.rows[idx] as T;
      }
      if (method === "DELETE") {
        store.rows.splice(idx, 1);
        return undefined as T;
      }
    }
  }

  notFound(path);
}
