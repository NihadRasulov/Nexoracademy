import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { Ban, Loader2, Pencil, Plus, Trash2 } from "lucide-react";
import { PageHeader } from "@/components/page-header";
import { ErrorBanner } from "@/components/error-banner";
import { DataTable } from "@/components/data-table";
import { ConfirmDialog } from "@/components/confirm-dialog";
import { ResourceFormDialog } from "@/resources/resource-form-dialog";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { formatDateTime, StatusBadge } from "@/lib/format";
import { ReferenceLabel } from "@/components/reference-picker";
import { api } from "@/lib/api";
import { ApiError } from "@/lib/api-error";
import type { FieldConfig } from "@/resources/types";

interface EnrollmentRow extends Record<string, unknown> {
  id: string;
  userId: string;
  groupId: string;
  status: string;
  enrolledAt: string;
  holdExpiresAt?: string | null;
  cancelledAt?: string | null;
}

const STATUS_OPTIONS = [
  { value: "WAITLISTED", label: "Növbədə" },
  { value: "HELD", label: "Saxlanılıb" },
  { value: "PENDING_PAYMENT", label: "Ödəniş gözləyir" },
  { value: "CONFIRMED", label: "Təsdiqlənib" },
  { value: "COMPLETED", label: "Tamamlanıb" },
  { value: "CANCELLED", label: "Ləğv edilib" },
  { value: "REFUNDED", label: "Geri qaytarılıb" },
];

const ENROLLMENT_FIELDS: FieldConfig[] = [
  { name: "userId", label: "İstifadəçi", type: "reference", refKind: "user", required: true, placeholder: "İstifadəçi seçin" },
  { name: "groupId", label: "Kurs qrupu", type: "reference", refKind: "courseGroup", required: true, placeholder: "Kurs qrupu seçin" },
  {
    name: "status",
    label: "Status",
    type: "select",
    options: STATUS_OPTIONS,
    helpText: "Yaradılanda yalnız staff tərəfindən təyin edilə bilər.",
  },
  { name: "idempotencyKey", label: "Idempotency açarı", type: "text", required: true, autoGenerate: "uuid" },
  { name: "consentVersion", label: "Razılıq versiyası", type: "text" },
  { name: "consentGivenAt", label: "Razılıq tarixi", type: "datetime" },
];

export function EnrollmentsPage() {
  const queryClient = useQueryClient();
  const [formOpen, setFormOpen] = useState(false);
  const [editing, setEditing] = useState<EnrollmentRow | null>(null);
  const [deleting, setDeleting] = useState<EnrollmentRow | null>(null);
  const [cancelling, setCancelling] = useState<EnrollmentRow | null>(null);
  const [cancelReason, setCancelReason] = useState("");
  const [fieldErrors, setFieldErrors] = useState<Record<string, string> | null>(null);

  const listQuery = useQuery({
    queryKey: ["enrollments"],
    queryFn: () => api.get<EnrollmentRow[]>("/api/v1/enrollments"),
  });

  const invalidate = () => queryClient.invalidateQueries({ queryKey: ["enrollments"] });

  const createMutation = useMutation({
    mutationFn: (body: Record<string, unknown>) => api.post("/api/v1/enrollments", body),
    onSuccess: () => {
      toast.success("Qeydiyyat yaradıldı.");
      setFormOpen(false);
      invalidate();
    },
    onError: handleError,
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, body }: { id: string; body: Record<string, unknown> }) =>
      api.patch(`/api/v1/enrollments/${id}`, body),
    onSuccess: () => {
      toast.success("Qeydiyyat yeniləndi.");
      setFormOpen(false);
      invalidate();
    },
    onError: handleError,
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => api.delete<void>(`/api/v1/enrollments/${id}`),
    onSuccess: () => {
      toast.success("Qeydiyyat silindi.");
      setDeleting(null);
      invalidate();
    },
    onError: handleError,
  });

  const cancelMutation = useMutation({
    mutationFn: ({ id, reason }: { id: string; reason: string }) =>
      api.post(`/api/v1/enrollments/${id}/cancel`, reason ? { reason } : {}),
    onSuccess: () => {
      toast.success("Qeydiyyat ləğv edildi.");
      setCancelling(null);
      setCancelReason("");
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

  function openEdit(row: EnrollmentRow) {
    setEditing(row);
    setFieldErrors(null);
    setFormOpen(true);
  }

  function handleSubmit(body: Record<string, unknown>) {
    setFieldErrors(null);
    if (editing) updateMutation.mutate({ id: editing.id, body });
    else createMutation.mutate(body);
  }

  return (
    <div>
      <PageHeader
        title="Qeydiyyatlar"
        description="Tələbələrin kurs qruplarına qeydiyyatının idarə edilməsi."
        actions={
          <Button onClick={openCreate}>
            <Plus className="size-4" />
            Yeni
          </Button>
        }
      />

      <ErrorBanner error={listQuery.error} />

      <DataTable
        columns={[
          { key: "userId", label: "İstifadəçi", render: (r) => <ReferenceLabel kind="user" value={r.userId} /> },
          { key: "groupId", label: "Qrup", render: (r) => <ReferenceLabel kind="courseGroup" value={r.groupId} /> },
          { key: "status", label: "Status", render: (r) => <StatusBadge value={r.status} /> },
          { key: "enrolledAt", label: "Qeydiyyat tarixi", render: (r) => formatDateTime(r.enrolledAt) },
          { key: "cancelledAt", label: "Ləğv tarixi", render: (r) => formatDateTime(r.cancelledAt) },
        ]}
        rows={listQuery.data ?? []}
        rowKey={(row) => row.id}
        loading={listQuery.isLoading}
        rowActions={(row) => (
          <div className="flex justify-end gap-1">
            {row.status !== "CANCELLED" && row.status !== "COMPLETED" && (
              <Button variant="ghost" size="icon" onClick={() => setCancelling(row)} aria-label="Ləğv et" title="Ləğv et">
                <Ban className="size-4 text-amber-600" />
              </Button>
            )}
            <Button variant="ghost" size="icon" onClick={() => openEdit(row)} aria-label="Redaktə et" title="Redaktə et">
              <Pencil className="size-4" />
            </Button>
            <Button variant="ghost" size="icon" onClick={() => setDeleting(row)} aria-label="Sil" title="Sil">
              <Trash2 className="size-4 text-destructive" />
            </Button>
          </div>
        )}
      />

      <ResourceFormDialog
        open={formOpen}
        onOpenChange={setFormOpen}
        config={{ title: "Qeydiyyat", fields: ENROLLMENT_FIELDS }}
        initialValues={editing}
        submitting={createMutation.isPending || updateMutation.isPending}
        fieldErrors={fieldErrors}
        onSubmit={handleSubmit}
      />

      <ConfirmDialog
        open={deleting !== null}
        onOpenChange={(open) => !open && setDeleting(null)}
        title="Qeydiyyat silinsin?"
        description="Bu əməliyyat geri qaytarıla bilməz."
        pending={deleteMutation.isPending}
        onConfirm={() => deleting && deleteMutation.mutate(deleting.id)}
      />

      <Dialog open={cancelling !== null} onOpenChange={(open: boolean) => !open && setCancelling(null)}>
        <DialogContent className="sm:max-w-sm">
          <DialogHeader>
            <DialogTitle>Qeydiyyatı ləğv et</DialogTitle>
            <DialogDescription>İstəyə bağlı olaraq ləğv səbəbini qeyd edə bilərsiniz.</DialogDescription>
          </DialogHeader>
          <div className="space-y-1.5">
            <Label htmlFor="cancelReason">Səbəb (istəyə bağlı)</Label>
            <Textarea
              id="cancelReason"
              value={cancelReason}
              onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) => setCancelReason(e.target.value)}
            />
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setCancelling(null)}>
              Bağla
            </Button>
            <Button
              variant="destructive"
              disabled={cancelMutation.isPending}
              onClick={() => cancelling && cancelMutation.mutate({ id: cancelling.id, reason: cancelReason })}
            >
              {cancelMutation.isPending && <Loader2 className="size-4 animate-spin" />}
              Ləğv et
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
