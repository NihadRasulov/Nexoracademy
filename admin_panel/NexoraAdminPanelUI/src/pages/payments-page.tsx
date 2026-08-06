import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { CheckCheck, Pencil, Plus, Trash2 } from "lucide-react";
import { PageHeader } from "@/components/page-header";
import { ErrorBanner } from "@/components/error-banner";
import { DataTable } from "@/components/data-table";
import { ConfirmDialog } from "@/components/confirm-dialog";
import { ResourceFormDialog } from "@/resources/resource-form-dialog";
import { Button } from "@/components/ui/button";
import { formatDateTime, StatusBadge } from "@/lib/format";
import { ReferenceLabel } from "@/components/reference-picker";
import { api } from "@/lib/api";
import { ApiError } from "@/lib/api-error";
import type { FieldConfig } from "@/resources/types";

interface PaymentRow extends Record<string, unknown> {
  id: string;
  enrollmentId: string;
  method: string;
  amount: number;
  currency: string;
  status: string;
  initiatedAt: string;
  capturedAt?: string | null;
}

const PAYMENT_FIELDS: FieldConfig[] = [
  { name: "enrollmentId", label: "Qeydiyyat", type: "reference", refKind: "enrollment", required: true, placeholder: "Qeydiyyat seçin" },
  {
    name: "method",
    label: "Ödəniş metodu",
    type: "select",
    required: true,
    options: [
      { value: "CARD", label: "Kart" },
      { value: "CASH", label: "Nağd" },
      { value: "BANK_TRANSFER", label: "Bank köçürməsi" },
    ],
  },
  { name: "amount", label: "Məbləğ", type: "number", required: true },
  { name: "currency", label: "Valyuta", type: "text", placeholder: "AZN" },
  { name: "externalTxnId", label: "Xarici tranzaksiya ID", type: "text" },
  { name: "idempotencyKey", label: "Idempotency açarı", type: "text", required: true, autoGenerate: "uuid" },
  { name: "installments", label: "Taksitlər (JSON massiv)", type: "jsonArray", advanced: true, helpText: "Texniki sahə — taksit qrafiki JSON formatında." },
];

export function PaymentsPage() {
  const queryClient = useQueryClient();
  const [formOpen, setFormOpen] = useState(false);
  const [editing, setEditing] = useState<PaymentRow | null>(null);
  const [deleting, setDeleting] = useState<PaymentRow | null>(null);
  const [capturing, setCapturing] = useState<PaymentRow | null>(null);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string> | null>(null);

  const listQuery = useQuery({
    queryKey: ["payments"],
    queryFn: () => api.get<PaymentRow[]>("/api/v1/payments"),
  });

  const invalidate = () => queryClient.invalidateQueries({ queryKey: ["payments"] });

  const createMutation = useMutation({
    mutationFn: (body: Record<string, unknown>) => api.post("/api/v1/payments", body),
    onSuccess: () => {
      toast.success("Ödəniş yaradıldı.");
      setFormOpen(false);
      invalidate();
    },
    onError: handleError,
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, body }: { id: string; body: Record<string, unknown> }) =>
      api.patch(`/api/v1/payments/${id}`, body),
    onSuccess: () => {
      toast.success("Ödəniş yeniləndi.");
      setFormOpen(false);
      invalidate();
    },
    onError: handleError,
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => api.delete<void>(`/api/v1/payments/${id}`),
    onSuccess: () => {
      toast.success("Ödəniş silindi.");
      setDeleting(null);
      invalidate();
    },
    onError: handleError,
  });

  const captureMutation = useMutation({
    mutationFn: (id: string) => api.post(`/api/v1/payments/${id}/capture`),
    onSuccess: () => {
      toast.success("Ödəniş capture edildi.");
      setCapturing(null);
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

  function openEdit(row: PaymentRow) {
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
        title="Ödənişlər"
        description="Bütün ödənişlərin idarə edilməsi."
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
          { key: "enrollmentId", label: "Qeydiyyat", render: (r) => <ReferenceLabel kind="enrollment" value={r.enrollmentId} /> },
          { key: "method", label: "Metod", render: (r) => <StatusBadge value={r.method} /> },
          {
            key: "amount",
            label: "Məbləğ",
            render: (r) => `${r.amount} ${r.currency ?? ""}`,
          },
          { key: "status", label: "Status", render: (r) => <StatusBadge value={r.status} /> },
          { key: "initiatedAt", label: "Başladılıb", render: (r) => formatDateTime(r.initiatedAt) },
          { key: "capturedAt", label: "Capture olunub", render: (r) => formatDateTime(r.capturedAt) },
        ]}
        rows={listQuery.data ?? []}
        rowKey={(row) => row.id}
        loading={listQuery.isLoading}
        rowActions={(row) => (
          <div className="flex justify-end gap-1">
            {!row.capturedAt && (
              <Button variant="ghost" size="icon" onClick={() => setCapturing(row)} aria-label="Ödənişi təsdiqlə" title="Ödənişi təsdiqlə">
                <CheckCheck className="size-4 text-emerald-600" />
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
        config={{ title: "Ödəniş", fields: PAYMENT_FIELDS }}
        initialValues={editing}
        submitting={createMutation.isPending || updateMutation.isPending}
        fieldErrors={fieldErrors}
        onSubmit={handleSubmit}
      />

      <ConfirmDialog
        open={deleting !== null}
        onOpenChange={(open) => !open && setDeleting(null)}
        title="Ödəniş silinsin?"
        description="Bu əməliyyat geri qaytarıla bilməz."
        pending={deleteMutation.isPending}
        onConfirm={() => deleting && deleteMutation.mutate(deleting.id)}
      />

      <ConfirmDialog
        open={capturing !== null}
        onOpenChange={(open) => !open && setCapturing(null)}
        title="Ödəniş capture edilsin?"
        description="Bu əməliyyat ödənişi təsdiqləyəcək."
        destructive={false}
        confirmLabel="Capture et"
        pending={captureMutation.isPending}
        onConfirm={() => capturing && captureMutation.mutate(capturing.id)}
      />
    </div>
  );
}
