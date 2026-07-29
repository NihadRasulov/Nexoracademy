export function BrandMark() {
  return (
    <div className="flex items-center gap-3 px-4 py-4">
      <img src="/nexora-wordmark.png" alt="Nexora" className="h-7 w-auto shrink-0 dark:brightness-110" />
      <span className="border-l border-sidebar-border pl-3 text-xs leading-tight font-medium text-muted-foreground">
        Admin
        <br />
        Panel
      </span>
    </div>
  );
}
