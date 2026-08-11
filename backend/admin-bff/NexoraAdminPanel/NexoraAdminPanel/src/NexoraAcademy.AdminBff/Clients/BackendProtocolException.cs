namespace NexoraAcademy.AdminBff.Clients;

public sealed class BackendProtocolException(string message, Exception? innerException = null)
    : Exception(message, innerException);
