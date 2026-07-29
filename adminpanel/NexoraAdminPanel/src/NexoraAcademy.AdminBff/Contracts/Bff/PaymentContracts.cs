namespace NexoraAcademy.AdminBff.Contracts.Bff;

public record PaymentRequest(
    Guid? EnrollmentId, string? Method, decimal? Amount, string? Currency, string? ExternalTxnId,
    string? IdempotencyKey, List<Dictionary<string, object>>? Installments);

public record PaymentResponse(
    Guid Id, Guid EnrollmentId, string Method, decimal Amount, string Currency, string Status,
    string? ExternalTxnId, string IdempotencyKey, List<Dictionary<string, object>>? Installments,
    decimal RefundAmount, string? RefundReason, DateTimeOffset InitiatedAt, DateTimeOffset? CapturedAt,
    string? FailureReason);
