import { lazy, Suspense } from "react";
import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import { Loader2 } from "lucide-react";
import { ADMIN_BASE_PATH } from "@/config/admin-base-path";
import { RequireAuth, RequireRole } from "@/auth/protected-route";

const AppLayout = lazy(() => import("@/layout/app-layout").then((module) => ({ default: module.AppLayout })));
const LoginPage = lazy(() => import("@/pages/login-page").then((module) => ({ default: module.LoginPage })));
const DashboardPage = lazy(() => import("@/pages/dashboard-page").then((module) => ({ default: module.DashboardPage })));
const ProfilePage = lazy(() => import("@/pages/profile-page").then((module) => ({ default: module.ProfilePage })));
const UsersPage = lazy(() => import("@/pages/users-page").then((module) => ({ default: module.UsersPage })));
const CoursesPage = lazy(() => import("@/pages/courses-page").then((module) => ({ default: module.CoursesPage })));
const CourseInstructorsPage = lazy(() => import("@/pages/course-instructors-page").then((module) => ({ default: module.CourseInstructorsPage })));
const EnrollmentsPage = lazy(() => import("@/pages/enrollments-page").then((module) => ({ default: module.EnrollmentsPage })));
const PaymentsPage = lazy(() => import("@/pages/payments-page").then((module) => ({ default: module.PaymentsPage })));
const ResourceRoutePage = lazy(() => import("@/resources/resource-route-page").then((module) => ({ default: module.ResourceRoutePage })));
const ForbiddenPage = lazy(() => import("@/pages/status-pages").then((module) => ({ default: module.ForbiddenPage })));
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
            <Route path="/403" element={<ForbiddenPage />} />

            <Route element={<AppLayout />}>
              <Route path="/" element={<Navigate to="/dashboard" replace />} />
              <Route path="/dashboard" element={<DashboardPage />} />
              <Route path="/profile" element={<ProfilePage />} />

              <Route element={<RequireRole group="adminOnly" />}>
                <Route path="/users" element={<UsersPage />} />
                <Route path="/payments" element={<PaymentsPage />} />
                <Route path="/scholarships" element={<ResourceRoutePage resource="scholarship" />} />
                <Route path="/oauth-accounts" element={<ResourceRoutePage resource="oauthAccount" />} />
                <Route path="/sessions" element={<ResourceRoutePage resource="session" />} />
                <Route path="/notifications" element={<ResourceRoutePage resource="notification" />} />
                <Route path="/audit-logs" element={<ResourceRoutePage resource="auditLog" />} />
              </Route>

              <Route element={<RequireRole group="contentManager" />}>
                <Route path="/categories" element={<ResourceRoutePage resource="category" />} />
                <Route path="/courses" element={<CoursesPage />} />
                <Route path="/instructors" element={<ResourceRoutePage resource="instructor" />} />
                <Route path="/course-instructors" element={<CourseInstructorsPage />} />
                <Route path="/course-groups" element={<ResourceRoutePage resource="courseGroup" />} />
                <Route path="/course-reviews" element={<ResourceRoutePage resource="courseReview" />} />
                <Route path="/graduate-outcomes" element={<ResourceRoutePage resource="graduateOutcome" />} />
                <Route path="/kb-articles" element={<ResourceRoutePage resource="kbArticle" />} />
                <Route path="/cms-content" element={<ResourceRoutePage resource="cmsContent" />} />
              </Route>

              <Route element={<RequireRole group="salesCrm" />}>
                <Route path="/enrollments" element={<EnrollmentsPage />} />
                <Route path="/campaigns" element={<ResourceRoutePage resource="campaign" />} />
                <Route path="/leads" element={<ResourceRoutePage resource="lead" />} />
                <Route path="/contact-submissions" element={<ResourceRoutePage resource="contactSubmission" />} />
                <Route path="/chat-sessions" element={<ResourceRoutePage resource="chatSession" />} />
              </Route>
            </Route>
          </Route>

          <Route path="*" element={<NotFoundPage />} />
        </Routes>
      </Suspense>
    </BrowserRouter>
  );
}
