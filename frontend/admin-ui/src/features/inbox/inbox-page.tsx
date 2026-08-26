import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Download, FileText, Mail, MessageSquareText, Search, Trash2, User, Phone, Calendar, Eye } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/page-header";
import { ErrorBanner } from "@/components/error-banner";
import { DataTable } from "@/components/data-table";
import { ConfirmDialog } from "@/components/confirm-dialog";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { adminApiUrl, api } from "@/lib/api";
import { ApiError } from "@/lib/api-error";
import { formatDateTime, StatusBadge } from "@/lib/format";

interface ApplicationRow extends Record<string, unknown> { id: number; applicationType: number; fullname: string; email: string; phone: string; letter: string; cvFilename?: string | null; status: string; createdAt: string }
interface ContactRow extends Record<string, unknown> { id: string; fullName?: string | null; email?: string | null; phone?: string | null; message?: string | null; status: string; submittedAt: string }
interface SubscriberRow extends Record<string, unknown> { id: string; email: string; consentVersion: string; active: boolean; subscribedAt: string }

export function InboxPage() {
  const queryClient = useQueryClient();
  const [search, setSearch] = useState("");
  const [deleteTarget, setDeleteTarget] = useState<{ kind: "application" | "contact" | "subscriber"; id: string } | null>(null);
  const [detailTarget, setDetailTarget] = useState<{ kind: "application" | "contact"; row: ApplicationRow | ContactRow } | null>(null);
  const applications = useQuery({ queryKey: ["inbox-applications"], queryFn: () => api.get<ApplicationRow[]>("/api/v1/applications") });
  const contacts = useQuery({ queryKey: ["inbox-contacts"], queryFn: () => api.get<ContactRow[]>("/api/v1/contact-submissions") });
  const subscribers = useQuery({ queryKey: ["inbox-subscribers"], queryFn: () => api.get<SubscriberRow[]>("/api/v1/newsletter-subscribers") });

  const invalidate = () => Promise.all([
    queryClient.invalidateQueries({ queryKey: ["inbox-applications"] }),
    queryClient.invalidateQueries({ queryKey: ["inbox-contacts"] }),
    queryClient.invalidateQueries({ queryKey: ["inbox-subscribers"] }),
    queryClient.invalidateQueries({ queryKey: ["dashboard-summary"] }),
  ]);
  const statusMutation = useMutation({
    mutationFn: ({ kind, id, status }: { kind: "application" | "contact"; id: string; status: string }) => kind === "application" ? api.patch(`/api/v1/applications/${id}/status`, { status }) : api.patch(`/api/v1/contact-submissions/${id}/status`, { status }),
    onSuccess: () => { void invalidate(); toast.success("Status yeniləndi."); },
    onError: (error) => toast.error(error instanceof ApiError ? error.message : "Status yenilənmədi."),
  });
  const deleteMutation = useMutation({
    mutationFn: (target: NonNullable<typeof deleteTarget>) => api.delete(target.kind === "application" ? `/api/v1/applications/${target.id}` : target.kind === "contact" ? `/api/v1/contact-submissions/${target.id}` : `/api/v1/newsletter-subscribers/${target.id}`),
    onSuccess: () => { setDeleteTarget(null); void invalidate(); toast.success("Qeyd silindi."); },
    onError: (error) => toast.error(error instanceof ApiError ? error.message : "Qeyd silinə bilmədi."),
  });

  const filter = <T extends Record<string, unknown>>(rows: T[] | undefined, keys: (keyof T)[]) => {
    const needle = search.trim().toLocaleLowerCase("az");
    if (!needle) return rows ?? [];
    return (rows ?? []).filter((row) => keys.some((key) => String(row[key] ?? "").toLocaleLowerCase("az").includes(needle)));
  };

  const pendingApplications = applications.data?.filter((row) => row.status === "PENDING").length ?? 0;
  const pendingContacts = contacts.data?.filter((row) => row.status === "pending").length ?? 0;

  return <div><PageHeader eyebrow="Gələnlər mərkəzi" title="Müraciətlər" description="Saytdan gələn başvurular, əlaqə mesajları və newsletter abunəçiləri." />
    <ErrorBanner error={applications.error ?? contacts.error ?? subscribers.error} />
    <div className="mb-6 grid gap-3 sm:grid-cols-3"><Summary icon={FileText} label="Yeni başvuru" value={pendingApplications} /><Summary icon={MessageSquareText} label="Yeni mesaj" value={pendingContacts} /><Summary icon={Mail} label="Aktiv abunəçi" value={subscribers.data?.filter((row) => row.active).length ?? 0} /></div>
    <Tabs defaultValue="applications" className="gap-5">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between"><TabsList className="h-auto rounded-xl border bg-card p-1"><TabsTrigger value="applications">Başvurular</TabsTrigger><TabsTrigger value="contacts">Əlaqə mesajları</TabsTrigger><TabsTrigger value="subscribers">Abunəçilər</TabsTrigger></TabsList><div className="relative w-full sm:max-w-sm"><Search className="pointer-events-none absolute top-2.5 left-3 size-4 text-muted-foreground" /><Input className="pl-9" placeholder="Ad və ya e-poçt axtar..." value={search} onChange={(event) => setSearch(event.target.value)} /></div></div>
      <TabsContent value="applications"><DataTable rows={filter(applications.data, ["fullname", "email", "phone"])} loading={applications.isLoading} rowKey={(row) => String(row.id)} onRowClick={(row) => setDetailTarget({ kind: "application", row })} columns={[
        { key: "fullname", label: "Ad soyad" }, { key: "email", label: "E-poçt" }, { key: "phone", label: "Telefon" },
        { key: "letter", label: "Məktub", className: "max-w-xs truncate" }, { key: "cvFilename", label: "CV", render: (row) => row.cvFilename ? <a className="inline-flex items-center gap-1.5 font-medium text-primary hover:underline" href={adminApiUrl(`/api/v1/applications/${row.id}/cv`)} onClick={(e) => e.stopPropagation()}><Download className="size-3.5" />{row.cvFilename}</a> : "-" },
        { key: "status", label: "Status", render: (row) => <StatusSelect value={row.status} options={["PENDING", "REVIEWED", "SHORTLISTED", "REJECTED"]} onChange={(status) => statusMutation.mutate({ kind: "application", id: String(row.id), status })} /> },
        { key: "createdAt", label: "Tarix", render: (row) => formatDateTime(row.createdAt) },
      ]} rowActions={(row) => <DeleteButton onClick={() => setDeleteTarget({ kind: "application", id: String(row.id) })} />} /></TabsContent>
      <TabsContent value="contacts"><DataTable rows={filter(contacts.data, ["fullName", "email", "phone", "message"])} loading={contacts.isLoading} rowKey={(row) => row.id} onRowClick={(row) => setDetailTarget({ kind: "contact", row })} columns={[
        { key: "fullName", label: "Ad soyad" }, { key: "email", label: "E-poçt" }, { key: "phone", label: "Telefon" }, { key: "message", label: "Mesaj", className: "max-w-sm truncate" },
        { key: "status", label: "Status", render: (row) => <StatusSelect value={row.status} options={["pending", "in_progress", "resolved", "archived"]} onChange={(status) => statusMutation.mutate({ kind: "contact", id: row.id, status })} /> },
        { key: "submittedAt", label: "Tarix", render: (row) => formatDateTime(row.submittedAt) },
      ]} rowActions={(row) => <DeleteButton onClick={() => setDeleteTarget({ kind: "contact", id: row.id })} />} /></TabsContent>
      <TabsContent value="subscribers"><DataTable rows={filter(subscribers.data, ["email"])} loading={subscribers.isLoading} rowKey={(row) => row.id} columns={[{ key: "email", label: "E-poçt" }, { key: "consentVersion", label: "Razılıq versiyası" }, { key: "subscribedAt", label: "Abunə tarixi", render: (row) => formatDateTime(row.subscribedAt) }, { key: "active", label: "Status", render: (row) => <StatusBadge value={row.active ? "ACTIVE" : "INACTIVE"} /> }]} rowActions={(row) => <DeleteButton onClick={() => setDeleteTarget({ kind: "subscriber", id: row.id })} />} /></TabsContent>
    </Tabs>
    <ConfirmDialog open={deleteTarget !== null} onOpenChange={(open) => !open && setDeleteTarget(null)} title="Qeyd silinsin?" description="Bu əməliyyat geri qaytarıla bilməz." pending={deleteMutation.isPending} onConfirm={() => deleteTarget && deleteMutation.mutate(deleteTarget)} />
    <DetailDialog detail={detailTarget} onClose={() => setDetailTarget(null)} />
  </div>;
}

function DetailDialog({ detail, onClose }: { detail: { kind: "application" | "contact"; row: ApplicationRow | ContactRow } | null; onClose: () => void }) {
  if (!detail) return null;
  const { kind } = detail;
  return (
    <Dialog open onOpenChange={(open) => !open && onClose()}>
      <DialogContent className="sm:max-w-lg">
        <DialogHeader>
          <DialogTitle>{kind === "application" ? "Başvuru detalları" : "Mesaj detalları"}</DialogTitle>
        </DialogHeader>
        <div className="space-y-4">
          {kind === "application" && (() => {
            const row = detail.row as ApplicationRow;
            return <>
              <DetailRow icon={User} label="Ad soyad" value={row.fullname} />
              <DetailRow icon={Mail} label="E-poçt" value={row.email} />
              <DetailRow icon={Phone} label="Telefon" value={row.phone} />
              <DetailRow icon={Calendar} label="Tarix" value={formatDateTime(row.createdAt)} />
              <DetailRow icon={Eye} label="Status" value={row.status?.replaceAll("_", " ")} />
              <div>
                <p className="mb-1 text-xs font-medium text-muted-foreground">Məktub</p>
                <div className="max-h-48 overflow-y-auto whitespace-pre-wrap rounded-lg border bg-muted/30 p-3 text-sm leading-relaxed">{row.letter}</div>
              </div>
              {row.cvFilename && (
                <div>
                  <p className="mb-1 text-xs font-medium text-muted-foreground">CV</p>
                  <a className="inline-flex items-center gap-1.5 font-medium text-primary hover:underline" href={adminApiUrl(`/api/v1/applications/${row.id}/cv`)}><Download className="size-3.5" />{row.cvFilename}</a>
                </div>
              )}
            </>;
          })()}
          {kind === "contact" && (() => {
            const row = detail.row as ContactRow;
            return <>
              <DetailRow icon={User} label="Ad soyad" value={row.fullName} />
              <DetailRow icon={Mail} label="E-poçt" value={row.email} />
              <DetailRow icon={Phone} label="Telefon" value={row.phone} />
              <DetailRow icon={Calendar} label="Tarix" value={formatDateTime(row.submittedAt)} />
              <DetailRow icon={Eye} label="Status" value={row.status} />
              <div>
                <p className="mb-1 text-xs font-medium text-muted-foreground">Mesaj</p>
                <div className="max-h-48 overflow-y-auto whitespace-pre-wrap rounded-lg border bg-muted/30 p-3 text-sm leading-relaxed">{row.message}</div>
              </div>
            </>;
          })()}
        </div>
      </DialogContent>
    </Dialog>
  );
}

function DetailRow({ icon: Icon, label, value }: { icon: typeof User; label: string; value?: string | null }) {
  return (
    <div className="flex items-start gap-3">
      <div className="mt-0.5 flex size-7 shrink-0 items-center justify-center rounded-lg bg-muted/50 text-muted-foreground">
        <Icon className="size-3.5" />
      </div>
      <div className="min-w-0">
        <p className="text-xs text-muted-foreground">{label}</p>
        <p className="text-sm font-medium">{value || "-"}</p>
      </div>
    </div>
  );
}

function Summary({ icon: Icon, label, value }: { icon: typeof FileText; label: string; value: number }) { return <Card className="rounded-2xl"><CardContent className="flex items-center gap-4 p-5"><div className="flex size-11 items-center justify-center rounded-xl bg-primary/10 text-primary"><Icon className="size-5" /></div><div><p className="text-2xl font-bold">{value}</p><p className="text-xs text-muted-foreground">{label}</p></div></CardContent></Card>; }
function DeleteButton({ onClick }: { onClick: () => void }) { return <Button variant="ghost" size="icon" onClick={onClick} aria-label="Sil"><Trash2 className="size-4 text-destructive" /></Button>; }
function StatusSelect({ value, options, onChange }: { value: string; options: string[]; onChange: (value: string) => void }) { return <select aria-label="Status" className="h-8 rounded-lg border bg-background px-2 text-xs font-medium" value={value} onChange={(event) => onChange(event.target.value)}>{options.map((option) => <option key={option} value={option}>{option.replaceAll("_", " ")}</option>)}</select>; }
