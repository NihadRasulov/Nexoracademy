export interface ErrorResponse {
  code?: string;
  error?: string;
  message?: string;
  fieldErrors?: Record<string, string> | null;
  errors?: Record<string, string> | null;
}

export class ApiError extends Error {
  code: string;
  status: number;
  fieldErrors?: Record<string, string> | null;

  constructor(status: number, body: ErrorResponse) {
    super(body.message || body.error || `Server ${status} xəta qaytardı.`);
    this.name = "ApiError";
    this.status = status;
    this.code = body.code || body.error || "UNKNOWN_ERROR";
    this.fieldErrors = body.fieldErrors || body.errors || null;
  }
}
