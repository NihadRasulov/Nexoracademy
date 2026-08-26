export const ROLE_GROUPS = { admin: ["ADMIN"] } as const;

export type RoleGroup = keyof typeof ROLE_GROUPS;

export function hasRole(userRole: string | undefined, group: RoleGroup): boolean {
  if (!userRole) return false;
  return (ROLE_GROUPS[group] as readonly string[]).includes(userRole);
}

export const ROLE_LABELS: Record<string, string> = {
  ADMIN: "Administrator",
};
