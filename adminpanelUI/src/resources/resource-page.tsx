import { useMemo, useState } from "react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { PageHeader } from "@/components/page-header";
import { ErrorBanner } from "@/components/error-banner";
import { DataTable } from "@/components/data-table";
import { ConfirmDialog } from "@/components/confirm-dialog";
import { ResourceFormDialog } from "@/resources/resource-form-dialog";
import { useResourceCrud } from "@/resources/use-resource-crud";
import type { ResourceConfig } from "@/resources/types";
import { ApiError } from "@/lib/api-error";
import { Pencil, Plus, Search, Trash2 } from "lucide-react";

export function ResourcePage<T extends Record<string, unknown>>({ config }: { config: ResourceConfig<T> }) {
  const { listQuery, createMutation, updateMutation, deleteMutation } = useResourceCrud<T>(config);
  const [search, setSearch] = useState("");
  const [formOpen, setFormOpen] = useState(false);
  const [editing, setEditing] = useState<T | null>(null);
  const [deleting, setDeleting] = useState<T | null>(null);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string> | null>(null);

  const rows = useMemo(() => {
    const items = listQuery.data ?? [];
    if (!search.trim() || !config.searchKeys?.length) return items;
    const term = search.trim().toLowerCase();
    return items.filter((item) =>
      config.searchKeys!.some((key) => String(item[key] ?? "").toLowerCase().includes(term)),
    );
  }, [listQuery.data, search, config.searchKeys]);

  function openCreate() {
    setEditing(null);
    setFieldErrors(null);
    setFormOpen(true);
  }

  function openEdit(row: T) {
    setEditing(row);
    setFieldErrors(null);
    setFormOpen(true);
  }

  function handleSubmit(body: Record<string, unknown>) {
    setFieldErrors(null);
    if (editing) {
      updateMutation.mutate(
        { id: String(editing[config.idField]), body: body as Partial<T> },
        {
          onSuccess: () => {
            toast.success(`${config.title} yeniləndi.`);
            setFormOpen(false);
          },
          onError: (error) => handleMutationError(error),
        },
      );
    } else {
      createMutation.mutate(body as Partial<T>, {
        onSuccess: () => {
          toast.success(`${config.title} yaradıldı.`);
          setFormOpen(false);
        },
        onError: (error) => handleMutationError(error),
      });
    }
  }

  function handleMutationError(error: unknown) {
    if (error instanceof ApiError) {
      if (error.fieldErrors) setFieldErrors(error.fieldErrors);
      toast.error(error.message);
    } else {
      toast.error("Gözlənilməz xəta baş verdi.");
    }
  }

  function handleDelete() {
    if (!deleting) return;
    deleteMutation.mutate(String(deleting[config.idField]), {
      onSuccess: () => {
        toast.success(`${config.title} silindi.`);
        setDeleting(null);
      },
      onError: (error) => handleMutationError(error),
    });
  }

  return (
    <div>
      <PageHeader
        title={config.title}
        description={config.description}
        actions={
          <Button onClick={openCreate}>
            <Plus className="size-4" />
            Yeni
          </Button>
        }
      />

      <ErrorBanner error={listQuery.error} />

      {config.searchKeys?.length ? (
        <div className="relative mb-4 max-w-sm">
          <Search className="pointer-events-none absolute left-2.5 top-2.5 size-4 text-muted-foreground" />
          <Input
            placeholder="Axtar..."
            className="pl-8"
            value={search}
            onChange={(e: React.ChangeEvent<HTMLInputElement>) => setSearch(e.target.value)}
          />
        </div>
      ) : null}

      <DataTable
        columns={config.columns}
        rows={rows}
        rowKey={(row) => String(row[config.idField])}
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

      <ResourceFormDialog
        open={formOpen}
        onOpenChange={setFormOpen}
        config={config}
        initialValues={editing}
        submitting={createMutation.isPending || updateMutation.isPending}
        fieldErrors={fieldErrors}
        onSubmit={handleSubmit}
      />

      <ConfirmDialog
        open={deleting !== null}
        onOpenChange={(open) => !open && setDeleting(null)}
        title={`${config.title} silinsin?`}
        description="Bu əməliyyat geri qaytarıla bilməz."
        pending={deleteMutation.isPending}
        onConfirm={handleDelete}
      />
    </div>
  );
}
