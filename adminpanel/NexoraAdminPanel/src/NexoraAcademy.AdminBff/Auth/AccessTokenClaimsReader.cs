using System.IdentityModel.Tokens.Jwt;

namespace NexoraAcademy.AdminBff.Auth;

public static class AccessTokenClaimsReader
{
    public static (Guid UserId, string Role) Read(string accessToken)
    {
        var jwt = new JwtSecurityTokenHandler().ReadJwtToken(accessToken);
        var sub = jwt.Claims.First(c => c.Type == "sub").Value;
        var role = jwt.Claims.First(c => c.Type == "role").Value;
        return (Guid.Parse(sub), role);
    }
}
