export interface PagedResult<T> {
  items: T[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

export interface MeResponse {
  id: string;
  email: string;
  fullName: string;
  phone?: string | null;
  role: string;
  status: string;
  locale: string;
  lastLoginAt?: string | null;
}
