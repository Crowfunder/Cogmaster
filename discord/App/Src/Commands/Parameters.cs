using Cogmaster.Src.Handlers;
using Cogmaster.Src.Helpers;
using Discord.Interactions;
using Microsoft.Extensions.Caching.Memory;
using System.Text.Json;

namespace Cogmaster.Src.Commands;

public class Parameters(IMemoryCache cache, IEmbedHandler embedHandler, IApiFetcher apiFetcher) : InteractionModuleBase<SocketInteractionContext>
{
    private static readonly MemoryCacheEntryOptions _cacheOptions = new() { AbsoluteExpirationRelativeToNow = TimeSpan.FromDays(1) };

    [SlashCommand("parameters", "Get the parameters from any item.")]
    public async Task ExecuteAsync([Summary(description: "Item the app should return the parameters for."), Autocomplete(), MinLength(3), MaxLength(69)] string item)
    {
        var cacheKey = $"parameter_{item}";
        var embed = embedHandler.GetEmbed(item);

        try
        {
            if (!cache.TryGetValue(cacheKey, out string? parameters) || string.IsNullOrEmpty(parameters))
            {
                var data = await apiFetcher.FetchAsync($"http://127.0.0.1:8080/api/v1/index/search?q={item}");
                parameters = FormatParameters(data.RootElement[0].GetProperty("routedParameters"));
                cache.Set(cacheKey, parameters, _cacheOptions);
            }
            
            embed.WithDescription(parameters);
        }
        catch
        {
            embed.WithDescription("Failed to find item.");
        }
        finally
        {
            await ModifyOriginalResponseAsync(msg => msg.Embed = embed.Build());
        }
    }

    private static string FormatParameters(JsonElement parameters)
    {
        if (!parameters.TryGetProperty("hashMap", out JsonElement hashMap))
            return "No parameters available.";

        var lines = new List<string>();

        foreach (var property in hashMap.EnumerateObject())
        {
            var nested = property.Value.GetProperty("nested").GetBoolean();
            var value = property.Value.GetProperty("value");

            if (nested)
            {
                if (value.TryGetProperty("hashMap", out JsonElement nestedHashMap))
                {
                    lines.Add($"**{property.Name}:**");

                    foreach (var nestedProp in nestedHashMap.EnumerateObject())
                    {
                        lines.Add($"- {nestedProp.Name}: {nestedProp.Value.GetProperty("value").GetString()}");
                    }
                }
                else
                {
                    lines.Add($"**{property.Name}:** (nested, but missing hashMap?)");
                }
            }
            else
            {
                lines.Add($"**{property.Name}:** {value.GetString()}");
            }
        }

        return string.Join("\n", lines);
    }
}
