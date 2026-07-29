import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { Plus, Search, Pencil, Trash2 } from "lucide-react";
import { PageHeader } from "@/components/page-header";
import { ErrorBanner } from "@/components/error-banner";
import { DataTable } from "@/components/data-table";
import { PaginationBar } from "@/components/pagination-bar";
import { ConfirmDialog } from "@/components/confirm-dialog";
import { ResourceFormDialog } from "@/resources/resource-form-dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { formatDateTime, StatusBadge } from "@/lib/format";
import { api } from "@/lib/api";
import { ApiError } from "@/lib/api-error";
import { ROLE_LABELS } from "@/auth/roles";
import type { PagedResult } from "@/types/common";
import type { FieldConfig } from "@/resources/types";

export interface UserRow extends Record<string, unknown> {
  id: string;
  email: string;
  phone?: string | null;
  fullName: string;
  role: string;
  status: string;
  locale: string;
  profile?: Record<string, unknown> | null;
  lastLoginAt?: string | null;
  createdAt: string;
}

const ROLE_OPTIONS = Object.entries(ROLE_LABELS).map(([value, label]) => ({ value, label }));
const STATUS_OPTIONS = [
  { value: "PENDING_VERIFICATION", label: "Təsdiq gözləyir" },
  { value: "ACTIVE", label: "Aktiv" },
  { value: "SUSPENDED", label: "Asqıya alınıb" },
  { value: "BANNED", label: "Banlanıb" },
  { value: "DEACTIVATED", label: "Deaktiv edilib" },
];
const ROLE_FILTER_OPTIONS = [{ value: "all", label: "Bütün rollar" }, ...ROLE_OPTIONS];
const STATUS_FILTER_OPTIONS = [{ value: "all", label: "Bütün statuslar" }, ...STATUS_OPTIONS];

const USER_FIELDS: FieldConfig[] = [
  { name: "email", label: "E-poçt", type: "text", required: true },
  { name: "fullName", label: "Ad Soyad", type: "text", required: true },
  { name: "phone", label: "Telefon", type: "text" },
  {
    name: "password",
    label: "Parol",
    type: "text",
    helpText: "Redaktə zamanı boş buraxsanız parol dəyişməyəcək.",
  },
  { name: "role", label: "Rol", type: "select", options: ROLE_OPTIONS },
  { name: "status", label: "Status", type: "select", options: STATUS_OPTIONS },
  { name: "locale", label: "Dil (locale)", type: "text", placeholder: "az" },
];

export function UsersPage() {
  const queryClient = useQueryClient();
  const [q, setQ] = useState("");
  const [role, setRole] = useState<string>("");
  const [status, setStatus] = useState<string>("");
  const [page, setPage] = useState(0);
  const size = 20;

  const [formOpen, setFormOpen] = useState(false);
  const [editing, setEditing] = useState<UserRow | null>(null);
  const [deleting, setDeleting] = useState<UserRow | null>(null);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string> | null>(null);

  const queryKey = ["users", { q, role, status, page, size }];
  const listQuery = useQuery({
    queryKey,
    queryFn: () =>
      api.get<PagedResult<UserRow>>("/api/users", {
        q: q || undefined,
        role: role || undefined,
        status: status || undefined,
        page,
        size,
        sort: "createdAt,desc",
      }),
  });

  const invalidate = () => queryClient.invalidateQueries({ queryKey: ["users"] });

  const createMutation = useMutation({
    mutationFn: (body: Record<string, unknown>) => api.post<UserRow>("/api/users", body),
    onSuccess: () => {
      toast.success("İstifadəçi yaradıldı.");
      setFormOpen(false);
      invalidate();
    },
    onError: handleError,
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, body }: { id: string; body: Record<string, unknown> }) =>
      api.patch<UserRow>(`/api/users/${id}`, body),
    onSuccess: () => {
      toast.success("İstifadəçi yeniləndi.");
      setFormOpen(false);
      invalidate();
    },
    onError: handleError,
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => api.delete<void>(`/api/users/${id}`),
    onSuccess: () => {
      toast.success("İstifadəçi silindi.");
      setDeleting(null);
      invalidate();
    },
    onError: handleError,
  });

  function handleError(error: unknown) {
    if (error instanceof ApiError) {
      if (error.fieldErrors) setFieldErrors(error.fieldErrors);
      toast.error(error.message);
    } else {
      toast.error("Gözlənilməz xəta baş verdi.");
    }
  }

  function openCreate() {
    setEditing(null);
    setFieldErrors(null);
    setFormOpen(true);
  }

  function openEdit(row: UserRow) {
    setEditing(row);
    setFieldErrors(null);
    setFormOpen(true);
  }

  function handleSubmit(body: Record<string, unknown>) {
    setFieldErrors(null);
    if (editing) {
      updateMutation.mutate({ id: editing.id, body });
    } else {
      createMutation.mutate(body);
    }
  }

  return (
    <div>
      <PageHeader
        title="İstifadəçilər"
        description="Bütün istifadəçi hesablarının idarə edilməsi."
        actions={
          <Button onClick={openCreate}>
            <Plus className="size-4" />
            Yeni
          </Button>
        }
      />

      <ErrorBanner error={listQuery.error} />

      <div className="mb-4 flex flex-col gap-2 sm:flex-row">
        <div className="relative flex-1 sm:max-w-xs">
          <Search className="pointer-events-none absolute left-2.5 top-2.5 size-4 text-muted-foreground" />
          <Input
            placeholder="Ad, e-poçt axtar..."
            className="pl-8"
            value={q}
            onChange={(e: React.ChangeEvent<HTMLInputElement>) => {
              setPage(0);
              setQ(e.target.value);
            }}
          />
        </div>
        <Select
          items={ROLE_FILTER_OPTIONS}
          value={role || "all"}
          onValueChange={(v: string | null) => {
            setPage(0);
            setRole(!v || v === "all" ? "" : v);
          }}
        >
          <SelectTrigger className="w-full sm:w-48">
            <SelectValue placeholder="Rol" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">Bütün rollar</SelectItem>
            {ROLE_OPTIONS.map((opt) => (
              <SelectItem key={opt.value} value={opt.value}>
                {opt.label}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
        <Select
          items={STATUS_FILTER_OPTIONS}
          value={status || "all"}
          onValueChange={(v: string | null) => {
            setPage(0);
            setStatus(!v || v === "all" ? "" : v);
          }}
        >
          <SelectTrigger className="w-full sm:w-48">
            <SelectValue placeholder="Status" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">Bütün statuslar</SelectItem>
            {STATUS_OPTIONS.map((opt) => (
              <SelectItem key={opt.value} value={opt.value}>
                {opt.label}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      <DataTable
        columns={[
          { key: "fullName", label: "Ad Soyad" },
          { key: "email", label: "E-poçt" },
          { key: "role", label: "Rol", render: (r) => <StatusBadge value={ROLE_LABELS[r.role] ?? r.role} /> },
          { key: "status", label: "Status", render: (r) => <StatusBadge value={r.status} /> },
          { key: "lastLoginAt", label: "Son giriş", render: (r) => formatDateTime(r.lastLoginAt) },
        ]}
        rows={listQuery.data?.items ?? []}
        rowKey={(row) => row.id}
        loading={listQuery.isLoading}
        rowActions={(row) => (
          <div className="flex justify-end gap-1">
            <Button variant="ghost" size="icon" onClick={() => openEdit(row)}>
              <Pencil className="size-4" />
            </Button>
            <Button variant="ghost" size="icon" onClick={() => setDeleting(row)}>
              <Trash2 className="size-4 text-destructive" />
            </Button>
          </div>
        )}
      />

      {listQuery.data && (
        <PaginationBar
          page={listQuery.data.page}
          totalPages={listQuery.data.totalPages}
          totalElements={listQuery.data.totalElements}
          onPageChange={setPage}
        />
      )}

      <ResourceFormDialog
        open={formOpen}
        onOpenChange={setFormOpen}
        config={{ title: "İstifadəçi", fields: USER_FIELDS }}
        initialValues={editing}
        submitting={createMutation.isPending || updateMutation.isPending}
        fieldErrors={fieldErrors}
        onSubmit={handleSubmit}
      />

      <ConfirmDialog
        open={deleting !== null}
        onOpenChange={(open) => !open && setDeleting(null)}
        title="İstifadəçi silinsin?"
        description="Bu əməliyyat geri qaytarıla bilməz."
        pending={deleteMutation.isPending}
        onConfirm={() => deleting && deleteMutation.mutate(deleting.id)}
      />
    </div>
  );
}
