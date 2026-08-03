import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import { RequireAuth, RequireRole } from "@/auth/protected-route";
import { AppLayout } from "@/layout/app-layout";
import { LoginPage } from "@/pages/login-page";
import { DashboardPage } from "@/pages/dashboard-page";
import { ProfilePage } from "@/pages/profile-page";
import { UsersPage } from "@/pages/users-page";
import { CoursesPage } from "@/pages/courses-page";
import { CourseInstructorsPage } from "@/pages/course-instructors-page";
import { EnrollmentsPage } from "@/pages/enrollments-page";
import { PaymentsPage } from "@/pages/payments-page";
import { ForbiddenPage, NotFoundPage } from "@/pages/status-pages";
import { ResourcePage } from "@/resources/resource-page";
import {
  applicationConfig,
  auditLogConfig,
  campaignConfig,
  categoryConfig,
  chatSessionConfig,
  cmsContentConfig,
  contactSubmissionConfig,
  courseGroupConfig,
  courseReviewConfig,
  graduateOutcomeConfig,
  instructorConfig,
  kbArticleConfig,
  leadConfig,
  notificationConfig,
  oauthAccountConfig,
  scholarshipConfig,
  sessionConfig,
} from "@/resources/resource-configs";

export function App() {
  return (
    <BrowserRouter basename="/admin" >
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
              <Route path="/scholarships" element={<ResourcePage config={scholarshipConfig} />} />
              <Route path="/oauth-accounts" element={<ResourcePage config={oauthAccountConfig} />} />
              <Route path="/sessions" element={<ResourcePage config={sessionConfig} />} />
              <Route path="/notifications" element={<ResourcePage config={notificationConfig} />} />
              <Route path="/audit-logs" element={<ResourcePage config={auditLogConfig} />} />
              <Route path="/applications" element={<ResourcePage config={applicationConfig} />} />
            </Route>

            <Route element={<RequireRole group="contentManager" />}>
              <Route path="/categories" element={<ResourcePage config={categoryConfig} />} />
              <Route path="/courses" element={<CoursesPage />} />
              <Route path="/instructors" element={<ResourcePage config={instructorConfig} />} />
              <Route path="/course-instructors" element={<CourseInstructorsPage />} />
              <Route path="/course-groups" element={<ResourcePage config={courseGroupConfig} />} />
              <Route path="/course-reviews" element={<ResourcePage config={courseReviewConfig} />} />
              <Route path="/graduate-outcomes" element={<ResourcePage config={graduateOutcomeConfig} />} />
              <Route path="/kb-articles" element={<ResourcePage config={kbArticleConfig} />} />
              <Route path="/cms-content" element={<ResourcePage config={cmsContentConfig} />} />
            </Route>

            <Route element={<RequireRole group="salesCrm" />}>
              <Route path="/enrollments" element={<EnrollmentsPage />} />
              <Route path="/campaigns" element={<ResourcePage config={campaignConfig} />} />
              <Route path="/leads" element={<ResourcePage config={leadConfig} />} />
              <Route path="/contact-submissions" element={<ResourcePage config={contactSubmissionConfig} />} />
              <Route path="/chat-sessions" element={<ResourcePage config={chatSessionConfig} />} />
            </Route>
          </Route>
        </Route>

        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </BrowserRouter>
  );
}
