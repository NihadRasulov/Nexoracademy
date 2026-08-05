import { Button } from "@/components/ui/button";
import { ChevronLeft, ChevronRight } from "lucide-react";

interface PaginationBarProps {
  page: number;
  totalPages: number;
  totalElements: number;
  onPageChange: (page: number) => void;
}

export function PaginationBar({ page, totalPages, totalElements, onPageChange }: PaginationBarProps) {
  return (
    <div className="mt-4 flex items-center justify-between text-sm text-muted-foreground">
      <span>Cəmi {totalElements} qeyd</span>
      <div className="flex items-center gap-2">
        <Button
          variant="outline"
          size="icon"
          disabled={page <= 0}
          onClick={() => onPageChange(page - 1)}
          aria-label="Əvvəlki səhifə"
        >
          <ChevronLeft className="size-4" />
        </Button>
        <span>
          Səhifə {totalPages === 0 ? 0 : page + 1} / {totalPages}
        </span>
        <Button
          variant="outline"
          size="icon"
          disabled={page + 1 >= totalPages}
          onClick={() => onPageChange(page + 1)}
          aria-label="Növbəti səhifə"
        >
          <ChevronRight className="size-4" />
        </Button>
      </div>
    </div>
  );
}
