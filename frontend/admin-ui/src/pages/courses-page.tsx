import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { Plus, Search, Pencil, Trash2, ImagePlus } from "lucide-react";
import { PageHeader } from "@/components/page-header";
import { ErrorBanner } from "@/components/error-banner";
import { DataTable } from "@/components/data-table";
import { PaginationBar } from "@/components/pagination-bar";
import { ConfirmDialog } from "@/components/confirm-dialog";
import { ResourceFormDialog } from "@/resources/resource-form-dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { BoolBadge, StatusBadge } from "@/lib/format";
import { api, assetUrl } from "@/lib/api";
import { ApiError } from "@/lib/api-error";
import type { PagedResult } from "@/types/common";
import type { FieldConfig } from "@/resources/types";

export interface CourseRow extends Record<string, unknown> {
  id: string;
  slug: string;
  categoryId: number;
  instructorId?: string | null;
  title: string;
  imageUrl?: string | null;
  difficulty: string;
  deliveryFormat: string;
  basePrice?: number | null;
  currency?: string | null;
  published: boolean;
  active: boolean;
}

const COURSE_FIELDS: FieldConfig[] = [
  { name: "slug", label: "Slug", type: "text", required: true, placeholder: "web-development-101" },
  { name: "categoryId", label: "Kateqoriya", type: "reference", refKind: "category", required: true, placeholder: "Kateqoriya seçin" },
  { name: "instructorId", label: "Təlimçi", type: "reference", refKind: "instructor", placeholder: "Təlimçi seçin (istəyə bağlı)" },
  { name: "title", label: "Başlıq", type: "text", required: true },
  {
    name: "imageUrl",
    label: "Kurs şəkli",
    type: "image",
    placeholder: "Şəkil yükləyin",
    helpText: "JPG, PNG və ya WEBP. Maksimum 5MB.",
  },
  { name: "shortDescription", label: "Qısa açıqlama", type: "textarea" },
  { name: "fullDescription", label: "Tam açıqlama", type: "textarea" },
  { name: "targetAudience", label: "Hədəf auditoriya", type: "textarea" },
  {
    name: "difficulty",
    label: "Çətinlik səviyyəsi",
    type: "select",
    options: [
      { value: "BEGINNER", label: "Başlanğıc" },
      { value: "INTERMEDIATE", label: "Orta" },
      { value: "ADVANCED", label: "İrəli" },
    ],
  },
  { name: "durationWeeks", label: "Müddət (həftə)", type: "number" },
  {
    name: "deliveryFormat",
    label: "Tədris formatı",
    type: "select",
    options: [
      { value: "ONLINE", label: "Onlayn" },
      { value: "OFFLINE", label: "Əyani" },
      { value: "HYBRID", label: "Hibrid" },
    ],
  },
  { name: "locationText", label: "Məkan", type: "text" },
  { name: "basePrice", label: "Qiymət", type: "number" },
  { name: "currency", label: "Valyuta", type: "text", placeholder: "AZN" },
  { name: "pricePeriod", label: "Qiymət dövrü", type: "text", placeholder: "ONE_TIME / MONTHLY" },
  { name: "published", label: "Yayımda", type: "boolean" },
  { name: "active", label: "Aktiv", type: "boolean" },
  { name: "archived", label: "Arxivlənib", type: "boolean" },
  { name: "validFrom", label: "Başlama tarixi", type: "datetime" },
  { name: "validUntil", label: "Bitmə tarixi", type: "datetime" },
  { name: "relatedCourseIds", label: "Əlaqəli kurslar", type: "referenceArray", refKind: "course", placeholder: "Kurs seçin" },
  { name: "content", label: "Əlavə məzmun", type: "keyValue", advanced: true, helpText: "Açar-dəyər cütləri (məs. syllabusUrl → ...)." },
];

export function CoursesPage() {
  const queryClient = useQueryClient();
  const [q, setQ] = useState("");
  const [page, setPage] = useState(0);
  const size = 20;

  const [formOpen, setFormOpen] = useState(false);
  const [editing, setEditing] = useState<CourseRow | null>(null);
  const [deleting, setDeleting] = useState<CourseRow | null>(null);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string> | null>(null);

  const queryKey = ["courses", { q, page, size }];
  const listQuery = useQuery({
    queryKey,
    queryFn: () =>
      api.get<PagedResult<CourseRow>>("/api/v1/courses", {
        q: q || undefined,
        page,
        size,
        sort: "createdAt,desc",
      }),
  });

  const invalidate = () => queryClient.invalidateQueries({ queryKey: ["courses"] });

  const createMutation = useMutation({
    mutationFn: (body: Record<string, unknown>) => api.post<CourseRow>("/api/v1/courses", body),
    onSuccess: () => {
      toast.success("Kurs yaradıldı.");
      setFormOpen(false);
      invalidate();
    },
    onError: handleError,
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, body }: { id: string; body: Record<string, unknown> }) =>
      api.patch<CourseRow>(`/api/v1/courses/${id}`, body),
    onSuccess: () => {
      toast.success("Kurs yeniləndi.");
      setFormOpen(false);
      invalidate();
    },
    onError: handleError,
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => api.delete<void>(`/api/v1/courses/${id}`),
    onSuccess: () => {
      toast.success("Kurs silindi.");
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

  function openEdit(row: CourseRow) {
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
        title="Kurslar"
        description="Bütün kursların idarə edilməsi."
        actions={
          <Button onClick={openCreate}>
            <Plus className="size-4" />
            Yeni
          </Button>
        }
      />

      <ErrorBanner error={listQuery.error} />

      <div className="relative mb-4 max-w-sm">
        <Search className="pointer-events-none absolute left-2.5 top-2.5 size-4 text-muted-foreground" />
        <Input
          placeholder="Başlıq/slug axtar..."
          className="pl-8"
          value={q}
          onChange={(e: React.ChangeEvent<HTMLInputElement>) => {
            setPage(0);
            setQ(e.target.value);
          }}
        />
      </div>

      <DataTable
        columns={[
          {
            key: "imageUrl",
            label: "Şəkil",
            render: (r) =>
              r.imageUrl ? (
                <img
                  src={assetUrl(r.imageUrl)}
                  alt=""
                  className="h-9 w-14 rounded border border-border object-cover"
                />
              ) : (
                <div className="flex h-9 w-14 items-center justify-center rounded border border-border bg-muted/30">
                  <ImagePlus className="size-3.5 text-muted-foreground" aria-hidden />
                </div>
              ),
          },
          { key: "title", label: "Başlıq" },
          { key: "slug", label: "Slug" },
          { key: "difficulty", label: "Səviyyə", render: (r) => <StatusBadge value={r.difficulty} /> },
          { key: "deliveryFormat", label: "Format", render: (r) => <StatusBadge value={r.deliveryFormat} /> },
          {
            key: "basePrice",
            label: "Qiymət",
            render: (r) => (r.basePrice != null ? `${r.basePrice} ${r.currency ?? ""}` : "-"),
          },
          { key: "published", label: "Yayımda", render: (r) => <BoolBadge value={r.published} /> },
        ]}
        rows={listQuery.data?.content ?? []}
        rowKey={(row) => row.id}
        loading={listQuery.isLoading}
        rowActions={(row) => (
          <div className="flex justify-end gap-1">
            <Button variant="ghost" size="icon" onClick={() => openEdit(row)} aria-label="Redaktə et" title="Redaktə et">
              <Pencil className="size-4" />
            </Button>
            <Button variant="ghost" size="icon" onClick={() => setDeleting(row)} aria-label="Sil" title="Sil">
              <Trash2 className="size-4 text-destructive" />
            </Button>
          </div>
        )}
      />

      {listQuery.data && (
        <PaginationBar
          page={listQuery.data.number}
          totalPages={listQuery.data.totalPages}
          totalElements={listQuery.data.totalElements}
          onPageChange={setPage}
        />
      )}

      <ResourceFormDialog
        open={formOpen}
        onOpenChange={setFormOpen}
        config={{ title: "Kurs", fields: COURSE_FIELDS }}
        initialValues={editing}
        submitting={createMutation.isPending || updateMutation.isPending}
        fieldErrors={fieldErrors}
        onSubmit={handleSubmit}
      />

      <ConfirmDialog
        open={deleting !== null}
        onOpenChange={(open) => !open && setDeleting(null)}
        title="Kurs silinsin?"
        description="Bu əməliyyat geri qaytarıla bilməz."
        pending={deleteMutation.isPending}
        onConfirm={() => deleting && deleteMutation.mutate(deleting.id)}
      />
    </div>
  );
}
