using Cogmaster.Src.Data.Classes;
using Cogmaster.Src.Handlers;
using Cogmaster.Src.Helpers;
using Discord;
using Discord.Interactions;
using System.Text.Json;

namespace Cogmaster.Src.Commands;

public class Stats(IEmbedHandler embedHandler, IApp app, IApiFetcher apiFetcher) : InteractionModuleBase<SocketInteractionContext>
{
    [SlashCommand(CommandIds.Stats, "Statistics about the Cogmaster app.")]
    public async Task ExecuteAsync()
    {
        var fields = new List<EmbedFieldBuilder>()
        {
            new EmbedFieldBuilder().WithName("App Id").WithValue(app.Client.CurrentUser.Id).WithIsInline(true),
            new EmbedFieldBuilder().WithName("Running Since").WithValue($"<t:{app.ReadyTimeStamp}:f>").WithIsInline(true),
            new EmbedFieldBuilder().WithName("Round-trip Latency").WithValue($"{app.Client.Latency}ms").WithIsInline(true),
        };

        ExtractStats(await apiFetcher.GetApiStatsAsync(), fields);

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
