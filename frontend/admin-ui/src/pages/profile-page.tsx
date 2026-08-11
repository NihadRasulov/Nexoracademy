import { useState } from "react";
import { useMutation } from "@tanstack/react-query";
import { toast } from "sonner";
import { Loader2 } from "lucide-react";
import { PageHeader } from "@/components/page-header";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Label } from "@/components/ui/label";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import { useAuth } from "@/auth/auth-context";
import { ROLE_LABELS } from "@/auth/roles";
import { api } from "@/lib/api";
import { ApiError } from "@/lib/api-error";
import type { MeResponse } from "@/types/common";

export function ProfilePage() {
  const { user, refreshMe } = useAuth();
  const [fullName, setFullName] = useState(user?.fullName ?? "");
  const [phone, setPhone] = useState(user?.phone ?? "");
  const [locale, setLocale] = useState(user?.locale ?? "");

  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");

  const updateProfile = useMutation({
    mutationFn: () =>
      api.patch<MeResponse>("/api/v1/users/me", {
        fullName: fullName || undefined,
        phone: phone || undefined,
        locale: locale || undefined,
      }),
    onSuccess: async () => {
      toast.success("Profil yeniləndi.");
      await refreshMe();
    },
    onError: (error) => toast.error(error instanceof ApiError ? error.message : "Xəta baş verdi."),
  });

  const changePassword = useMutation({
    mutationFn: () => api.post<void>("/api/v1/users/me/password", { currentPassword, newPassword }),
    onSuccess: () => {
      toast.success("Parol dəyişdirildi.");
      setCurrentPassword("");
      setNewPassword("");
    },
    onError: (error) => toast.error(error instanceof ApiError ? error.message : "Xəta baş verdi."),
  });

  return (
    <div className="max-w-2xl">
      <PageHeader title="Profilim" description="Hesab məlumatlarınızı idarə edin." />

      <Card className="mb-6">
        <CardHeader>
          <CardTitle className="text-base">Şəxsi Məlumatlar</CardTitle>
          <CardDescription>
            {user?.email} · {user ? (ROLE_LABELS[user.role] ?? user.role) : ""}
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-1.5">
            <Label htmlFor="fullName">Ad Soyad</Label>
            <Input
              id="fullName"
              value={fullName}
              onChange={(e: React.ChangeEvent<HTMLInputElement>) => setFullName(e.target.value)}
            />
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="phone">Telefon</Label>
            <Input
              id="phone"
              value={phone ?? ""}
              onChange={(e: React.ChangeEvent<HTMLInputElement>) => setPhone(e.target.value)}
            />
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="locale">Dil (locale)</Label>
            <Input
              id="locale"
              value={locale}
              onChange={(e: React.ChangeEvent<HTMLInputElement>) => setLocale(e.target.value)}
              placeholder="az"
            />
          </div>
          <Button onClick={() => updateProfile.mutate()} disabled={updateProfile.isPending}>
            {updateProfile.isPending && <Loader2 className="size-4 animate-spin" />}
            Yadda saxla
          </Button>
        </CardContent>
      </Card>

      <Separator className="mb-6" />

      <Card>
        <CardHeader>
          <CardTitle className="text-base">Parolu Dəyişdir</CardTitle>
          <CardDescription>Cari parolunuzu təsdiqləyib yenisini təyin edin.</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-1.5">
            <Label htmlFor="currentPassword">Cari parol</Label>
            <Input
              id="currentPassword"
              type="password"
              autoComplete="current-password"
              maxLength={256}
              value={currentPassword}
              onChange={(e: React.ChangeEvent<HTMLInputElement>) => setCurrentPassword(e.target.value)}
            />
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="newPassword">Yeni parol</Label>
            <Input
              id="newPassword"
              type="password"
              autoComplete="new-password"
              maxLength={256}
              value={newPassword}
              onChange={(e: React.ChangeEvent<HTMLInputElement>) => setNewPassword(e.target.value)}
            />
          </div>
          <Button
            onClick={() => changePassword.mutate()}
            disabled={changePassword.isPending || !currentPassword || !newPassword}
          >
            {changePassword.isPending && <Loader2 className="size-4 animate-spin" />}
            Parolu dəyişdir
          </Button>
        </CardContent>
      </Card>
    </div>
  );
}
