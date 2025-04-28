using System.Text.Json;

namespace Cogmaster.Src.Helpers;

public interface IApiFetcher
{
    Task<JsonDocument> FetchAsync(string url);
}
