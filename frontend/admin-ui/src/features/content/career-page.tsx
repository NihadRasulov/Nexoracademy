import { useEffect, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  BriefcaseBusiness,
  ExternalLink,
  ImageIcon,
  Loader2,
  MapPinned,
  Save,
  Users,
} from "lucide-react";
import { Link } from "react-router-dom";
import { toast } from "sonner";
import { ErrorBanner } from "@/components/error-banner";
import { ImageUploadField } from "@/components/image-upload";
import { PageHeader } from "@/components/page-header";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Skeleton } from "@/components/ui/skeleton";
import { Switch } from "@/components/ui/switch";
import { Textarea } from "@/components/ui/textarea";
import { EditorCard, NumberBadge } from "@/features/homepage/components/editor-card";
import type { CmsContentDocument } from "@/features/homepage/types";
import { api } from "@/lib/api";
import { ApiError } from "@/lib/api-error";

const KEY = "page.career";

interface CareerHero {
  titleLead: string;
  titleAccent: string;
  titleTail: string;
  description: string;
  buttonLabel: string;
  buttonUrl: string;
  imageUrl: string;
}

interface CareerJourneyItem {
  title: string;
  description: string;
  imageUrl: string;
}

interface CareerJourney {
  titleLead: string;
  titleAccent: string;
  items: CareerJourneyItem[];
}

interface CareerIntro {
  titleLead: string;
  titleAccent: string;
  titleTail: string;
  paragraphs: string[];
  imageUrl: string;
}

interface CareerVacancies {
  title: string;
  description: string;
}

interface CareerPageData {
  hero: CareerHero;
  journey: CareerJourney;
  intro: CareerIntro;
  vacancies: CareerVacancies;
}

const DEFAULT_DATA: CareerPageData = {
  hero: {
    titleLead: "Karyera",
    titleAccent: "Nexora Academy",
    titleTail: "Komanda təcrübəsini, inkişaf imkanlarını və mövcud açıq vakansiyaları bir yerdə araşdırın.",
    description: "Uyğunluq, qüvvədəolma müddəti və tətbiq olunan kurs məlumatlarına əsasən imkanları müqayisə edin.",
    buttonLabel: "Araşdır",
    buttonUrl: "contact.html",
    imageUrl: "assets/career/karyera-hero-collage-wide-3rows.jpg",
  },
  journey: {
    titleLead: "Karyera",
    titleAccent: "yol xəritəniz",
    items: [
      {
        title: "İmkanları nəzərdən keçirin",
        description: "Açıq mövqeləri, iş formatını və gözləntiləri nəzərdən keçirin.",
        imageUrl: "assets/career/imkanlari-nezerden-kechirin-cv-transparent.png",
      },
      {
        title: "Məlumatları hazırlayın",
        description: "CV-nizi və qısa motivasiya məktubunuzu hazırlayın.",
        imageUrl: "assets/career/melumatlari-hazirlayin-blue-transparent.png",
      },
      {
        title: "Qərarı izləyin",
        description: "Uyğun vakansiya olduqda saytdakı müraciət formasından istifadə edin.",
        imageUrl: "assets/career/qerari-izleyin-briefcase-transparent.png",
      },
    ],
  },
  intro: {
    titleLead: "Bacarıqlarınızı",
    titleAccent: "inamla və məqsədlə",
    titleTail: "inkişaf etdirin.",
    paragraphs: [
      "Nexora Academy aydın kurs məlumatını, praktik proqramları, qeydiyyat prosesini və şəffaf tələbə dəstəyini birləşdirir.",
      "Məqsədinizə uyğun kursu və dəstək imkanını seçin, sonra rəsmi müraciət və ya qeydiyyat yoluna keçin.",
    ],
    imageUrl: "assets/career/karyera-team-group.jpg",
  },
  vacancies: {
    title: "Karyera",
    description: "Mövcud imkanları nəzərdən keçirin və sizə uyğun olanın təfərrüatını açın.",
  },
};

function objectValue(value: unknown): Record<string, unknown> {
  return value && typeof value === "object" && !Array.isArray(value)
    ? (value as Record<string, unknown>)
    : {};
}

function textValue(value: unknown, fallback: string): string {
  return typeof value === "string" ? value : fallback;
}

function parseCareerData(value: unknown): CareerPageData {
  const data = objectValue(value);
  const hero = objectValue(data.hero);
  const journey = objectValue(data.journey);
  const intro = objectValue(data.intro);
  const vacancies = objectValue(data.vacancies);
  const savedJourneyItems = Array.isArray(journey.items) ? journey.items : [];
  const savedParagraphs = Array.isArray(intro.paragraphs) ? intro.paragraphs : [];

  return {
    hero: {
      titleLead: textValue(hero.titleLead, DEFAULT_DATA.hero.titleLead),
      titleAccent: textValue(hero.titleAccent, DEFAULT_DATA.hero.titleAccent),
      titleTail: textValue(hero.titleTail, DEFAULT_DATA.hero.titleTail),
      description: textValue(hero.description, DEFAULT_DATA.hero.description),
      buttonLabel: textValue(hero.buttonLabel, DEFAULT_DATA.hero.buttonLabel),
      buttonUrl: textValue(hero.buttonUrl, DEFAULT_DATA.hero.buttonUrl),
      imageUrl: textValue(hero.imageUrl, DEFAULT_DATA.hero.imageUrl),
    },
    journey: {
      titleLead: textValue(journey.titleLead, DEFAULT_DATA.journey.titleLead),
      titleAccent: textValue(journey.titleAccent, DEFAULT_DATA.journey.titleAccent),
      items: DEFAULT_DATA.journey.items.map((fallback, index) => {
        const item = objectValue(savedJourneyItems[index]);
        return {
          title: textValue(item.title, fallback.title),
          description: textValue(item.description, fallback.description),
          imageUrl: textValue(item.imageUrl, fallback.imageUrl),
        };
      }),
    },
    intro: {
      titleLead: textValue(intro.titleLead, DEFAULT_DATA.intro.titleLead),
      titleAccent: textValue(intro.titleAccent, DEFAULT_DATA.intro.titleAccent),
      titleTail: textValue(intro.titleTail, DEFAULT_DATA.intro.titleTail),
      paragraphs: DEFAULT_DATA.intro.paragraphs.map((fallback, index) =>
        textValue(savedParagraphs[index], fallback),
      ),
      imageUrl: textValue(intro.imageUrl, DEFAULT_DATA.intro.imageUrl),
    },
    vacancies: {
      title: textValue(vacancies.title, DEFAULT_DATA.vacancies.title),
      description: textValue(vacancies.description, DEFAULT_DATA.vacancies.description),
    },
  };
}

export function CareerPage() {
  const queryClient = useQueryClient();
  const [document, setDocument] = useState<CmsContentDocument | null>(null);
  const [content, setContent] = useState<CareerPageData>(DEFAULT_DATA);
  const [published, setPublished] = useState(true);
  const [ready, setReady] = useState(false);
  const query = useQuery({
    queryKey: ["cms-career"],
    queryFn: () => api.get<CmsContentDocument[]>("/api/v1/content/cms-content"),
  });

  useEffect(() => {
    if (ready || !query.data) return;
    const found = query.data.find((item) => item.key === KEY) ?? null;
    setDocument(found);
    setContent(parseCareerData(found?.data));
    setPublished(found?.published ?? true);
    setReady(true);
  }, [query.data, ready]);

  const saveMutation = useMutation({
    mutationFn: () => {
      const payload = {
        key: KEY,
        type: "PAGE",
        title: "Karyera",
        body: content.hero.description.trim(),
        data: { ...(document?.data ?? {}), ...content },
        published,
        sortOrder: 20,
      };
      return document
        ? api.patch<CmsContentDocument>(`/api/v1/content/cms-content/${document.id}`, payload)
        : api.post<CmsContentDocument>("/api/v1/content/cms-content", payload);
    },
    onSuccess: (saved) => {
      setDocument(saved);
      void queryClient.invalidateQueries({ queryKey: ["cms-career"] });
      toast.success("Karyera səhifəsi yadda saxlanıldı.");
    },
    onError: (error) =>
      toast.error(error instanceof ApiError ? error.message : "Karyera səhifəsi yadda saxlanıla bilmədi."),
  });

  function updateHero<Key extends keyof CareerHero>(field: Key, value: CareerHero[Key]) {
    setContent((current) => ({ ...current, hero: { ...current.hero, [field]: value } }));
  }

  function updateJourney<Key extends keyof Omit<CareerJourney, "items">>(
    field: Key,
    value: CareerJourney[Key],
  ) {
    setContent((current) => ({
      ...current,
      journey: { ...current.journey, [field]: value },
    }));
  }

  function updateJourneyItem(index: number, field: keyof CareerJourneyItem, value: string) {
    setContent((current) => ({
      ...current,
      journey: {
        ...current.journey,
        items: current.journey.items.map((item, itemIndex) =>
          itemIndex === index ? { ...item, [field]: value } : item,
        ),
      },
    }));
  }

  function updateIntro<Key extends keyof Omit<CareerIntro, "paragraphs">>(
    field: Key,
    value: CareerIntro[Key],
  ) {
    setContent((current) => ({ ...current, intro: { ...current.intro, [field]: value } }));
  }

  function updateIntroParagraph(index: number, value: string) {
    setContent((current) => ({
      ...current,
      intro: {
        ...current.intro,
        paragraphs: current.intro.paragraphs.map((paragraph, paragraphIndex) =>
          paragraphIndex === index ? value : paragraph,
        ),
      },
    }));
  }

  function updateVacancies<Key extends keyof CareerVacancies>(
    field: Key,
    value: CareerVacancies[Key],
  ) {
    setContent((current) => ({
      ...current,
      vacancies: { ...current.vacancies, [field]: value },
    }));
  }

  if (query.isLoading || !ready) {
    return (
      <div className="space-y-4">
        <Skeleton className="h-24 rounded-2xl" />
        <Skeleton className="h-96 rounded-2xl" />
      </div>
    );
  }

  return (
    <div className="space-y-5">
      <PageHeader
        eyebrow="Məzmun studiyası"
        title="Karyera"
        description="Karyera səhifəsinin hero, yol xəritəsi və təqdimat məzmununu idarə edin."
        actions={
          <>
            <Button
              variant="outline"
              nativeButton={false}
              render={<a href="/career.html" target="_blank" rel="noreferrer" />}
            >
              <ExternalLink className="size-4" />
              Saytı aç
            </Button>
            <Button onClick={() => saveMutation.mutate()} disabled={saveMutation.isPending}>
              {saveMutation.isPending ? (
                <Loader2 className="size-4 animate-spin" />
              ) : (
                <Save className="size-4" />
              )}
              Yadda saxla
            </Button>
          </>
        }
      />
      <ErrorBanner error={query.error} />

      <EditorCard
        title="Hero bölməsi"
        description="Səhifənin ilk görünən başlığını, çağırışını və fon şəklini dəyişin."
        icon={ImageIcon}
        actions={
          <div className="flex items-center gap-2">
            <Label htmlFor="career-published" className="text-xs">
              Yayımda
            </Label>
            <Switch
              id="career-published"
              checked={published}
              onCheckedChange={setPublished}
            />
          </div>
        }
      >
        <div className="grid gap-6 xl:grid-cols-[1fr_22rem]">
          <div className="grid gap-4 md:grid-cols-2">
            <div className="space-y-1.5">
              <Label htmlFor="career-hero-lead">Başlığın ilk hissəsi</Label>
              <Input
                id="career-hero-lead"
                value={content.hero.titleLead}
                onChange={(event) => updateHero("titleLead", event.target.value)}
              />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="career-hero-accent">Vurğulanan hissə</Label>
              <Input
                id="career-hero-accent"
                value={content.hero.titleAccent}
                onChange={(event) => updateHero("titleAccent", event.target.value)}
              />
            </div>
            <div className="space-y-1.5 md:col-span-2">
              <Label htmlFor="career-hero-tail">Başlığın davamı</Label>
              <Textarea
                id="career-hero-tail"
                rows={3}
                value={content.hero.titleTail}
                onChange={(event) => updateHero("titleTail", event.target.value)}
              />
            </div>
            <div className="space-y-1.5 md:col-span-2">
              <Label htmlFor="career-hero-description">Açıqlama</Label>
              <Textarea
                id="career-hero-description"
                rows={3}
                value={content.hero.description}
                onChange={(event) => updateHero("description", event.target.value)}
              />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="career-hero-button">Düymə mətni</Label>
              <Input
                id="career-hero-button"
                value={content.hero.buttonLabel}
                onChange={(event) => updateHero("buttonLabel", event.target.value)}
              />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="career-hero-link">Düymə keçidi</Label>
              <Input
                id="career-hero-link"
                value={content.hero.buttonUrl}
                onChange={(event) => updateHero("buttonUrl", event.target.value)}
                placeholder="contact.html"
              />
            </div>
          </div>
          <div className="space-y-2">
            <Label>Hero fon şəkli</Label>
            <ImageUploadField
              id="career-hero-image"
              value={content.hero.imageUrl}
              onChange={(value) => updateHero("imageUrl", value)}
            />
          </div>
        </div>
      </EditorCard>

      <EditorCard
        title="Karyera yol xəritəsi"
        description="Namizədlərə göstərilən üç əsas mərhələni idarə edin."
        icon={MapPinned}
      >
        <div className="mb-6 grid gap-4 md:grid-cols-2">
          <div className="space-y-1.5">
            <Label htmlFor="career-journey-lead">Vurğulanan başlıq</Label>
            <Input
              id="career-journey-lead"
              value={content.journey.titleLead}
              onChange={(event) => updateJourney("titleLead", event.target.value)}
            />
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="career-journey-accent">Başlığın davamı</Label>
            <Input
              id="career-journey-accent"
              value={content.journey.titleAccent}
              onChange={(event) => updateJourney("titleAccent", event.target.value)}
            />
          </div>
        </div>
        <div className="grid gap-4 xl:grid-cols-3">
          {content.journey.items.map((item, index) => (
            <div key={index} className="space-y-4 rounded-xl border border-border/70 bg-muted/15 p-4">
              <div className="flex items-center gap-2">
                <NumberBadge value={index + 1} />
                <p className="font-medium">Mərhələ {index + 1}</p>
              </div>
              <div className="space-y-1.5">
                <Label htmlFor={`career-journey-title-${index}`}>Başlıq</Label>
                <Input
                  id={`career-journey-title-${index}`}
                  value={item.title}
                  onChange={(event) => updateJourneyItem(index, "title", event.target.value)}
                />
              </div>
              <div className="space-y-1.5">
                <Label htmlFor={`career-journey-description-${index}`}>Açıqlama</Label>
                <Textarea
                  id={`career-journey-description-${index}`}
                  rows={4}
                  value={item.description}
                  onChange={(event) => updateJourneyItem(index, "description", event.target.value)}
                />
              </div>
              <div className="space-y-2">
                <Label>İkon / şəkil</Label>
                <ImageUploadField
                  id={`career-journey-image-${index}`}
                  value={item.imageUrl}
                  onChange={(value) => updateJourneyItem(index, "imageUrl", value)}
                />
              </div>
            </div>
          ))}
        </div>
      </EditorCard>

      <EditorCard
        title="Təqdimat bölməsi"
        description="Komanda şəklinin yanında görünən təqdimat məzmununu dəyişin."
        icon={Users}
      >
        <div className="grid gap-6 xl:grid-cols-[1fr_22rem]">
          <div className="grid gap-4 md:grid-cols-2">
            <div className="space-y-1.5">
              <Label htmlFor="career-intro-lead">Başlığın ilk hissəsi</Label>
              <Input
                id="career-intro-lead"
                value={content.intro.titleLead}
                onChange={(event) => updateIntro("titleLead", event.target.value)}
              />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="career-intro-accent">Vurğulanan hissə</Label>
              <Input
                id="career-intro-accent"
                value={content.intro.titleAccent}
                onChange={(event) => updateIntro("titleAccent", event.target.value)}
              />
            </div>
            <div className="space-y-1.5 md:col-span-2">
              <Label htmlFor="career-intro-tail">Başlığın davamı</Label>
              <Input
                id="career-intro-tail"
                value={content.intro.titleTail}
                onChange={(event) => updateIntro("titleTail", event.target.value)}
              />
            </div>
            {content.intro.paragraphs.map((paragraph, index) => (
              <div key={index} className="space-y-1.5 md:col-span-2">
                <Label htmlFor={`career-intro-paragraph-${index}`}>Mətn {index + 1}</Label>
                <Textarea
                  id={`career-intro-paragraph-${index}`}
                  rows={4}
                  value={paragraph}
                  onChange={(event) => updateIntroParagraph(index, event.target.value)}
                />
              </div>
            ))}
          </div>
          <div className="space-y-2">
            <Label>Komanda şəkli</Label>
            <ImageUploadField
              id="career-team-image"
              value={content.intro.imageUrl}
              onChange={(value) => updateIntro("imageUrl", value)}
            />
          </div>
        </div>
      </EditorCard>

      <EditorCard
        title="Vakansiya siyahısı"
        description="Bölmə başlığını burada, vakansiya elanlarını isə ayrıca səhifədə idarə edin."
        icon={BriefcaseBusiness}
        actions={
          <Button
            variant="outline"
            size="sm"
            nativeButton={false}
            render={<Link to="/vacancies" />}
          >
            Vakansiyaları idarə et
          </Button>
        }
      >
        <div className="grid gap-4 md:grid-cols-2">
          <div className="space-y-1.5">
            <Label htmlFor="career-vacancies-title">Bölmə başlığı</Label>
            <Input
              id="career-vacancies-title"
              value={content.vacancies.title}
              onChange={(event) => updateVacancies("title", event.target.value)}
            />
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="career-vacancies-description">Bölmə açıqlaması</Label>
            <Textarea
              id="career-vacancies-description"
              rows={3}
              value={content.vacancies.description}
              onChange={(event) => updateVacancies("description", event.target.value)}
            />
          </div>
        </div>
      </EditorCard>
    </div>
  );
}
