using NexoraAcademy.AdminBff.Contracts.Backend;

namespace NexoraAcademy.AdminBff.Clients;

public class BackendApiException : Exception
{
    public int StatusCode { get; }
    public BackendErrorResponse? Backend { get; }

    public BackendApiException(int statusCode, BackendErrorResponse? backend)
        : base(backend?.Message ?? $"Backend returned status {statusCode}")
    {
        StatusCode = statusCode;
        Backend = backend;
    }
}
