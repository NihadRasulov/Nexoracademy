FROM mcr.microsoft.com/dotnet/sdk:10.0 AS build
RUN apt-get update && apt-get install -y --no-install-recommends curl && \
    curl -fsSL https://deb.nodesource.com/setup_22.x | bash - && \
    apt-get install -y --no-install-recommends nodejs && \
    rm -rf /var/lib/apt/lists/*
WORKDIR /build

COPY NexoraAdminPanel/NexoraAdminPanel/src/NexoraAcademy.AdminBff/ NexoraAcademy.AdminBff/
COPY NexoraAdminPanelUI/ /NexoraAdminPanelUI/

WORKDIR /build/NexoraAcademy.AdminBff
RUN dotnet restore
RUN dotnet publish -c Release -o /app/publish

FROM mcr.microsoft.com/dotnet/aspnet:10.0
WORKDIR /app
COPY --from=build /app/publish .
EXPOSE 8080
ENTRYPOINT ["dotnet", "NexoraAcademy.AdminBff.dll"]
