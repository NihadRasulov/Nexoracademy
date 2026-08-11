import { Link } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { ShieldAlert, FileQuestion } from "lucide-react";

function StatusScreen({
  icon: Icon,
  title,
  description,
}: {
  icon: typeof ShieldAlert;
  title: string;
  description: string;
}) {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center gap-4 p-6 text-center">
      <div className="flex size-14 items-center justify-center rounded-full bg-muted">
        <Icon className="size-7 text-muted-foreground" />
      </div>
      <div>
        <h1 className="text-lg font-semibold">{title}</h1>
        <p className="mt-1 text-sm text-muted-foreground">{description}</p>
      </div>
      <Button variant="outline" render={<Link to="/dashboard">Ana panelə qayıt</Link>} />
    </div>
  );
}

export function ForbiddenPage() {
  return (
    <StatusScreen
      icon={ShieldAlert}
      title="İcazə yoxdur"
      description="Bu səhifəyə baxmaq üçün kifayət qədər icazəniz yoxdur."
    />
  );
}

export function NotFoundPage() {
  return (
    <StatusScreen icon={FileQuestion} title="Səhifə tapılmadı" description="Axtardığınız səhifə mövcud deyil." />
  );
}
