export const ROLE_GROUPS = {
  adminOnly: ["ADMIN", "SYSTEM_ADMIN"],
  contentManager: ["ADMIN", "SYSTEM_ADMIN", "CONTENT_MANAGER"],
  salesCrm: ["ADMIN", "SYSTEM_ADMIN", "SALES_CRM"],
  panelAccess: ["ADMIN", "SYSTEM_ADMIN", "CONTENT_MANAGER", "SALES_CRM"],
} as const;

export type RoleGroup = keyof typeof ROLE_GROUPS;

export function hasRole(userRole: string | undefined, group: RoleGroup): boolean {
  if (!userRole) return false;
  return (ROLE_GROUPS[group] as readonly string[]).includes(userRole);
}

export const ROLE_LABELS: Record<string, string> = {
  SALES_CRM: "Satış/CRM",
  CONTENT_MANAGER: "Kontent Meneceri",
  ADMIN: "Admin",
  SYSTEM_ADMIN: "Sistem Admini",
};
