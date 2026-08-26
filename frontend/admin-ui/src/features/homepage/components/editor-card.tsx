import type { ReactNode } from "react";
import type { LucideIcon } from "lucide-react";
import { ChevronDown, ChevronUp, Trash2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";

export function EditorCard({
  title,
  description,
  icon: Icon,
  actions,
  children,
}: {
  title: string;
  description?: string;
  icon?: LucideIcon;
  actions?: ReactNode;
  children: ReactNode;
}) {
  return (
    <Card className="border-border/70 bg-card/90 shadow-sm shadow-sky-950/5 backdrop-blur">
      <CardHeader className="border-b border-border/60 pb-4">
        <div className="flex items-start justify-between gap-4">
          <div className="flex min-w-0 items-start gap-3">
            {Icon && (
              <div className="mt-0.5 flex size-9 shrink-0 items-center justify-center rounded-xl bg-gradient-to-br from-sky-400/20 to-indigo-500/20 text-primary ring-1 ring-primary/15">
                <Icon className="size-4.5" />
              </div>
            )}
            <div>
              <CardTitle>{title}</CardTitle>
              {description && <CardDescription className="mt-1">{description}</CardDescription>}
            </div>
          </div>
          {actions}
        </div>
      </CardHeader>
      <CardContent className="pt-5">{children}</CardContent>
    </Card>
  );
}

export function ArrayItemActions({
  index,
  length,
  onMove,
  onRemove,
}: {
  index: number;
  length: number;
  onMove: (from: number, to: number) => void;
  onRemove: () => void;
}) {
  return (
    <div className="flex items-center gap-1">
      <Button type="button" variant="ghost" size="icon-sm" disabled={index === 0} onClick={() => onMove(index, index - 1)} aria-label="Yuxarı daşı">
        <ChevronUp className="size-4" />
      </Button>
      <Button type="button" variant="ghost" size="icon-sm" disabled={index === length - 1} onClick={() => onMove(index, index + 1)} aria-label="Aşağı daşı">
        <ChevronDown className="size-4" />
      </Button>
      <Button type="button" variant="ghost" size="icon-sm" className="text-destructive hover:text-destructive" onClick={onRemove} aria-label="Sil">
        <Trash2 className="size-4" />
      </Button>
    </div>
  );
}

export function NumberBadge({ value }: { value: number }) {
  return (
    <span className="flex size-7 shrink-0 items-center justify-center rounded-full bg-primary/10 text-xs font-semibold text-primary">
      {value}
    </span>
  );
}
