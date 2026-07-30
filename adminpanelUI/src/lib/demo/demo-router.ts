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
  "/api/admin/categories": { rows: clone(seed.demoCategories) },
  "/api/admin/courses": { rows: clone(seed.demoCourses) },
  "/api/admin/instructors": { rows: clone(seed.demoInstructors) },
  "/api/admin/course-groups": { rows: clone(seed.demoCourseGroups) },
  "/api/admin/scholarships": { rows: clone(seed.demoScholarships) },
  "/api/admin/cms-content": { rows: clone(seed.demoCmsContent) },
  "/api/admin/sales/campaigns": { rows: clone(seed.demoCampaigns) },
  "/api/admin/sales/chat-sessions": { rows: clone(seed.demoChatSessions) },
  "/api/admin/sales/contact-submissions": { rows: clone(seed.demoContactSubmissions) },
  "/api/admin/sales/leads": { rows: clone(seed.demoLeads) },
  "/api/admin/oauth-accounts": { rows: clone(seed.demoOAuthAccounts) },
  "/api/admin/sessions": { rows: clone(seed.demoSessions) },
  "/api/admin/notifications": { rows: clone(seed.demoNotifications) },
  "/api/admin/kb-articles": { rows: clone(seed.demoKbArticles) },
  "/api/admin/course-reviews": { rows: clone(seed.demoCourseReviews) },
  "/api/admin/graduate-outcomes": { rows: clone(seed.demoGraduateOutcomes) },
  "/api/admin/audit-logs": { rows: clone(seed.demoAuditLogs) },
  "/api/admin/users": { rows: clone(seed.demoUsers) },
  "/api/admin/enrollments": { rows: clone(seed.demoEnrollments) },
  "/api/admin/payments": { rows: clone(seed.demoPayments) },
};

let meState: Record<string, unknown> = clone(seed.DEMO_ME);
let courseInstructorRows: Record<string, unknown>[] = clone(seed.demoCourseInstructors);

const PAGED_PATHS = new Set(["/api/admin/users", "/api/admin/courses"]);

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

  if (path === "/api/auth/login" && method === "POST") return meState as T;
  if (path === "/api/auth/logout" && method === "POST") return undefined as T;
  if (path === "/api/auth/me" && method === "GET") return meState as T;
  if (path === "/api/health/backend" && method === "GET") return seed.DEMO_HEALTH as T;

  if (path === "/api/admin/users/me" && method === "PATCH") {
    meState = { ...meState, ...(body as Record<string, unknown>), updatedAt: new Date().toISOString() };
    const usersStore = stores["/api/admin/users"];
    const idx = usersStore.rows.findIndex((r) => r.id === meState.id);
    if (idx >= 0) usersStore.rows[idx] = { ...usersStore.rows[idx], ...meState };
    return meState as T;
  }
  if (path === "/api/admin/users/me/password" && method === "POST") return undefined as T;

  if (segments[0] === "api" && segments[1] === "admin" && segments[2] === "course-instructors") {
    if (segments.length === 3) {
      if (method === "GET") return courseInstructorRows as T;
      if (method === "POST") {
        const created = body as Record<string, unknown>;
        courseInstructorRows.push(created);
        return created as T;
      }
    }
    if (segments.length === 5) {
      const [, , , courseId, instructorId] = segments;
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

  if (path.match(/^\/api\/admin\/enrollments\/[^/]+\/cancel$/) && method === "POST") {
    const id = segments[3];
    const store = findStore("/api/admin/enrollments");
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
  if (path.match(/^\/api\/admin\/payments\/[^/]+\/capture$/) && method === "POST") {
    const id = segments[3];
    const store = findStore("/api/admin/payments");
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
        if (basePath === "/api/admin/categories" || basePath === "/api/admin/scholarships") {
          created.id = Math.max(0, ...store.rows.map((r) => Number(r.id) || 0)) + 1;
        }
        if (
          basePath === "/api/admin/cms-content" ||
          basePath === "/api/admin/oauth-accounts" ||
          basePath === "/api/admin/course-reviews" ||
          basePath === "/api/admin/graduate-outcomes" ||
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
