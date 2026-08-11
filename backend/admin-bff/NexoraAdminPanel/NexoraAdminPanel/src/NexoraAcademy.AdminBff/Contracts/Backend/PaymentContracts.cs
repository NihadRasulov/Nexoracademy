using System.Text.Json.Serialization;

namespace NexoraAcademy.AdminBff.Contracts.Backend;

public record PaymentRequest(
    [property: JsonPropertyName("enrollmentId")] Guid? EnrollmentId,
    [property: JsonPropertyName("method")] string? Method,
    [property: JsonPropertyName("amount")] decimal? Amount,
    [property: JsonPropertyName("currency")] string? Currency,
    [property: JsonPropertyName("externalTxnId")] string? ExternalTxnId,
    [property: JsonPropertyName("idempotencyKey")] string? IdempotencyKey,
    [property: JsonPropertyName("installments")] List<Dictionary<string, object>>? Installments);

public record PaymentResponse(
    [property: JsonPropertyName("id")] Guid Id,
    [property: JsonPropertyName("enrollmentId")] Guid EnrollmentId,
    [property: JsonPropertyName("method")] string Method,
    [property: JsonPropertyName("amount")] decimal Amount,
    [property: JsonPropertyName("currency")] string Currency,
    [property: JsonPropertyName("status")] string Status,
    [property: JsonPropertyName("externalTxnId")] string? ExternalTxnId,
    [property: JsonPropertyName("idempotencyKey")] string IdempotencyKey,
    [property: JsonPropertyName("installments")] List<Dictionary<string, object>>? Installments,
    [property: JsonPropertyName("refundAmount")] decimal RefundAmount,
    [property: JsonPropertyName("refundReason")] string? RefundReason,
    [property: JsonPropertyName("initiatedAt")] DateTimeOffset InitiatedAt,
    [property: JsonPropertyName("capturedAt")] DateTimeOffset? CapturedAt,
    [property: JsonPropertyName("failureReason")] string? FailureReason);
