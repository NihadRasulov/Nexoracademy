import { useEffect, useRef, useState } from "react";
import { Film, Loader2, Trash2, Upload } from "lucide-react";
import { Button } from "@/components/ui/button";
import { api, assetUrl } from "@/lib/api";
import { ApiError } from "@/lib/api-error";

const MAX_SIZE_BYTES = 25 * 1024 * 1024;
const ACCEPTED = ".mp4,.webm";

export function MediaUploadField({
  id,
  value,
  onChange,
}: {
  id: string;
  value: string;
  onChange: (value: string) => void;
}) {
  const inputRef = useRef<HTMLInputElement>(null);
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [previewFailed, setPreviewFailed] = useState(false);

  useEffect(() => setPreviewFailed(false), [value]);

  async function handleFile(file: File | undefined) {
    if (!file) return;
    setError(null);
    if (file.size > MAX_SIZE_BYTES) {
      setError("Video ölçüsü 25MB limitini aşır.");
      return;
    }

    setUploading(true);
    try {
      const formData = new FormData();
      formData.append("file", file);
      const result = await api.postForm<{ url: string }>("/api/v1/uploads/media", formData);
      onChange(result.url);
    } catch (uploadError) {
      setError(
        uploadError instanceof ApiError
          ? uploadError.message
          : "Video yüklənə bilmədi. Yenidən cəhd edin.",
      );
    } finally {
      setUploading(false);
      if (inputRef.current) inputRef.current.value = "";
    }
  }

  return (
    <div className="space-y-3">
      <div className="overflow-hidden rounded-xl border border-border/70 bg-slate-950">
        {value && !previewFailed ? (
          <video
            className="aspect-video w-full object-cover"
            src={assetUrl(value)}
            controls
            muted
            preload="metadata"
            onError={() => setPreviewFailed(true)}
          />
        ) : (
          <div className="flex aspect-video flex-col items-center justify-center gap-2 px-4 text-center text-slate-400">
            <Film className="size-8" aria-hidden />
            {previewFailed && (
              <span className="text-xs">Bu ünvanın önizləməsi admin paneldə əlçatan deyil.</span>
            )}
          </div>
        )}
      </div>

      <div className="flex flex-wrap gap-2">
        <input
          ref={inputRef}
          id={id}
          type="file"
          accept={ACCEPTED}
          className="hidden"
          disabled={uploading}
          onChange={(event) => void handleFile(event.target.files?.[0])}
        />
        <Button type="button" variant="outline" size="sm" disabled={uploading} onClick={() => inputRef.current?.click()}>
          {uploading ? <Loader2 className="size-4 animate-spin" /> : <Upload className="size-4" />}
          {uploading ? "Yüklənir..." : value ? "Videonu dəyiş" : "Video seç"}
        </Button>
        {value && (
          <Button type="button" variant="ghost" size="sm" className="text-destructive" onClick={() => onChange("")}>
            <Trash2 className="size-4" />
            Təmizlə
          </Button>
        )}
      </div>
      <p className="text-xs text-muted-foreground">MP4 və ya WebM, maksimum 25MB. URL/path sahəsini əl ilə də dəyişə bilərsiniz.</p>
      {error && <p className="text-xs font-medium text-destructive">{error}</p>}
    </div>
  );
}
