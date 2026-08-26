import { useEffect, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { ExternalLink, Loader2, Save } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/page-header";
import { ErrorBanner } from "@/components/error-banner";
import { ImageUploadField } from "@/components/image-upload";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Skeleton } from "@/components/ui/skeleton";
import { Switch } from "@/components/ui/switch";
import { Textarea } from "@/components/ui/textarea";
import { api } from "@/lib/api";
import { ApiError } from "@/lib/api-error";
import type { CmsContentDocument } from "@/features/homepage/types";

const KEY = "page.about";

export function AboutPage() {
  const queryClient = useQueryClient();
  const [document, setDocument] = useState<CmsContentDocument | null>(null);
  const [title, setTitle] = useState("Nexora Academy haqqında");
  const [body, setBody] = useState("");
  const [imageUrl, setImageUrl] = useState("");
  const [mission, setMission] = useState("");
  const [graduates, setGraduates] = useState(0);
  const [employmentRate, setEmploymentRate] = useState(0);
  const [instructors, setInstructors] = useState(0);
  const [published, setPublished] = useState(true);
  const [ready, setReady] = useState(false);
  const query = useQuery({ queryKey: ["cms-about"], queryFn: () => api.get<CmsContentDocument[]>("/api/v1/content/cms-content") });

  useEffect(() => {
    if (ready || !query.data) return;
    const found = query.data.find((item) => item.key === KEY) ?? null;
    const stats = (found?.data?.stats && typeof found.data.stats === "object" ? found.data.stats : {}) as Record<string, unknown>;
    setDocument(found); setTitle(found?.title ?? "Nexora Academy haqqında"); setBody(found?.body ?? ""); setImageUrl(String(found?.data?.heroImage ?? "")); setMission(String(found?.data?.mission ?? "")); setGraduates(Number(stats.graduates) || 0); setEmploymentRate(Number(stats.employmentRate) || 0); setInstructors(Number(stats.instructors) || 0); setPublished(found?.published ?? true); setReady(true);
  }, [query.data, ready]);

  const saveMutation = useMutation({
    mutationFn: () => {
      const payload = { key: KEY, type: "PAGE", title: title.trim(), body: body.trim(), data: { ...(document?.data ?? {}), heroImage: imageUrl.trim(), mission: mission.trim(), stats: { graduates, employmentRate, instructors } }, published, sortOrder: 10 };
      return document ? api.patch<CmsContentDocument>(`/api/v1/content/cms-content/${document.id}`, payload) : api.post<CmsContentDocument>("/api/v1/content/cms-content", payload);
    },
    onSuccess: (saved) => { setDocument(saved); void queryClient.invalidateQueries({ queryKey: ["cms-about"] }); toast.success("Haqqımızda səhifəsi yadda saxlanıldı."); },
    onError: (error) => toast.error(error instanceof ApiError ? error.message : "Səhifə yadda saxlanıla bilmədi."),
  });

  if (query.isLoading || !ready) return <div className="space-y-4"><Skeleton className="h-24 rounded-2xl" /><Skeleton className="h-96 rounded-2xl" /></div>;
  return <div><PageHeader eyebrow="Məzmun studiyası" title="Haqqımızda" description="İctimai haqqımızda səhifəsinin əsas mətn və vizualını idarə edin." actions={<><Button variant="outline" render={<a href="/about-us.html" target="_blank" rel="noreferrer" />}><ExternalLink className="size-4" /> Saytı aç</Button><Button onClick={() => saveMutation.mutate()} disabled={saveMutation.isPending}>{saveMutation.isPending ? <Loader2 className="size-4 animate-spin" /> : <Save className="size-4" />} Yadda saxla</Button></>} /><ErrorBanner error={query.error} />
    <Card className="rounded-2xl"><CardHeader className="flex-row items-center justify-between"><CardTitle>Səhifə məzmunu</CardTitle><div className="flex items-center gap-2"><Label htmlFor="about-published" className="text-xs">Yayımda</Label><Switch id="about-published" checked={published} onCheckedChange={setPublished} /></div></CardHeader><CardContent className="grid gap-6 lg:grid-cols-[1fr_.85fr]"><div className="space-y-4"><div className="space-y-1.5"><Label>Başlıq</Label><Input value={title} onChange={(event) => setTitle(event.target.value)} /></div><div className="space-y-1.5"><Label>Əsas mətn</Label><Textarea rows={10} value={body} onChange={(event) => setBody(event.target.value)} /></div><div className="space-y-1.5"><Label>Missiyamız</Label><Textarea rows={5} value={mission} onChange={(event) => setMission(event.target.value)} /></div></div><div className="space-y-2"><Label>Əsas şəkil</Label><ImageUploadField id="about-main-image" value={imageUrl} onChange={setImageUrl} /></div></CardContent></Card>
    <Card className="rounded-2xl mt-4"><CardHeader><CardTitle>Sayt göstəriciləri</CardTitle></CardHeader><CardContent className="grid gap-4 md:grid-cols-3"><div className="space-y-1.5"><Label>Məzunlar (ədəd)</Label><Input type="number" min={0} value={graduates} onChange={(event) => setGraduates(Number(event.target.value) || 0)} /></div><div className="space-y-1.5"><Label>İşlə təminat faizi (%)</Label><Input type="number" min={0} max={100} value={employmentRate} onChange={(event) => setEmploymentRate(Number(event.target.value) || 0)} /></div><div className="space-y-1.5"><Label>Təlimçi sayı</Label><Input type="number" min={0} value={instructors} onChange={(event) => setInstructors(Number(event.target.value) || 0)} /></div></CardContent></Card>
  </div>;
}
