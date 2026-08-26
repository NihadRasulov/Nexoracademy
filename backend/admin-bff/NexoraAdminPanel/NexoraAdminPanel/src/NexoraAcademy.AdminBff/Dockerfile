# NOTE: build context must be the repository root (e.g. `docker build .`),
# because the build copies both the BFF project (backend/admin-bff) and the
# admin SPA (frontend/admin-ui) whose `dist` gets bundled into wwwroot.
FROM mcr.microsoft.com/dotnet/sdk:10.0 AS build
RUN apt-get update && apt-get install -y --no-install-recommends curl && \
    curl -fsSL https://deb.nodesource.com/setup_22.x | bash - && \
    apt-get install -y --no-install-recommends nodejs && \
    rm -rf /var/lib/apt/lists/*
WORKDIR /build

COPY backend/admin-bff/NexoraAdminPanel/NexoraAdminPanel/src/NexoraAcademy.AdminBff/ NexoraAcademy.AdminBff/
COPY frontend/admin-ui/ /frontend/admin-ui/

WORKDIR /build/NexoraAcademy.AdminBff
RUN dotnet restore
RUN dotnet publish -c Release -o /app/publish

FROM mcr.microsoft.com/dotnet/aspnet:10.0
# Pull latest security patches for openssl and other system libs
RUN apt-get update && apt-get upgrade -y --no-install-recommends \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=build /app/publish .
EXPOSE 8080
ENTRYPOINT ["dotnet", "NexoraAcademy.AdminBff.dll"]
