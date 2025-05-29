# Build Stage
FROM mcr.microsoft.com/dotnet/sdk:8.0 AS build
WORKDIR /App

COPY *.csproj ./
RUN dotnet restore

COPY . ./
RUN dotnet publish "./Cogmaster.csproj" -c Release -o out --no-restore

# Runtime Stage
FROM mcr.microsoft.com/dotnet/runtime:8.0
WORKDIR /App
COPY --from=build /App/out .
ENTRYPOINT ["dotnet", "Cogmaster.dll"]
