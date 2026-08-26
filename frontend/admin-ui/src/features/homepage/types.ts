export interface CmsContentDocument {
  id: number;
  key: string;
  type: string;
  title?: string | null;
  body?: string | null;
  data?: Record<string, unknown> | null;
  published: boolean;
  sortOrder: number;
  updatedAt?: string | null;
}

export interface HomeMetric {
  value: string;
  label: string;
}

export interface HomeService {
  title: string;
  description: string;
  imageUrl: string;
}

export interface TeamMember {
  name: string;
  role: string;
  imageUrl: string;
}

export interface RoadmapItem {
  title: string;
  description: string;
}

export interface HomepageData extends Record<string, unknown> {
  hero: {
    titleLead: string;
    titleAccent: string;
    videoUrl: string;
    posterUrl: string;
  };
  stats: HomeMetric[];
  services: {
    titleLead: string;
    titleAccent: string;
    titleTail: string;
    items: HomeService[];
  };
  about: {
    titleLead: string;
    titleAccent: string;
    description: string;
    highlights: HomeMetric[];
    team: TeamMember[];
  };
  roadmap: {
    eyebrow: string;
    title: string;
    description: string;
    items: RoadmapItem[];
  };
  featuredCourseIds: string[];
  sections: {
    coursesTitleLead: string;
    coursesTitleAccent: string;
    newsTitleLead: string;
    newsTitleAccent: string;
    applicationTitleLead: string;
    applicationTitleAccent: string;
    newsletterTitle: string;
  };
}

export interface SocialLink {
  label: string;
  url: string;
}

export interface SiteSettingsData extends Record<string, unknown> {
  address: string;
  addressUrl: string;
  phone: string;
  email: string;
  socials: SocialLink[];
}

export interface CourseOption {
  id: string;
  title: string;
  slug: string;
  published: boolean;
  active: boolean;
}
