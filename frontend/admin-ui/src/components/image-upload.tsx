import { useEffect, useRef, useState } from "react";
import { ImagePlus, Loader2, Trash2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { api, assetUrl } from "@/lib/api";
import { ApiError } from "@/lib/api-error";

const MAX_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB — Java ImageUploadService.MAX_IMAGE_SIZE ilə eyni
const ACCEPTED = ".jpg,.jpeg,.png,.webp";

export function ImageUploadField({
  id,
  value,
  onChange,
  placeholder,
}: {
  id: string;
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
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
      setError("Fayl ölçüsü 5MB limitini aşır.");
      return;
    }

    setUploading(true);
    try {
      const formData = new FormData();
      formData.append("file", file);
      const result = await api.postForm<{ url: string }>("/api/v1/uploads", formData);
      onChange(result.url);
    } catch (err) {
      setError(
        err instanceof ApiError ? err.message : "Şəkil yüklənə bilmədi. Yenidən cəhd edin.",
      );
    } finally {
      setUploading(false);
      if (inputRef.current) inputRef.current.value = "";
    }
  }

  return (
    <div className="space-y-2">
      <div className="flex items-start gap-3">
        <div className="flex h-24 w-40 shrink-0 items-center justify-center overflow-hidden rounded-md border border-border bg-muted/30">
          {value && !previewFailed ? (
            <img
              src={assetUrl(value)}
              alt="Şəkil önizləmə"
              className="h-full w-full object-cover"
              onError={() => setPreviewFailed(true)}
            />
          ) : (
            <ImagePlus className="size-6 text-muted-foreground" aria-hidden />
          )}
        </div>

        <div className="flex flex-col gap-1.5">
          <input
            ref={inputRef}
            id={id}
            type="file"
            accept={ACCEPTED}
            className="hidden"
            disabled={uploading}
            onChange={(e) => void handleFile(e.target.files?.[0])}
          />
          <Button
            type="button"
            variant="outline"
            size="sm"
            disabled={uploading}
            onClick={() => inputRef.current?.click()}
          >
            {uploading ? (
              <>
                <Loader2 className="size-4 animate-spin" />
                Yüklənir...
              </>
            ) : (
              <>
                <ImagePlus className="size-4" />
                {value ? "Şəkli dəyiş" : "Şəkil seç"}
              </>
            )}
          </Button>

          {value && (
            <Button
              type="button"
              variant="ghost"
              size="sm"
              className="text-destructive hover:text-destructive"
              onClick={() => onChange("")}
            >
              <Trash2 className="size-4" />
              Təmizlə
            </Button>
          )}
          {!value && placeholder && <p className="text-xs text-muted-foreground">{placeholder}</p>}
        </div>
      </div>

      {error && <p className="text-xs font-medium text-destructive">{error}</p>}
    </div>
  );
}
