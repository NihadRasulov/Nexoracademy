function getAdminBasePath(): string {
  const path = new URL(document.baseURI).pathname.replace(/\/+$/, "");
  if (!path || path === "/") {
    throw new Error("Admin base path was not configured by the host.");
  }

  return path;
}

export const ADMIN_BASE_PATH = getAdminBasePath();
