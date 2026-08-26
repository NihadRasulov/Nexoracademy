import { useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Pencil, Plus, Search, Trash2 } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/page-header";
import { ErrorBanner } from "@/components/error-banner";
import { DataTable } from "@/components/data-table";
import { ConfirmDialog } from "@/components/confirm-dialog";
import { BoolBadge } from "@/lib/format";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Switch } from "@/components/ui/switch";
import { Textarea } from "@/components/ui/textarea";
import { api } from "@/lib/api";
import { ApiError } from "@/lib/api-error";
import type { CmsContentDocument } from "@/features/homepage/types";

type CollectionType = "FAQ" | "VACANCY";
interface Row extends CmsContentDocument, Record<string, unknown> { body?: string | null }
interface FormState { key: string; title: string; body: string; published: boolean; sortOrder: string; department: string; location: string; employmentType: string }

const EMPTY: FormState = { key: "", title: "", body: "", published: false, sortOrder: "0", department: "", location: "", employmentType: "" };

function slugify(value: string) {
  return value.toLocaleLowerCase("az").replaceAll("ə", "e").replaceAll("ı", "i").replaceAll("ö", "o").replaceAll("ü", "u").replaceAll("ş", "s").replaceAll("ç", "c").replaceAll("ğ", "g").replace(/[^a-z0-9]+/g, "-").replace(/^-+|-+$/g, "").slice(0, 120);
}

export function CmsCollectionPage({ type }: { type: CollectionType }) {
  const isFaq = type === "FAQ";
  const meta = isFaq
    ? { title: "Tez-tez verilən suallar", description: "Saytda görünən sual-cavabları sıralayın və yayımlayın.", prefix: "faq", singular: "sual" }
    : { title: "Vakansiyalar", description: "Karyera səhifəsində görünən açıq mövqeləri idarə edin.", prefix: "vacancy", singular: "vakansiya" };
  const queryClient = useQueryClient();
  const [search, setSearch] = useState("");
  const [editing, setEditing] = useState<Row | null>(null);
  const [deleting, setDeleting] = useState<Row | null>(null);
  const [formOpen, setFormOpen] = useState(false);
  const [form, setForm] = useState<FormState>(EMPTY);

  const query = useQuery({ queryKey: ["cms-collection", type], queryFn: () => api.get<Row[]>("/api/v1/content/cms-content") });
  const rows = useMemo(() => {
    const needle = search.trim().toLocaleLowerCase("az");
    return (query.data ?? []).filter((row) => String(row.type).toUpperCase() === type)
      .filter((row) => !needle || `${row.title ?? ""} ${row.body ?? ""}`.toLocaleLowerCase("az").includes(needle))
      .sort((a, b) => a.sortOrder - b.sortOrder);
  }, [query.data, search, type]);

  const saveMutation = useMutation({
    mutationFn: () => {
      const data = isFaq ? {} : { department: form.department.trim(), location: form.location.trim(), employmentType: form.employmentType.trim() };
      const payload = { key: form.key.trim(), type, title: form.title.trim(), body: form.body.trim(), data, published: form.published, sortOrder: Number(form.sortOrder) || 0 };
      return editing ? api.patch<Row>(`/api/v1/content/cms-content/${editing.id}`, payload) : api.post<Row>("/api/v1/content/cms-content", payload);
    },
    onSuccess: () => { void queryClient.invalidateQueries({ queryKey: ["cms-collection", type] }); setFormOpen(false); toast.success(`${meta.singular[0].toLocaleUpperCase("az")}${meta.singular.slice(1)} yadda saxlanıldı.`); },
    onError: (error) => toast.error(error instanceof ApiError ? error.message : "Məzmun yadda saxlanıla bilmədi."),
  });
  const deleteMutation = useMutation({
    mutationFn: (id: number) => api.delete<void>(`/api/v1/content/cms-content/${id}`),
    onSuccess: () => { void queryClient.invalidateQueries({ queryKey: ["cms-collection", type] }); setDeleting(null); toast.success("Məzmun silindi."); },
    onError: (error) => toast.error(error instanceof ApiError ? error.message : "Məzmun silinə bilmədi."),
  });

  function openCreate() { setEditing(null); setForm(EMPTY); setFormOpen(true); }
  function openEdit(row: Row) {
    const data = row.data && typeof row.data === "object" ? row.data : {};
    setEditing(row);
    setForm({ key: row.key, title: row.title ?? "", body: row.body ?? "", published: row.published, sortOrder: String(row.sortOrder ?? 0), department: String(data.department ?? ""), location: String(data.location ?? ""), employmentType: String(data.employmentType ?? "") });
    setFormOpen(true);
  }

  return (
    <div>
      <PageHeader eyebrow="Məzmun studiyası" title={meta.title} description={meta.description} actions={<Button onClick={openCreate}><Plus className="size-4" /> Yeni {meta.singular}</Button>} />
      <ErrorBanner error={query.error} />
      <div className="relative mb-4 max-w-md"><Search className="pointer-events-none absolute top-2.5 left-3 size-4 text-muted-foreground" /><Input className="pl-9" placeholder="Axtar..." value={search} onChange={(event) => setSearch(event.target.value)} /></div>
      <DataTable rows={rows} loading={query.isLoading} rowKey={(row) => String(row.id)} columns={[
        { key: "title", label: isFaq ? "Sual" : "Vəzifə" },
        { key: "body", label: isFaq ? "Cavab" : "Açıqlama", className: "max-w-md truncate" },
        ...(!isFaq ? [{ key: "location", label: "Məkan", render: (row: Row) => String(row.data?.location ?? "-") }] : []),
        { key: "published", label: "Yayımda", render: (row) => <BoolBadge value={row.published} /> },
        { key: "sortOrder", label: "Sıra" },
      ]} rowActions={(row) => <div className="flex justify-end gap-1"><Button variant="ghost" size="icon" onClick={() => openEdit(row)} aria-label="Redaktə et"><Pencil className="size-4" /></Button><Button variant="ghost" size="icon" onClick={() => setDeleting(row)} aria-label="Sil"><Trash2 className="size-4 text-destructive" /></Button></div>} />

      <Dialog open={formOpen} onOpenChange={setFormOpen}><DialogContent className="max-h-[90vh] overflow-y-auto sm:max-w-2xl"><DialogHeader><DialogTitle>{editing ? "Redaktə et" : `Yeni ${meta.singular}`}</DialogTitle><DialogDescription>Yalnız yayımlanan məzmun ictimai saytda görünür.</DialogDescription></DialogHeader>
        <form className="space-y-4" onSubmit={(event) => { event.preventDefault(); saveMutation.mutate(); }}>
          <div className="space-y-1.5"><Label>{isFaq ? "Sual" : "Vəzifə adı"} *</Label><Input required value={form.title} onChange={(event) => setForm((current) => ({ ...current, title: event.target.value, key: editing || current.key ? current.key : `${meta.prefix}.${slugify(event.target.value)}` }))} /></div>
          <div className="space-y-1.5"><Label>{isFaq ? "Cavab" : "Ətraflı açıqlama"} *</Label><Textarea required rows={7} value={form.body} onChange={(event) => setForm((current) => ({ ...current, body: event.target.value }))} /></div>
          {!isFaq && <div className="grid gap-4 sm:grid-cols-3"><div className="space-y-1.5"><Label>Şöbə</Label><Input value={form.department} onChange={(event) => setForm((current) => ({ ...current, department: event.target.value }))} /></div><div className="space-y-1.5"><Label>Məkan</Label><Input value={form.location} onChange={(event) => setForm((current) => ({ ...current, location: event.target.value }))} /></div><div className="space-y-1.5"><Label>İş formatı</Label><Input value={form.employmentType} onChange={(event) => setForm((current) => ({ ...current, employmentType: event.target.value }))} /></div></div>}
          <div className="grid gap-4 sm:grid-cols-2"><div className="space-y-1.5"><Label>Sıra</Label><Input type="number" min="0" value={form.sortOrder} onChange={(event) => setForm((current) => ({ ...current, sortOrder: event.target.value }))} /></div><div className="flex items-center justify-between rounded-xl border p-3"><div><p className="text-sm font-medium">Yayımla</p><p className="text-xs text-muted-foreground">Saytda dərhal göstərilsin.</p></div><Switch checked={form.published} onCheckedChange={(published) => setForm((current) => ({ ...current, published }))} /></div></div>
          <DialogFooter><Button type="button" variant="outline" onClick={() => setFormOpen(false)}>Ləğv et</Button><Button type="submit" disabled={saveMutation.isPending}>Yadda saxla</Button></DialogFooter>
        </form></DialogContent></Dialog>
      <ConfirmDialog open={deleting !== null} onOpenChange={(open) => !open && setDeleting(null)} title="Məzmun silinsin?" description="Bu əməliyyat geri qaytarıla bilməz." pending={deleteMutation.isPending} onConfirm={() => deleting && deleteMutation.mutate(deleting.id)} />
    </div>
  );
}
