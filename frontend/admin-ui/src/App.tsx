import { lazy, Suspense } from "react";
import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import { Loader2 } from "lucide-react";
import { ADMIN_BASE_PATH } from "@/config/admin-base-path";
import { RequireAuth } from "@/auth/protected-route";

const AppLayout = lazy(() => import("@/layout/app-layout").then((module) => ({ default: module.AppLayout })));
const LoginPage = lazy(() => import("@/pages/login-page").then((module) => ({ default: module.LoginPage })));
const DashboardPage = lazy(() => import("@/pages/dashboard-page").then((module) => ({ default: module.DashboardPage })));
const HomepagePage = lazy(() => import("@/features/homepage/homepage-page").then((module) => ({ default: module.HomepagePage })));
const NewsPage = lazy(() => import("@/features/news/news-page").then((module) => ({ default: module.NewsPage })));
const AboutPage = lazy(() => import("@/features/content/about-page").then((module) => ({ default: module.AboutPage })));
const CmsCollectionPage = lazy(() => import("@/features/content/cms-collection-page").then((module) => ({ default: module.CmsCollectionPage })));
const CatalogPage = lazy(() => import("@/features/catalog/catalog-page").then((module) => ({ default: module.CatalogPage })));
const InboxPage = lazy(() => import("@/features/inbox/inbox-page").then((module) => ({ default: module.InboxPage })));
const ProfilePage = lazy(() => import("@/pages/profile-page").then((module) => ({ default: module.ProfilePage })));
const NotFoundPage = lazy(() => import("@/pages/status-pages").then((module) => ({ default: module.NotFoundPage })));

function RouteFallback() {
  return (
    <div className="flex min-h-48 items-center justify-center" role="status" aria-label="Səhifə yüklənir">
      <Loader2 className="size-6 animate-spin text-muted-foreground" />
    </div>
  );
}

export function App() {
  return (
    <BrowserRouter basename={ADMIN_BASE_PATH}>
      <Suspense fallback={<RouteFallback />}>
        <Routes>
          <Route path="/login" element={<LoginPage />} />

          <Route element={<RequireAuth />}>
            <Route element={<AppLayout />}>
              <Route path="/" element={<Navigate to="/dashboard" replace />} />
              <Route path="/dashboard" element={<DashboardPage />} />
              <Route path="/profile" element={<ProfilePage />} />
              <Route path="/homepage" element={<HomepagePage />} />
              <Route path="/about" element={<AboutPage />} />
              <Route path="/news" element={<NewsPage />} />
              <Route path="/faq" element={<CmsCollectionPage type="FAQ" />} />
              <Route path="/vacancies" element={<CmsCollectionPage type="VACANCY" />} />
              <Route path="/catalog" element={<CatalogPage />} />
              <Route path="/inbox" element={<InboxPage />} />
            </Route>
          </Route>

          <Route path="*" element={<NotFoundPage />} />
        </Routes>
      </Suspense>
    </BrowserRouter>
  );
}
