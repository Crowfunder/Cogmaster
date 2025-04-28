using Cogmaster.Src.Handlers;
using Cogmaster.Src.Helpers;
using Discord.Interactions;
using System.Text.Json;

namespace Cogmaster.Src.Commands;

public class Parameters(IEmbedHandler embedHandler, IApiFetcher apiFetcher) : InteractionModuleBase<SocketInteractionContext>
{

    [SlashCommand("parameters", "Get the parameters from any item.")]
    public async Task ExecuteAsync([Summary(description: "Item the app should return the parameters for."), Autocomplete(), MinLength(3), MaxLength(69)] string item)
    {
        var embed = embedHandler.GetEmbed(item);

        try
        {
            var data = await apiFetcher.FetchAsync($"http://127.0.0.1:8080/api/v1/index/search?q={item}");
            var parameters = data.RootElement[0].GetProperty("routedParameters");
            embed.WithDescription(FormatParameters(parameters));
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
