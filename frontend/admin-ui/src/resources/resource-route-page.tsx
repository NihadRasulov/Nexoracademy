import { ResourcePage } from "@/resources/resource-page";
import {
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

export type ResourceRouteKey =
  | "auditLog"
  | "campaign"
  | "category"
  | "chatSession"
  | "cmsContent"
  | "contactSubmission"
  | "courseGroup"
  | "courseReview"
  | "graduateOutcome"
  | "instructor"
  | "kbArticle"
  | "lead"
  | "notification"
  | "oauthAccount"
  | "scholarship"
  | "session";

export function ResourceRoutePage({ resource }: { resource: ResourceRouteKey }) {
  switch (resource) {
    case "auditLog": return <ResourcePage config={auditLogConfig} />;
    case "campaign": return <ResourcePage config={campaignConfig} />;
    case "category": return <ResourcePage config={categoryConfig} />;
    case "chatSession": return <ResourcePage config={chatSessionConfig} />;
    case "cmsContent": return <ResourcePage config={cmsContentConfig} />;
    case "contactSubmission": return <ResourcePage config={contactSubmissionConfig} />;
    case "courseGroup": return <ResourcePage config={courseGroupConfig} />;
    case "courseReview": return <ResourcePage config={courseReviewConfig} />;
    case "graduateOutcome": return <ResourcePage config={graduateOutcomeConfig} />;
    case "instructor": return <ResourcePage config={instructorConfig} />;
    case "kbArticle": return <ResourcePage config={kbArticleConfig} />;
    case "lead": return <ResourcePage config={leadConfig} />;
    case "notification": return <ResourcePage config={notificationConfig} />;
    case "oauthAccount": return <ResourcePage config={oauthAccountConfig} />;
    case "scholarship": return <ResourcePage config={scholarshipConfig} />;
    case "session": return <ResourcePage config={sessionConfig} />;
  }
}
