import type { ComponentProps } from "react";
import logoUrl from "@/assets/nexora-logo-primary.png";
import { cn } from "@/lib/utils";

type BrandLogoProps = Omit<ComponentProps<"img">, "src">;

export function BrandLogo({
  alt = "Nexora — Networking Simplified",
  className,
  ...props
}: BrandLogoProps) {
  return (
    <img
      src={logoUrl}
      alt={alt}
      className={cn("h-auto w-full object-contain", className)}
      decoding="async"
      {...props}
    />
  );
}
