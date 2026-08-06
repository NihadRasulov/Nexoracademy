import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { Loader2, Pencil, Plus, Trash2 } from "lucide-react";
import { PageHeader } from "@/components/page-header";
import { ErrorBanner } from "@/components/error-banner";
import { DataTable } from "@/components/data-table";
import { ConfirmDialog } from "@/components/confirm-dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { ReferenceLabel, ReferencePicker } from "@/components/reference-picker";
import { StatusBadge } from "@/lib/format";
import { api } from "@/lib/api";
import { ApiError } from "@/lib/api-error";

interface CourseInstructorRow extends Record<string, unknown> {
  courseId: string;
  instructorId: string;
  role: string;
}

const ROLE_OPTIONS = [
  { value: "lead", label: "Lead" },
  { value: "co-instructor", label: "Co-instructor" },
  { value: "mentor", label: "Mentor" },
];

export function CourseInstructorsPage() {
  const queryClient = useQueryClient();
  const [formOpen, setFormOpen] = useState(false);
  const [editing, setEditing] = useState<CourseInstructorRow | null>(null);
  const [deleting, setDeleting] = useState<CourseInstructorRow | null>(null);
  const [courseId, setCourseId] = useState("");
  const [instructorId, setInstructorId] = useState("");
  const [role, setRole] = useState("lead");

  const listQuery = useQuery({
    queryKey: ["course-instructors"],
    queryFn: () => api.get<CourseInstructorRow[]>("/api/v1/course-instructors"),
  });

  const invalidate = () => queryClient.invalidateQueries({ queryKey: ["course-instructors"] });

  const createMutation = useMutation({
    mutationFn: () => api.post("/api/v1/course-instructors", { courseId, instructorId, role }),
    onSuccess: () => {
      toast.success("Əlaqə yaradıldı.");
      setFormOpen(false);
      invalidate();
    },
    onError: handleError,
  });

  const updateMutation = useMutation({
    mutationFn: () =>
      api.patch(`/api/v1/course-instructors/${courseId}/${instructorId}`, { courseId, instructorId, role }),
    onSuccess: () => {
      toast.success("Əlaqə yeniləndi.");
      setFormOpen(false);
      invalidate();
    },
    onError: handleError,
  });

  const deleteMutation = useMutation({
    mutationFn: (row: CourseInstructorRow) =>
      api.delete<void>(`/api/v1/course-instructors/${row.courseId}/${row.instructorId}`),
    onSuccess: () => {
      toast.success("Əlaqə silindi.");
      setDeleting(null);
      invalidate();
    },
    onError: handleError,
  });

  function handleError(error: unknown) {
    toast.error(error instanceof ApiError ? error.message : "Gözlənilməz xəta baş verdi.");
  }

  function openCreate() {
    setEditing(null);
    setCourseId("");
    setInstructorId("");
    setRole("lead");
    setFormOpen(true);
  }

  function openEdit(row: CourseInstructorRow) {
    setEditing(row);
    setCourseId(row.courseId);
    setInstructorId(row.instructorId);
    setRole(row.role);
    setFormOpen(true);
  }

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (editing) updateMutation.mutate();
    else createMutation.mutate();
  }

  return (
    <div>
      <PageHeader
        title="Kurs-Müəllim Əlaqələri"
        description="Hansı müəllimin hansı kursu tədris etdiyinin idarə edilməsi."
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
          { key: "courseId", label: "Kurs", render: (r) => <ReferenceLabel kind="course" value={r.courseId} /> },
          { key: "instructorId", label: "Müəllim", render: (r) => <ReferenceLabel kind="instructor" value={r.instructorId} /> },
          { key: "role", label: "Rol", render: (r) => <StatusBadge value={r.role} /> },
        ]}
        rows={listQuery.data ?? []}
        rowKey={(row) => `${row.courseId}-${row.instructorId}`}
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

      <Dialog open={formOpen} onOpenChange={setFormOpen}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>{editing ? "Əlaqəni redaktə et" : "Yeni əlaqə"}</DialogTitle>
            <DialogDescription>Kursu və müəllimi seçin.</DialogDescription>
          </DialogHeader>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-1.5">
              <Label htmlFor="courseId">Kurs</Label>
              {editing ? (
                <Input id="courseId" value={courseId} disabled />
              ) : (
                <ReferencePicker id="courseId" kind="course" value={courseId} onChange={setCourseId} placeholder="Kurs seçin" />
              )}
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="instructorId">Müəllim</Label>
              {editing ? (
                <Input id="instructorId" value={instructorId} disabled />
              ) : (
                <ReferencePicker
                  id="instructorId"
                  kind="instructor"
                  value={instructorId}
                  onChange={setInstructorId}
                  placeholder="Müəllim seçin"
                />
              )}
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="role">Rol</Label>
              <Select items={ROLE_OPTIONS} value={role} onValueChange={(v) => setRole(v ?? "lead")}>
                <SelectTrigger id="role" className="w-full">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {ROLE_OPTIONS.map((opt) => (
                    <SelectItem key={opt.value} value={opt.value}>
                      {opt.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <DialogFooter>
              <Button type="button" variant="outline" onClick={() => setFormOpen(false)}>
                Ləğv et
              </Button>
              <Button type="submit" disabled={createMutation.isPending || updateMutation.isPending}>
                {(createMutation.isPending || updateMutation.isPending) && (
                  <Loader2 className="size-4 animate-spin" />
                )}
                {editing ? "Yadda saxla" : "Yarat"}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      <ConfirmDialog
        open={deleting !== null}
        onOpenChange={(open) => !open && setDeleting(null)}
        title="Əlaqə silinsin?"
        description="Bu əməliyyat geri qaytarıla bilməz."
        pending={deleteMutation.isPending}
        onConfirm={() => deleting && deleteMutation.mutate(deleting)}
      />
    </div>
  );
}
