import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { AlertCircle } from "lucide-react";
import { ApiError } from "@/lib/api-error";

export function ErrorBanner({ error }: { error: unknown }) {
  if (!error) return null;
  const message = error instanceof ApiError ? error.message : "Gözlənilməz xəta baş verdi.";
  const code = error instanceof ApiError ? error.code : undefined;

  return (
    <Alert variant="destructive" className="mb-4">
      <AlertCircle className="size-4" />
      <AlertTitle>{code ?? "Xəta"}</AlertTitle>
      <AlertDescription>{message}</AlertDescription>
    </Alert>
  );
}
