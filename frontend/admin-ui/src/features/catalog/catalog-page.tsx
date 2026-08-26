import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { CoursesPage } from "@/pages/courses-page";
import { ResourcePage } from "@/resources/resource-page";
import { categoryConfig, instructorConfig } from "@/resources/resource-configs";

export function CatalogPage() {
  return (
    <Tabs defaultValue="courses" className="gap-5">
      <div className="overflow-x-auto">
        <TabsList className="h-auto rounded-xl border bg-card p-1 shadow-sm">
          <TabsTrigger value="courses">Kurslar</TabsTrigger>
          <TabsTrigger value="instructors">Təlimçilər</TabsTrigger>
          <TabsTrigger value="categories">Kateqoriyalar</TabsTrigger>
        </TabsList>
      </div>
      <TabsContent value="courses"><CoursesPage /></TabsContent>
      <TabsContent value="instructors"><ResourcePage config={instructorConfig} /></TabsContent>
      <TabsContent value="categories"><ResourcePage config={categoryConfig} /></TabsContent>
    </Tabs>
  );
}
