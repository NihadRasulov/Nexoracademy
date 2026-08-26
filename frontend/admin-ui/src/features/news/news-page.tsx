import { useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { ImagePlus, Newspaper, Pencil, Plus, Search, Trash2 } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/page-header";
import { ErrorBanner } from "@/components/error-banner";
import { DataTable } from "@/components/data-table";
import { ConfirmDialog } from "@/components/confirm-dialog";
import { ImageUploadField } from "@/components/image-upload";
import { BoolBadge } from "@/lib/format";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Switch } from "@/components/ui/switch";
import { Textarea } from "@/components/ui/textarea";
import { api, assetUrl } from "@/lib/api";
import { ApiError } from "@/lib/api-error";
import type { CmsContentDocument } from "@/features/homepage/types";

interface NewsRow extends CmsContentDocument, Record<string, unknown> {
  body?: string | null;
}

interface NewsFormState {
  key: string;
  title: string;
  body: string;
  coverImageUrl: string;
  published: boolean;
  sortOrder: string;
}

const EMPTY_FORM: NewsFormState = {
  key: "",
  title: "",
  body: "",
  coverImageUrl: "",
  published: false,
  sortOrder: "0",
};

function slugify(value: string) {
  return value
    .toLocaleLowerCase("az")
    .replaceAll("ə", "e")
    .replaceAll("ı", "i")
    .replaceAll("ö", "o")
    .replaceAll("ü", "u")
    .replaceAll("ş", "s")
    .replaceAll("ç", "c")
    .replaceAll("ğ", "g")
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/^-+|-+$/g, "")
    .slice(0, 120);
}

function coverOf(row: NewsRow): string {
  const data = row.data && typeof row.data === "object" ? row.data : {};
  return typeof data.cover_image_url === "string" ? data.cover_image_url : "";
}

export function NewsPage() {
  const queryClient = useQueryClient();
  const [search, setSearch] = useState("");
  const [formOpen, setFormOpen] = useState(false);
  const [editing, setEditing] = useState<NewsRow | null>(null);
  const [deleting, setDeleting] = useState<NewsRow | null>(null);
  const [form, setForm] = useState<NewsFormState>(EMPTY_FORM);

  const query = useQuery({
    queryKey: ["cms-news"],
    queryFn: () => api.get<NewsRow[]>("/api/v1/content/cms-content"),
  });

  const rows = useMemo(() => {
    const needle = search.trim().toLocaleLowerCase("az");
    return (query.data ?? [])
      .filter((item) => String(item.type).toUpperCase() === "NEWS")
      .filter((item) => !needle || `${item.title ?? ""} ${item.key} ${item.body ?? ""}`.toLocaleLowerCase("az").includes(needle))
      .sort((left, right) => left.sortOrder - right.sortOrder || left.key.localeCompare(right.key, "az"));
  }, [query.data, search]);

  const saveMutation = useMutation({
    mutationFn: async () => {
      const payload = {
        key: form.key.trim(),
        type: "NEWS",
        title: form.title.trim(),
        body: form.body.trim(),
        data: { cover_image_url: form.coverImageUrl.trim() },
        published: form.published,
        sortOrder: Number(form.sortOrder) || 0,
      };
      return editing
        ? api.patch<NewsRow>(`/api/v1/content/cms-content/${editing.id}`, payload)
        : api.post<NewsRow>("/api/v1/content/cms-content", payload);
    },
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ["cms-news"] });
      setFormOpen(false);
      toast.success(editing ? "Xəbər yeniləndi." : "Xəbər yaradıldı.");
    },
    onError: (error) => toast.error(error instanceof ApiError ? error.message : "Xəbər yadda saxlanıla bilmədi."),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => api.delete<void>(`/api/v1/content/cms-content/${id}`),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ["cms-news"] });
      setDeleting(null);
      toast.success("Xəbər silindi.");
    },
    onError: (error) => toast.error(error instanceof ApiError ? error.message : "Xəbər silinə bilmədi."),
  });

  function openCreate() {
    setEditing(null);
    setForm(EMPTY_FORM);
    setFormOpen(true);
  }

  function openEdit(row: NewsRow) {
    setEditing(row);
    setForm({
      key: row.key,
      title: row.title ?? "",
      body: row.body ?? "",
      coverImageUrl: coverOf(row),
      published: row.published,
      sortOrder: String(row.sortOrder ?? 0),
    });
    setFormOpen(true);
  }

  function handleTitle(value: string) {
    setForm((current) => ({
      ...current,
      title: value,
      key: editing || current.key ? current.key : `news.${slugify(value)}`,
    }));
  }

  return (
    <div>
      <PageHeader
        eyebrow="Məzmun studiyası"
        title="Xəbərlər"
        description="Ana səhifə və xəbərlər səhifəsində görünən elanları hazırlayın, sıralayın və yayımlayın."
        actions={<Button onClick={openCreate}><Plus className="size-4" /> Yeni xəbər</Button>}
      />
      <ErrorBanner error={query.error} />

      <div className="relative mb-4 max-w-md">
        <Search className="pointer-events-none absolute top-2.5 left-3 size-4 text-muted-foreground" />
        <Input className="pl-9" placeholder="Xəbər axtar..." value={search} onChange={(event) => setSearch(event.target.value)} />
      </div>

      <DataTable
        rows={rows}
        loading={query.isLoading}
        rowKey={(row) => String(row.id)}
        columns={[
          {
            key: "cover",
            label: "Şəkil",
            render: (row) => coverOf(row) ? <img src={assetUrl(coverOf(row))} alt="" className="h-11 w-16 rounded-lg object-cover ring-1 ring-border" /> : <div className="flex h-11 w-16 items-center justify-center rounded-lg bg-muted"><ImagePlus className="size-4 text-muted-foreground" /></div>,
          },
          { key: "title", label: "Başlıq" },
          { key: "key", label: "Açar", className: "font-mono text-xs" },
          { key: "published", label: "Yayımda", render: (row) => <BoolBadge value={row.published} /> },
          { key: "sortOrder", label: "Sıra" },
        ]}
        rowActions={(row) => <div className="flex justify-end gap-1"><Button variant="ghost" size="icon" onClick={() => openEdit(row)} aria-label="Redaktə et"><Pencil className="size-4" /></Button><Button variant="ghost" size="icon" onClick={() => setDeleting(row)} aria-label="Sil"><Trash2 className="size-4 text-destructive" /></Button></div>}
      />

      <Dialog open={formOpen} onOpenChange={setFormOpen}>
        <DialogContent className="max-h-[90vh] overflow-y-auto sm:max-w-2xl">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2"><Newspaper className="size-5 text-primary" />{editing ? "Xəbəri redaktə et" : "Yeni xəbər"}</DialogTitle>
            <DialogDescription>Başlıq, mətn və cover şəkli ictimai saytda istifadə olunur.</DialogDescription>
          </DialogHeader>
          <form className="space-y-4" onSubmit={(event) => { event.preventDefault(); saveMutation.mutate(); }}>
            <div className="space-y-1.5"><Label>Başlıq *</Label><Input required value={form.title} onChange={(event) => handleTitle(event.target.value)} /></div>
            <div className="space-y-1.5"><Label>Unikal açar *</Label><Input required value={form.key} onChange={(event) => setForm((current) => ({ ...current, key: event.target.value }))} placeholder="news.yeni-xeber" /><p className="text-xs text-muted-foreground">Kiçik hərflər, rəqəmlər və nöqtə/defis istifadə edin.</p></div>
            <div className="space-y-1.5"><Label>Mətn *</Label><Textarea required rows={7} value={form.body} onChange={(event) => setForm((current) => ({ ...current, body: event.target.value }))} /></div>
            <div className="space-y-1.5"><Label>Cover şəkli</Label><ImageUploadField id="news-cover" value={form.coverImageUrl} onChange={(value) => setForm((current) => ({ ...current, coverImageUrl: value }))} /></div>
            <div className="grid gap-4 sm:grid-cols-2">
              <div className="space-y-1.5"><Label>Sıra</Label><Input type="number" min="0" value={form.sortOrder} onChange={(event) => setForm((current) => ({ ...current, sortOrder: event.target.value }))} /></div>
              <div className="flex items-center justify-between rounded-xl border p-3"><div><p className="text-sm font-medium">Yayımla</p><p className="text-xs text-muted-foreground">Aktiv olduqda saytda görünür.</p></div><Switch checked={form.published} onCheckedChange={(published) => setForm((current) => ({ ...current, published }))} /></div>
            </div>
            <DialogFooter><Button type="button" variant="outline" onClick={() => setFormOpen(false)}>Ləğv et</Button><Button type="submit" disabled={saveMutation.isPending}>{editing ? "Yadda saxla" : "Yarat"}</Button></DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      <ConfirmDialog open={deleting !== null} onOpenChange={(open) => !open && setDeleting(null)} title="Xəbər silinsin?" description="Bu əməliyyat geri qaytarıla bilməz." pending={deleteMutation.isPending} onConfirm={() => deleting && deleteMutation.mutate(deleting.id)} />
    </div>
  );
}
