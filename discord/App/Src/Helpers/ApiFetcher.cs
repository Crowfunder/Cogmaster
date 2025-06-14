using System.Text.Json;

namespace Cogmaster.Src.Helpers;

public class ApiFetcher : IApiFetcher
{
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
}
