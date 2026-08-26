import { useEffect, useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  BarChart3,
  BookOpenCheck,
  ExternalLink,
  Film,
  Globe2,
  GripVertical,
  ImageIcon,
  LayoutTemplate,
  Loader2,
  Plus,
  Route,
  Save,
  Settings2,
  Sparkles,
  UsersRound,
} from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/page-header";
import { ErrorBanner } from "@/components/error-banner";
import { ImageUploadField } from "@/components/image-upload";
import { MediaUploadField } from "@/components/media-upload";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Skeleton } from "@/components/ui/skeleton";
import { Switch } from "@/components/ui/switch";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Textarea } from "@/components/ui/textarea";
import { api } from "@/lib/api";
import { ApiError } from "@/lib/api-error";
import type { PagedResult } from "@/types/common";
import { ArrayItemActions, EditorCard, NumberBadge } from "@/features/homepage/components/editor-card";
import {
  DEFAULT_HOMEPAGE,
  DEFAULT_SITE_SETTINGS,
  normalizeHomepage,
  normalizeSiteSettings,
} from "@/features/homepage/defaults";
import type {
  CmsContentDocument,
  CourseOption,
  HomeMetric,
  HomeService,
  HomepageData,
  RoadmapItem,
  SiteSettingsData,
  SocialLink,
  TeamMember,
} from "@/features/homepage/types";

const HOME_KEY = "page.home";
const SETTINGS_KEY = "page.site-settings";

function moveItem<T>(items: T[], from: number, to: number): T[] {
  if (to < 0 || to >= items.length) return items;
  const next = [...items];
  const [item] = next.splice(from, 1);
  next.splice(to, 0, item);
  return next;
}

function text(value: unknown): string {
  return typeof value === "string" ? value : "";
}

export function HomepagePage() {
  const queryClient = useQueryClient();
  const [initialized, setInitialized] = useState(false);
  const [homeDocument, setHomeDocument] = useState<CmsContentDocument | null>(null);
  const [settingsDocument, setSettingsDocument] = useState<CmsContentDocument | null>(null);
  const [home, setHome] = useState<HomepageData>(() => structuredClone(DEFAULT_HOMEPAGE));
  const [settings, setSettings] = useState<SiteSettingsData>(() => structuredClone(DEFAULT_SITE_SETTINGS));
  const [homePublished, setHomePublished] = useState(true);
  const [settingsPublished, setSettingsPublished] = useState(true);

  const cmsQuery = useQuery({
    queryKey: ["homepage-cms-documents"],
    queryFn: () => api.get<CmsContentDocument[]>("/api/v1/content/cms-content"),
  });

  const coursesQuery = useQuery({
    queryKey: ["homepage-course-options"],
    queryFn: () => api.get<PagedResult<CourseOption>>("/api/v1/courses", { page: 0, size: 100, sort: "title,asc" }),
  });

  useEffect(() => {
    if (initialized || !cmsQuery.data) return;
    const existingHome = cmsQuery.data.find((item) => item.key === HOME_KEY) ?? null;
    const existingSettings = cmsQuery.data.find((item) => item.key === SETTINGS_KEY) ?? null;
    setHomeDocument(existingHome);
    setSettingsDocument(existingSettings);
    setHome(normalizeHomepage(existingHome?.data));
    setSettings(normalizeSiteSettings(existingSettings?.data));
    setHomePublished(existingHome?.published ?? true);
    setSettingsPublished(existingSettings?.published ?? true);
    setInitialized(true);
  }, [cmsQuery.data, initialized]);

  const availableCourses = useMemo(
    () => (coursesQuery.data?.content ?? []).filter((course) => course.published && course.active),
    [coursesQuery.data],
  );
  const courseById = useMemo(
    () => new Map((coursesQuery.data?.content ?? []).map((course) => [course.id, course])),
    [coursesQuery.data],
  );

  async function upsertDocument(
    document: CmsContentDocument | null,
    key: string,
    title: string,
    body: string,
    data: Record<string, unknown>,
    published: boolean,
    sortOrder: number,
  ) {
    if (document) {
      return api.patch<CmsContentDocument>(`/api/v1/content/cms-content/${document.id}`, {
        title,
        body,
        data,
        published,
        sortOrder,
      });
    }
    return api.post<CmsContentDocument>("/api/v1/content/cms-content", {
      key,
      type: "PAGE",
      title,
      body,
      data,
      published,
      sortOrder,
    });
  }

  const saveMutation = useMutation({
    mutationFn: async () => {
      const [savedHome, savedSettings] = await Promise.all([
        upsertDocument(
          homeDocument,
          HOME_KEY,
          "Nexora Academy — Ana səhifə",
          "Ana səhifənin idarə olunan məzmunu.",
          home,
          homePublished,
          0,
        ),
        upsertDocument(
          settingsDocument,
          SETTINGS_KEY,
          "Sayt ayarları",
          "Bütün ictimai səhifələrdə istifadə olunan əlaqə və sosial media məlumatları.",
          settings,
          settingsPublished,
          1,
        ),
      ]);
      return { savedHome, savedSettings };
    },
    onSuccess: ({ savedHome, savedSettings }) => {
      setHomeDocument(savedHome);
      setSettingsDocument(savedSettings);
      void queryClient.invalidateQueries({ queryKey: ["homepage-cms-documents"] });
      toast.success("Ana səhifə məzmunu yadda saxlanıldı.");
    },
    onError: (error) => {
      toast.error(error instanceof ApiError ? error.message : "Məzmun yadda saxlanıla bilmədi.");
    },
  });

  if (cmsQuery.isLoading || !initialized) {
    return (
      <div className="space-y-5">
        <Skeleton className="h-20 w-full" />
        <Skeleton className="h-12 w-full" />
        <Skeleton className="h-96 w-full" />
      </div>
    );
  }

  return (
    <div className="pb-10">
      <PageHeader
        eyebrow="Məzmun studiyası"
        title="Ana səhifə"
        description="Nexora Academy-nin ictimai ana səhifəsində görünən məzmunu bir yerdən idarə edin."
        actions={
          <>
            <Button
              variant="outline"
              nativeButton={false}
              render={<a href="/" target="_blank" rel="noreferrer" />}
            >
              <ExternalLink className="size-4" />
              Saytı aç
            </Button>
            <Button onClick={() => saveMutation.mutate()} disabled={saveMutation.isPending}>
              {saveMutation.isPending ? <Loader2 className="size-4 animate-spin" /> : <Save className="size-4" />}
              Yadda saxla
            </Button>
          </>
        }
      />

      <ErrorBanner error={cmsQuery.error ?? coursesQuery.error} />

      <div className="mb-6 grid gap-3 sm:grid-cols-3">
        <StatusTile label="Ana səhifə" enabled={homePublished} onChange={setHomePublished} />
        <StatusTile label="Sayt ayarları" enabled={settingsPublished} onChange={setSettingsPublished} />
        <div className="rounded-2xl border border-primary/15 bg-gradient-to-br from-sky-500/10 via-primary/5 to-indigo-500/10 p-4">
          <p className="text-xs font-semibold tracking-wide text-primary uppercase">Son vəziyyət</p>
          <p className="mt-2 text-sm font-medium">{homeDocument?.updatedAt ? new Date(homeDocument.updatedAt).toLocaleString("az-AZ") : "İlk dəfə yaradılacaq"}</p>
          <p className="mt-1 text-xs text-muted-foreground">Yalnız yayımlanan sənədlər ictimai API-də görünür.</p>
        </div>
      </div>

      <Tabs defaultValue="hero" className="gap-5">
        <div className="overflow-x-auto pb-1">
          <TabsList className="h-auto min-w-max rounded-xl border border-border/70 bg-card/80 p-1 shadow-sm">
            <TabsTrigger value="hero" className="px-3 py-2"><Sparkles /> Hero</TabsTrigger>
            <TabsTrigger value="stats" className="px-3 py-2"><BarChart3 /> Göstəricilər</TabsTrigger>
            <TabsTrigger value="services" className="px-3 py-2"><LayoutTemplate /> Xidmətlər</TabsTrigger>
            <TabsTrigger value="team" className="px-3 py-2"><UsersRound /> Komanda</TabsTrigger>
            <TabsTrigger value="roadmap" className="px-3 py-2"><Route /> Roadmap</TabsTrigger>
            <TabsTrigger value="courses" className="px-3 py-2"><BookOpenCheck /> Kurslar</TabsTrigger>
            <TabsTrigger value="sections" className="px-3 py-2"><Settings2 /> Bölmələr</TabsTrigger>
            <TabsTrigger value="site" className="px-3 py-2"><Globe2 /> Sayt ayarları</TabsTrigger>
          </TabsList>
        </div>

        <TabsContent value="hero">
          <EditorCard icon={Film} title="Hero sahəsi" description="İlk ekranda görünən başlıq, video və poster.">
            <div className="grid gap-5 lg:grid-cols-[1fr_1.1fr]">
              <div className="space-y-4">
                <TextField label="Başlığın ilk hissəsi" value={home.hero.titleLead} onChange={(value) => setHome((current) => ({ ...current, hero: { ...current.hero, titleLead: value } }))} />
                <TextField label="Başlığın vurğulanan hissəsi" value={home.hero.titleAccent} onChange={(value) => setHome((current) => ({ ...current, hero: { ...current.hero, titleAccent: value } }))} />
                <TextField label="Video URL/path" value={home.hero.videoUrl} onChange={(value) => setHome((current) => ({ ...current, hero: { ...current.hero, videoUrl: value } }))} />
                <MediaUploadField id="homepage-video" value={home.hero.videoUrl} onChange={(value) => setHome((current) => ({ ...current, hero: { ...current.hero, videoUrl: value } }))} />
              </div>
              <div className="space-y-3">
                <Label>Video poster şəkli</Label>
                <ImageUploadField id="homepage-poster" value={home.hero.posterUrl} onChange={(value) => setHome((current) => ({ ...current, hero: { ...current.hero, posterUrl: value } }))} placeholder="Poster seçilməzsə video özü göstəriləcək." />
              </div>
            </div>
          </EditorCard>
        </TabsContent>

        <TabsContent value="stats">
          <EditorCard
            icon={BarChart3}
            title="Rəqəmsal göstəricilər"
            description="Dəyər marketinq formatında yazıla bilər: 100+, 3 il, 98%."
            actions={<Button size="sm" variant="outline" onClick={() => setHome((current) => ({ ...current, stats: [...current.stats, { value: "0+", label: "Yeni göstərici" }] }))}><Plus className="size-4" /> Əlavə et</Button>}
          >
            <div className="grid gap-3 md:grid-cols-2">
              {home.stats.map((metric, index) => (
                <MetricEditor
                  key={index}
                  metric={metric}
                  index={index}
                  length={home.stats.length}
                  onChange={(next) => setHome((current) => ({ ...current, stats: current.stats.map((item, itemIndex) => itemIndex === index ? next : item) }))}
                  onMove={(from, to) => setHome((current) => ({ ...current, stats: moveItem(current.stats, from, to) }))}
                  onRemove={() => setHome((current) => ({ ...current, stats: current.stats.filter((_, itemIndex) => itemIndex !== index) }))}
                />
              ))}
            </div>
          </EditorCard>
        </TabsContent>

        <TabsContent value="services" className="space-y-5">
          <EditorCard icon={LayoutTemplate} title="Bölmə başlığı">
            <div className="grid gap-4 md:grid-cols-3">
              <TextField label="Başlanğıc" value={home.services.titleLead} onChange={(value) => setHome((current) => ({ ...current, services: { ...current.services, titleLead: value } }))} />
              <TextField label="Vurğulanan hissə" value={home.services.titleAccent} onChange={(value) => setHome((current) => ({ ...current, services: { ...current.services, titleAccent: value } }))} />
              <TextAreaField label="Davamı" value={home.services.titleTail} onChange={(value) => setHome((current) => ({ ...current, services: { ...current.services, titleTail: value } }))} />
            </div>
          </EditorCard>
          <EditorCard
            icon={ImageIcon}
            title="Xidmət kartları"
            description="Kartlar saxlanılan sıra ilə göstərilir."
            actions={<Button size="sm" variant="outline" onClick={() => setHome((current) => ({ ...current, services: { ...current.services, items: [...current.services.items, { title: "Yeni xidmət", description: "", imageUrl: "" }] } }))}><Plus className="size-4" /> Kart əlavə et</Button>}
          >
            <div className="space-y-4">
              {home.services.items.map((service, index) => (
                <ServiceEditor
                  key={index}
                  service={service}
                  index={index}
                  length={home.services.items.length}
                  onChange={(next) => setHome((current) => ({ ...current, services: { ...current.services, items: current.services.items.map((item, itemIndex) => itemIndex === index ? next : item) } }))}
                  onMove={(from, to) => setHome((current) => ({ ...current, services: { ...current.services, items: moveItem(current.services.items, from, to) } }))}
                  onRemove={() => setHome((current) => ({ ...current, services: { ...current.services, items: current.services.items.filter((_, itemIndex) => itemIndex !== index) } }))}
                />
              ))}
            </div>
          </EditorCard>
        </TabsContent>

        <TabsContent value="team" className="space-y-5">
          <EditorCard icon={Sparkles} title="Haqqımızda mətni">
            <div className="grid gap-4 md:grid-cols-2">
              <TextField label="Başlıq" value={home.about.titleLead} onChange={(value) => setHome((current) => ({ ...current, about: { ...current.about, titleLead: value } }))} />
              <TextField label="Başlığın davamı" value={home.about.titleAccent} onChange={(value) => setHome((current) => ({ ...current, about: { ...current.about, titleAccent: value } }))} />
              <div className="md:col-span-2"><TextAreaField label="Açıqlama" value={home.about.description} onChange={(value) => setHome((current) => ({ ...current, about: { ...current.about, description: value } }))} rows={4} /></div>
              {home.about.highlights.map((metric, index) => (
                <MetricEditor
                  key={index}
                  metric={metric}
                  index={index}
                  length={home.about.highlights.length}
                  onChange={(next) => setHome((current) => ({ ...current, about: { ...current.about, highlights: current.about.highlights.map((item, itemIndex) => itemIndex === index ? next : item) } }))}
                  onMove={(from, to) => setHome((current) => ({ ...current, about: { ...current.about, highlights: moveItem(current.about.highlights, from, to) } }))}
                  onRemove={() => setHome((current) => ({ ...current, about: { ...current.about, highlights: current.about.highlights.filter((_, itemIndex) => itemIndex !== index) } }))}
                />
              ))}
            </div>
          </EditorCard>
          <EditorCard
            icon={UsersRound}
            title="Komanda üzvləri"
            description={`${home.about.team.length} üzv — şəkillər, adlar və rollar göstərilən sıra ilə yayımlanır.`}
            actions={<Button size="sm" variant="outline" onClick={() => setHome((current) => ({ ...current, about: { ...current.about, team: [...current.about.team, { name: "Yeni üzv", role: "", imageUrl: "" }] } }))}><Plus className="size-4" /> Üzv əlavə et</Button>}
          >
            <div className="grid gap-4 xl:grid-cols-2">
              {home.about.team.map((member, index) => (
                <TeamEditor
                  key={index}
                  member={member}
                  index={index}
                  length={home.about.team.length}
                  onChange={(next) => setHome((current) => ({ ...current, about: { ...current.about, team: current.about.team.map((item, itemIndex) => itemIndex === index ? next : item) } }))}
                  onMove={(from, to) => setHome((current) => ({ ...current, about: { ...current.about, team: moveItem(current.about.team, from, to) } }))}
                  onRemove={() => setHome((current) => ({ ...current, about: { ...current.about, team: current.about.team.filter((_, itemIndex) => itemIndex !== index) } }))}
                />
              ))}
            </div>
          </EditorCard>
        </TabsContent>

        <TabsContent value="roadmap">
          <EditorCard icon={Route} title="Tələbə yol xəritəsi" description="Vizual animasiya sabit qalır; burada mətnlər və ardıcıllıq idarə olunur.">
            <div className="mb-6 grid gap-4 md:grid-cols-3">
              <TextField label="Üst mətn" value={home.roadmap.eyebrow} onChange={(value) => setHome((current) => ({ ...current, roadmap: { ...current.roadmap, eyebrow: value } }))} />
              <TextField label="Vurğulanan başlıq" value={home.roadmap.title} onChange={(value) => setHome((current) => ({ ...current, roadmap: { ...current.roadmap, title: value } }))} />
              <TextAreaField label="Açıqlama" value={home.roadmap.description} onChange={(value) => setHome((current) => ({ ...current, roadmap: { ...current.roadmap, description: value } }))} />
            </div>
            <div className="space-y-3">
              {home.roadmap.items.map((item, index) => (
                <RoadmapEditor
                  key={index}
                  item={item}
                  index={index}
                  length={home.roadmap.items.length}
                  onChange={(next) => setHome((current) => ({ ...current, roadmap: { ...current.roadmap, items: current.roadmap.items.map((entry, itemIndex) => itemIndex === index ? next : entry) } }))}
                  onMove={(from, to) => setHome((current) => ({ ...current, roadmap: { ...current.roadmap, items: moveItem(current.roadmap.items, from, to) } }))}
                  onRemove={() => setHome((current) => ({ ...current, roadmap: { ...current.roadmap, items: current.roadmap.items.filter((_, itemIndex) => itemIndex !== index) } }))}
                />
              ))}
              <Button type="button" variant="outline" className="w-full border-dashed" onClick={() => setHome((current) => ({ ...current, roadmap: { ...current.roadmap, items: [...current.roadmap.items, { title: "Yeni mərhələ", description: "" }] } }))}>
                <Plus className="size-4" /> Mərhələ əlavə et
              </Button>
            </div>
          </EditorCard>
        </TabsContent>

        <TabsContent value="courses">
          <EditorCard icon={BookOpenCheck} title="Seçilmiş kurslar" description="Maksimum üç kurs ana səhifədə, burada göstərilən sıra ilə çıxacaq.">
            <div className="grid gap-5 lg:grid-cols-[1fr_1.2fr]">
              <div className="space-y-3">
                <p className="text-sm font-medium">Seçilənlər ({home.featuredCourseIds.length}/3)</p>
                {home.featuredCourseIds.length === 0 && <EmptyHint text="Kurs seçilməsə, sistem son yenilənən üç aktiv kursu göstərir." />}
                {home.featuredCourseIds.map((courseId, index) => {
                  const course = courseById.get(courseId);
                  return (
                    <div key={courseId} className="flex items-center gap-3 rounded-xl border bg-background/60 p-3">
                      <NumberBadge value={index + 1} />
                      <div className="min-w-0 flex-1">
                        <p className="truncate font-medium">{course?.title ?? courseId}</p>
                        <p className="truncate text-xs text-muted-foreground">{course?.slug ?? "Kurs artıq siyahıda tapılmır"}</p>
                      </div>
                      <ArrayItemActions
                        index={index}
                        length={home.featuredCourseIds.length}
                        onMove={(from, to) => setHome((current) => ({ ...current, featuredCourseIds: moveItem(current.featuredCourseIds, from, to) }))}
                        onRemove={() => setHome((current) => ({ ...current, featuredCourseIds: current.featuredCourseIds.filter((id) => id !== courseId) }))}
                      />
                    </div>
                  );
                })}
              </div>
              <div className="space-y-3">
                <p className="text-sm font-medium">Aktiv və yayımlanmış kurslar</p>
                {coursesQuery.isLoading ? <Skeleton className="h-40" /> : (
                  <div className="max-h-[26rem] space-y-2 overflow-y-auto rounded-xl border p-2">
                    {availableCourses.map((course) => {
                      const selected = home.featuredCourseIds.includes(course.id);
                      return (
                        <button
                          key={course.id}
                          type="button"
                          disabled={!selected && home.featuredCourseIds.length >= 3}
                          onClick={() => setHome((current) => ({
                            ...current,
                            featuredCourseIds: selected
                              ? current.featuredCourseIds.filter((id) => id !== course.id)
                              : [...current.featuredCourseIds, course.id].slice(0, 3),
                          }))}
                          className="flex w-full items-center justify-between gap-3 rounded-lg border border-transparent px-3 py-2 text-left transition hover:border-primary/20 hover:bg-primary/5 disabled:cursor-not-allowed disabled:opacity-45"
                        >
                          <span className="min-w-0"><span className="block truncate text-sm font-medium">{course.title}</span><span className="block truncate text-xs text-muted-foreground">{course.slug}</span></span>
                          <Badge variant={selected ? "default" : "outline"}>{selected ? "Seçilib" : "Seç"}</Badge>
                        </button>
                      );
                    })}
                  </div>
                )}
              </div>
            </div>
          </EditorCard>
        </TabsContent>

        <TabsContent value="sections">
          <EditorCard icon={Settings2} title="Bölmə başlıqları" description="Ana səhifədəki əsas CTA və kontent bloklarının başlıqları.">
            <div className="grid gap-4 md:grid-cols-2">
              <TextField label="Kurslar — başlanğıc" value={home.sections.coursesTitleLead} onChange={(value) => updateSection(setHome, "coursesTitleLead", value)} />
              <TextField label="Kurslar — vurğu" value={home.sections.coursesTitleAccent} onChange={(value) => updateSection(setHome, "coursesTitleAccent", value)} />
              <TextField label="Xəbərlər — başlanğıc" value={home.sections.newsTitleLead} onChange={(value) => updateSection(setHome, "newsTitleLead", value)} />
              <TextField label="Xəbərlər — vurğu" value={home.sections.newsTitleAccent} onChange={(value) => updateSection(setHome, "newsTitleAccent", value)} />
              <TextField label="Başvuru — başlanğıc" value={home.sections.applicationTitleLead} onChange={(value) => updateSection(setHome, "applicationTitleLead", value)} />
              <TextField label="Başvuru — vurğu" value={home.sections.applicationTitleAccent} onChange={(value) => updateSection(setHome, "applicationTitleAccent", value)} />
              <div className="md:col-span-2"><TextAreaField label="Newsletter başlığı" value={home.sections.newsletterTitle} onChange={(value) => updateSection(setHome, "newsletterTitle", value)} /></div>
            </div>
          </EditorCard>
        </TabsContent>

        <TabsContent value="site">
          <EditorCard icon={Globe2} title="Əlaqə və sosial media" description="Bu məlumatlar footer olan bütün ictimai səhifələrə tətbiq olunur.">
            <div className="grid gap-4 md:grid-cols-2">
              <TextField label="Ünvan" value={settings.address} onChange={(value) => setSettings((current) => ({ ...current, address: value }))} />
              <TextField label="Xəritə linki" value={settings.addressUrl} onChange={(value) => setSettings((current) => ({ ...current, addressUrl: value }))} />
              <TextField label="Telefon" value={settings.phone} onChange={(value) => setSettings((current) => ({ ...current, phone: value }))} />
              <TextField label="E-poçt" value={settings.email} onChange={(value) => setSettings((current) => ({ ...current, email: value }))} />
            </div>
            <div className="mt-7 flex items-center justify-between">
              <div><p className="font-medium">Sosial media keçidləri</p><p className="text-xs text-muted-foreground">Sıralama footer-dəki sıranı müəyyən edir.</p></div>
              <Button size="sm" variant="outline" onClick={() => setSettings((current) => ({ ...current, socials: [...current.socials, { label: "Yeni keçid", url: "https://" }] }))}><Plus className="size-4" /> Əlavə et</Button>
            </div>
            <div className="mt-3 space-y-3">
              {settings.socials.map((social, index) => (
                <SocialEditor
                  key={index}
                  social={social}
                  index={index}
                  length={settings.socials.length}
                  onChange={(next) => setSettings((current) => ({ ...current, socials: current.socials.map((item, itemIndex) => itemIndex === index ? next : item) }))}
                  onMove={(from, to) => setSettings((current) => ({ ...current, socials: moveItem(current.socials, from, to) }))}
                  onRemove={() => setSettings((current) => ({ ...current, socials: current.socials.filter((_, itemIndex) => itemIndex !== index) }))}
                />
              ))}
            </div>
          </EditorCard>
        </TabsContent>
      </Tabs>
    </div>
  );
}

function updateSection(
  setter: React.Dispatch<React.SetStateAction<HomepageData>>,
  field: keyof HomepageData["sections"],
  value: string,
) {
  setter((current) => ({ ...current, sections: { ...current.sections, [field]: value } }));
}

function StatusTile({ label, enabled, onChange }: { label: string; enabled: boolean; onChange: (value: boolean) => void }) {
  return (
    <div className="flex items-center justify-between rounded-2xl border border-border/70 bg-card/90 p-4 shadow-sm">
      <div><p className="text-sm font-semibold">{label}</p><p className="mt-1 text-xs text-muted-foreground">{enabled ? "İctimai saytda yayımlanır" : "Draft olaraq saxlanır"}</p></div>
      <Switch checked={enabled} onCheckedChange={onChange} aria-label={`${label} yayın statusu`} />
    </div>
  );
}

function TextField({ label, value, onChange, placeholder }: { label: string; value: string; onChange: (value: string) => void; placeholder?: string }) {
  return <div className="space-y-1.5"><Label>{label}</Label><Input value={value} placeholder={placeholder} onChange={(event) => onChange(event.target.value)} /></div>;
}

function TextAreaField({ label, value, onChange, rows = 3 }: { label: string; value: string; onChange: (value: string) => void; rows?: number }) {
  return <div className="space-y-1.5"><Label>{label}</Label><Textarea value={value} rows={rows} onChange={(event) => onChange(event.target.value)} /></div>;
}

function MetricEditor({ metric, index, length, onChange, onMove, onRemove }: { metric: HomeMetric; index: number; length: number; onChange: (value: HomeMetric) => void; onMove: (from: number, to: number) => void; onRemove: () => void }) {
  return (
    <div className="rounded-xl border border-border/70 bg-background/60 p-4">
      <div className="mb-3 flex items-center justify-between"><NumberBadge value={index + 1} /><ArrayItemActions index={index} length={length} onMove={onMove} onRemove={onRemove} /></div>
      <div className="grid gap-3 sm:grid-cols-[0.7fr_1.3fr]"><TextField label="Dəyər" value={text(metric.value)} onChange={(value) => onChange({ ...metric, value })} /><TextField label="Başlıq" value={text(metric.label)} onChange={(value) => onChange({ ...metric, label: value })} /></div>
    </div>
  );
}

function ServiceEditor({ service, index, length, onChange, onMove, onRemove }: { service: HomeService; index: number; length: number; onChange: (value: HomeService) => void; onMove: (from: number, to: number) => void; onRemove: () => void }) {
  return (
    <div className="rounded-xl border border-border/70 bg-background/60 p-4">
      <div className="mb-4 flex items-center justify-between"><div className="flex items-center gap-2"><GripVertical className="size-4 text-muted-foreground" /><span className="font-medium">Kart {index + 1}</span></div><ArrayItemActions index={index} length={length} onMove={onMove} onRemove={onRemove} /></div>
      <div className="grid gap-4 lg:grid-cols-[1.3fr_0.7fr]"><div className="space-y-4"><TextField label="Başlıq" value={text(service.title)} onChange={(value) => onChange({ ...service, title: value })} /><TextAreaField label="Açıqlama" value={text(service.description)} onChange={(value) => onChange({ ...service, description: value })} rows={4} /></div><div className="space-y-1.5"><Label>Şəkil</Label><ImageUploadField id={`service-image-${index}`} value={text(service.imageUrl)} onChange={(value) => onChange({ ...service, imageUrl: value })} /></div></div>
    </div>
  );
}

function TeamEditor({ member, index, length, onChange, onMove, onRemove }: { member: TeamMember; index: number; length: number; onChange: (value: TeamMember) => void; onMove: (from: number, to: number) => void; onRemove: () => void }) {
  return (
    <div className="rounded-xl border border-border/70 bg-background/60 p-4">
      <div className="mb-3 flex items-center justify-between"><div className="flex items-center gap-2"><NumberBadge value={index + 1} /><span className="truncate text-sm font-medium">{member.name || "Yeni üzv"}</span></div><ArrayItemActions index={index} length={length} onMove={onMove} onRemove={onRemove} /></div>
      <div className="grid gap-4 sm:grid-cols-[1fr_10rem]"><div className="space-y-3"><TextField label="Ad və soyad" value={text(member.name)} onChange={(value) => onChange({ ...member, name: value })} /><TextField label="Vəzifə" value={text(member.role)} onChange={(value) => onChange({ ...member, role: value })} /></div><div className="space-y-1.5"><Label>Portret</Label><ImageUploadField id={`team-image-${index}`} value={text(member.imageUrl)} onChange={(value) => onChange({ ...member, imageUrl: value })} /></div></div>
    </div>
  );
}

function RoadmapEditor({ item, index, length, onChange, onMove, onRemove }: { item: RoadmapItem; index: number; length: number; onChange: (value: RoadmapItem) => void; onMove: (from: number, to: number) => void; onRemove: () => void }) {
  return (
    <div className="grid items-end gap-3 rounded-xl border border-border/70 bg-background/60 p-3 sm:grid-cols-[auto_0.8fr_1.2fr_auto]">
      <NumberBadge value={index + 1} />
      <TextField label="Mərhələ" value={text(item.title)} onChange={(value) => onChange({ ...item, title: value })} />
      <TextField label="Açıqlama" value={text(item.description)} onChange={(value) => onChange({ ...item, description: value })} />
      <ArrayItemActions index={index} length={length} onMove={onMove} onRemove={onRemove} />
    </div>
  );
}

function SocialEditor({ social, index, length, onChange, onMove, onRemove }: { social: SocialLink; index: number; length: number; onChange: (value: SocialLink) => void; onMove: (from: number, to: number) => void; onRemove: () => void }) {
  return (
    <div className="grid items-end gap-3 rounded-xl border border-border/70 bg-background/60 p-3 sm:grid-cols-[0.7fr_1.3fr_auto]">
      <TextField label="Ad" value={text(social.label)} onChange={(value) => onChange({ ...social, label: value })} />
      <TextField label="URL" value={text(social.url)} onChange={(value) => onChange({ ...social, url: value })} />
      <ArrayItemActions index={index} length={length} onMove={onMove} onRemove={onRemove} />
    </div>
  );
}

function EmptyHint({ text: message }: { text: string }) {
  return <div className="rounded-xl border border-dashed border-primary/25 bg-primary/5 p-4 text-sm text-muted-foreground">{message}</div>;
}
