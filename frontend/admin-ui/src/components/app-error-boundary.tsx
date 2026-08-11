import { Component, type ErrorInfo, type ReactNode } from "react";
import { RefreshCw } from "lucide-react";
import { BrandLogo } from "@/components/brand-logo";
import { Button } from "@/components/ui/button";

interface AppErrorBoundaryProps {
  children: ReactNode;
}

interface AppErrorBoundaryState {
  failed: boolean;
}

export class AppErrorBoundary extends Component<AppErrorBoundaryProps, AppErrorBoundaryState> {
  state: AppErrorBoundaryState = { failed: false };

  static getDerivedStateFromError(): AppErrorBoundaryState {
    return { failed: true };
  }

  componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.error("Unhandled admin UI error", error, errorInfo);
  }

  render() {
    if (!this.state.failed) return this.props.children;

    return (
      <main className="flex min-h-screen items-center justify-center bg-muted/30 p-6">
        <section className="w-full max-w-md rounded-xl border bg-card p-8 text-center shadow-sm">
          <BrandLogo className="mx-auto mb-6 max-w-56" />
          <h1 className="text-xl font-semibold">Gözlənilməyən xəta baş verdi</h1>
          <p className="mt-2 text-sm text-muted-foreground">
            Səhifəni yeniləyin. Problem davam edərsə, sistem administratoruna müraciət edin.
          </p>
          <Button className="mt-6" type="button" onClick={() => window.location.reload()}>
            <RefreshCw className="size-4" />
            Səhifəni yenilə
          </Button>
        </section>
      </main>
    );
  }
}
