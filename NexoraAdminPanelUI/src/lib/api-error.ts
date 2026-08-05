export interface ErrorResponse {
  code: string;
  message: string;
  fieldErrors?: Record<string, string> | null;
}

export class ApiError extends Error {
  code: string;
  status: number;
  fieldErrors?: Record<string, string> | null;

  constructor(status: number, body: ErrorResponse) {
    super(body.message);
    this.name = "ApiError";
    this.status = status;
    this.code = body.code;
    this.fieldErrors = body.fieldErrors;
  }
}
