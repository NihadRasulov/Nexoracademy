import type { ReactNode } from "react";

export function PageHeader({
  eyebrow,
  title,
  description,
  actions,
}: {
  eyebrow?: string;
  title: string;
  description?: string;
  actions?: ReactNode;
}) {
  return (
    <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
      <div className="max-w-3xl">
        {eyebrow && <p className="mb-2 text-[0.68rem] font-semibold tracking-[0.2em] text-primary uppercase">{eyebrow}</p>}
        <h1 className="text-2xl font-semibold tracking-[-0.025em] sm:text-3xl">{title}</h1>
        {description && <p className="mt-2 text-sm leading-6 text-muted-foreground">{description}</p>}
      </div>
      {actions && <div className="flex items-center gap-2">{actions}</div>}
    </div>
  );
}
