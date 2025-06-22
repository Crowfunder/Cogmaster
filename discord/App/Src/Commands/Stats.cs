using Cogmaster.Src.Data.Classes;
using Cogmaster.Src.Handlers;
using Cogmaster.Src.Helpers;
using Discord;
using Discord.Interactions;
using Microsoft.Extensions.Caching.Memory;
using System.Text.Json;

namespace Cogmaster.Src.Commands;

public class Stats(IMemoryCache cache, IEmbedHandler embedHandler, IApiFetcher apiFetcher, IApp app) : InteractionModuleBase<SocketInteractionContext>
{
    private readonly MemoryCacheEntryOptions CacheOptions = new() { AbsoluteExpirationRelativeToNow = TimeSpan.FromDays(1) };
    private const string Cachekey = "api_stats";

    [SlashCommand(CommandIds.Stats, "Statistics about the Cogmaster app.")]
    public async Task ExecuteAsync()
    {
        if (!cache.TryGetValue(Cachekey, out JsonElement[]? apiStats) || apiStats is null)
        {
            var configStats = await apiFetcher.FetchDocumentAsync($"{DotNetEnv.Env.GetString("api")}/index/info/stats");
            var translationStats = await apiFetcher.FetchDocumentAsync($"{DotNetEnv.Env.GetString("api")}/translations/info/stats");

            apiStats = configStats == null || translationStats == null ? [] : [configStats.RootElement, translationStats.RootElement];
            if (apiStats.Length > 0) cache.Set(Cachekey, apiStats, CacheOptions);
        }

        var fields = new List<EmbedFieldBuilder>()
        {
            new EmbedFieldBuilder().WithName("App Id").WithValue(app.Client.CurrentUser.Id).WithIsInline(true),
            new EmbedFieldBuilder().WithName("Running Since").WithValue($"<t:{app.ReadyTimeStamp}:f>").WithIsInline(true),
            new EmbedFieldBuilder().WithName("Round-trip Latency").WithValue($"{app.Client.Latency}ms").WithIsInline(true),
        };

        ExtractStats(apiStats, fields);

        await ModifyOriginalResponseAsync(msg => msg.Embed = embedHandler.GetEmbed("Cogmaster Statistics").WithFields(fields).Build());
    }

    private static void ExtractStats(JsonElement[] stats, List<EmbedFieldBuilder> fields)
    {
        foreach (var section in stats)
        {
            foreach (var stat in section.EnumerateObject())
            {
                fields.Add(new EmbedFieldBuilder().WithName(stat.Name).WithValue($"{stat.Value.GetInt32():N0}").WithIsInline(true));
            }
        }
    }
}
