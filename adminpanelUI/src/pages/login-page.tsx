import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { useLocation, useNavigate } from "react-router-dom";
import { Loader2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { useAuth } from "@/auth/auth-context";
import { ApiError } from "@/lib/api-error";

const schema = z.object({
  email: z.string().email("Düzgün bir e-poçt daxil edin."),
  password: z.string().min(1, "Parol tələb olunur."),
});

type FormValues = z.infer<typeof schema>;

export function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [serverError, setServerError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({ resolver: zodResolver(schema) });

  async function onSubmit(values: FormValues) {
    setServerError(null);
    try {
      await login(values.email, values.password);
      const redirectTo = (location.state as { from?: Location })?.from?.pathname ?? "/dashboard";
      navigate(redirectTo, { replace: true });
    } catch (error) {
      if (error instanceof ApiError) {
        setServerError(error.message);
      } else {
        setServerError("Gözlənilməz xəta baş verdi.");
      }
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-gradient-to-b from-accent/60 to-muted/40 p-4">
      <Card className="w-full max-w-sm shadow-lg">
        <CardHeader className="items-center text-center">
          <img src="/nexora-logo.png" alt="Nexora Academy" className="mb-3 h-14 w-auto" />
          <CardTitle className="text-xl">Nexora Academy</CardTitle>
          <CardDescription>İdarəetmə panelinə daxil olun</CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <div className="space-y-1.5">
              <Label htmlFor="email">E-poçt</Label>
              <Input id="email" type="email" autoComplete="username" {...register("email")} />
              {errors.email && <p className="text-xs font-medium text-destructive">{errors.email.message}</p>}
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="password">Parol</Label>
              <Input id="password" type="password" autoComplete="current-password" {...register("password")} />
              {errors.password && (
                <p className="text-xs font-medium text-destructive">{errors.password.message}</p>
              )}
            </div>

            {serverError && <p className="text-sm font-medium text-destructive">{serverError}</p>}

            <Button type="submit" className="w-full" disabled={isSubmitting}>
              {isSubmitting && <Loader2 className="size-4 animate-spin" />}
              Daxil ol
            </Button>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
