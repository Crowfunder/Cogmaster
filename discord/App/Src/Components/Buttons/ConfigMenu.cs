using Cogmaster.Src.Data.Classes;
using Cogmaster.Src.Helpers;
using Discord;
using Discord.Interactions;
using Discord.WebSocket;

namespace Cogmaster.Src.Components.Buttons;

public class ConfigMenu(IDiscordPaginator paginator, IConfigHelper configHelper) : InteractionModuleBase<SocketInteractionContext>
{
    private const string BaseId = $"{ComponentIds.Menu}{ComponentIds.ConfigBase}";

    [ComponentInteraction($"{BaseId}*")]
    public async Task ExecuteAsync(string action)
    {
        var context = (SocketMessageComponent)Context.Interaction;
        var oldEmbed = context.Message.Embeds.First();
        var pageCacheKey = $"{CommandIds.Configs}_{oldEmbed.Author!.Value.Name}_{ComponentIds.Menu}";
        var userCacheKey = $"{pageCacheKey}_{Context.User.Id}";
        var (Page, Icon) = paginator.GetPage(configHelper.CacheOptions, pageCacheKey, userCacheKey, action);
        var files = Icon == string.Empty ? new List<FileAttachment>() : [new(Icon)];

        await ModifyOriginalResponseAsync(msg =>
        {
            msg.Embed = Page;
            msg.Components = paginator.GetComponents(pageCacheKey, userCacheKey, BaseId).Build();
            msg.Attachments = files;
        });
    }
}
