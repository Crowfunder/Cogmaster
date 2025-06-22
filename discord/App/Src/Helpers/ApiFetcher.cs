using Microsoft.Extensions.Caching.Memory;
using System.Text.Json;

namespace Cogmaster.Src.Helpers;

public class ApiFetcher(IMemoryCache cache) : IApiFetcher
{
    private readonly MemoryCacheEntryOptions CacheOptions = new() { AbsoluteExpirationRelativeToNow = TimeSpan.FromDays(1) };
    private const string Cachekey = "api_stats";

    public async Task<JsonDocument?> FetchDocumentAsync(string url)
    {
        try
        {
            using var client = new HttpClient();
            var response = await client.GetAsync(new Uri(url));
            if (!response.IsSuccessStatusCode) return null;

            var json = await response.Content.ReadAsStringAsync();
            return JsonDocument.Parse(json);
        }
        catch
        {
            return null;
        }
    }

    public async Task<JsonElement[]> GetApiStatsAsync()
    {
        if (!cache.TryGetValue(Cachekey, out JsonElement[]? apiStats) || apiStats is null)
        {
            var configStats = await FetchDocumentAsync($"{DotNetEnv.Env.GetString("api")}/index/info/stats");
            var translationStats = await FetchDocumentAsync($"{DotNetEnv.Env.GetString("api")}/translations/info/stats");

            apiStats = configStats == null || translationStats == null ? [] : [configStats.RootElement, translationStats.RootElement];
            if (apiStats.Length > 0) cache.Set(Cachekey, apiStats, CacheOptions);
        }

        return apiStats;
    }

    
}
