using System.Text.Json;

namespace Cogmaster.Src.Helpers;

public interface IApiFetcher
{
    Task<JsonDocument?> FetchDocumentAsync(string url);
    Task<JsonElement[]> GetApiStatsAsync();
}
