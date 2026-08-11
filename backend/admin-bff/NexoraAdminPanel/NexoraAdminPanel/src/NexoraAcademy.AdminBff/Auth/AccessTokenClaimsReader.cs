using System.IdentityModel.Tokens.Jwt;
using Microsoft.IdentityModel.Tokens;
using NexoraAcademy.AdminBff.Clients;

namespace NexoraAcademy.AdminBff.Auth;

public static class AccessTokenClaimsReader
{
    public static Guid ReadSubject(string accessToken)
    {
        try
        {
            var jwt = new JwtSecurityTokenHandler().ReadJwtToken(accessToken);
            var subject = jwt.Claims.FirstOrDefault(claim => claim.Type == "sub")?.Value;

            if (!Guid.TryParse(subject, out var userId))
            {
                throw new BackendProtocolException(
                    "The backend access token is missing a valid subject claim.");
            }

            return userId;
        }
        catch (BackendProtocolException)
        {
            throw;
        }
        catch (Exception ex) when (ex is ArgumentException or SecurityTokenException)
        {
            throw new BackendProtocolException(
                "The backend returned an unreadable access token.",
                ex);
        }
    }
}
