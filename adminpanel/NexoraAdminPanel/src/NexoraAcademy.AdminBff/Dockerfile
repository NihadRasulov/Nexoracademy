# syntax=docker/dockerfile:1

# --- Build stage -------------------------------------------------------
FROM mcr.microsoft.com/dotnet/sdk:10.0-alpine AS build
WORKDIR /src

COPY NexoraAcademy.AdminBff.csproj .
RUN dotnet restore NexoraAcademy.AdminBff.csproj

COPY . .
RUN dotnet publish NexoraAcademy.AdminBff.csproj -c Release -o /app/publish --no-restore

# --- Runtime stage -------------------------------------------------------
FROM mcr.microsoft.com/dotnet/aspnet:10.0-alpine
WORKDIR /app

RUN addgroup -S bff && adduser -S bff -G bff
USER bff:bff

COPY --from=build --chown=bff:bff /app/publish .

ENV ASPNETCORE_URLS=http://+:8080
ENV ASPNETCORE_ENVIRONMENT=Production
EXPOSE 8080

# /api/health/backend proxies to the Spring Boot backend's own health check,
# so this also reports unhealthy while the backend is unreachable — that's
# intentional, since the BFF isn't functional without it either.
HEALTHCHECK --interval=30s --timeout=5s --start-period=20s --retries=3 \
    CMD wget -qO- http://localhost:8080/api/health/backend || exit 1

ENTRYPOINT ["dotnet", "NexoraAcademy.AdminBff.dll"]
