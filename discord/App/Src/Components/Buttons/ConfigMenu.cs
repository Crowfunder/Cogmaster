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

        switch (action)
        {
            case ComponentIds.More: await SelectConfigAsync(oldEmbed); break;
            default: await ContinueMenuAsync(oldEmbed, action); break;
        }
    }

    private async Task SelectConfigAsync(Embed embed)
    {
        var dashPos = embed.Title.IndexOf('-');
        var slashPos = embed.Title.IndexOf('/');
        var pageIndex = int.Parse(embed.Title.Substring(dashPos + 2, slashPos - dashPos - 2).Trim());
        var configName = embed.Description.Split('\n').First().Split("/").Last();

        var url = $"{DotNetEnv.Env.GetString("api")}/index/search?q={embed.Author!.Value.Name}";
        var cacheKey = $"{CommandIds.Configs}_{configName}";
        var userCacheKey = $"{cacheKey}_{Context.User.Id}";

        await configHelper.CreateConfigPagesAsync(url, cacheKey, configName, pageIndex - 1);

        var (Page, Icon) = paginator.GetPage(configHelper.CacheOptions, cacheKey, userCacheKey, string.Empty);
        var files = Icon == string.Empty ? new List<FileAttachment>() : [new(Icon)];

        await ModifyOriginalResponseAsync(msg =>
        {
            msg.Embed = Page;
            msg.Components = configHelper.GetComponents(cacheKey, userCacheKey, ComponentIds.ConfigBase);
            msg.Attachments = files;
        });
    }

    private async Task ContinueMenuAsync(Embed embed, string action)
    {
        var pageCacheKey = $"{CommandIds.Configs}_{embed.Author!.Value.Name}_{ComponentIds.Menu}";
        var userCacheKey = $"{pageCacheKey}_{Context.User.Id}";
        var (Page, Icon) = paginator.GetPage(configHelper.CacheOptions, pageCacheKey, userCacheKey, action);
        var files = Icon == string.Empty ? new List<FileAttachment>() : [new(Icon)];

        await ModifyOriginalResponseAsync(msg =>
        {
            msg.Embed = Page;
            msg.Components = configHelper.GetMenuComponents(pageCacheKey, userCacheKey, BaseId);
            msg.Attachments = files;
        });
    }
}
