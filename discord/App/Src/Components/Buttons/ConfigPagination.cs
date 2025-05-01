using Cogmaster.Src.Data.Classes;
using Cogmaster.Src.Helpers;
using Cogmaster.Src.Models;
using Discord;
using Discord.Interactions;
using Discord.WebSocket;
using Microsoft.Extensions.Caching.Memory;

namespace Cogmaster.Src.Components.Buttons;

public class ConfigPagination(IMemoryCache cache, IDiscordPaginator paginator, IConfigHelper configHelper) : InteractionModuleBase<SocketInteractionContext>
{
    private static readonly List<string> _indexIds = [ComponentIds.Basic, ComponentIds.Full, ComponentIds.Parent, ComponentIds.Own];

    [ComponentInteraction($"{ComponentIds.ConfigBase}*")]
    public async Task ExecuteAsync(string action)
    {
        var context = (SocketMessageComponent)Context.Interaction;
        var oldEmbed = context.Message.Embeds.First();
        var pageCacheKey = $"{CommandIds.Configs}_{oldEmbed.Author!.Value.Name}";
        var userCacheKey = $"{pageCacheKey}_{Context.User.Id}";
        var embed = _indexIds.Contains(action) ? GetPageWithIndex(pageCacheKey, userCacheKey, action) : paginator.GetPage(configHelper.CacheOptions, pageCacheKey, userCacheKey, action);

        await ModifyOriginalResponseAsync(msg =>
        {
            msg.Embed = embed;
            msg.Components = configHelper.GetComponents(pageCacheKey, userCacheKey, ComponentIds.ConfigBase);
        });
    }

    private Embed GetPageWithIndex(string pageCacheKey, string userCacheKey, string action)
    {
        if (!cache.TryGetValue($"{pageCacheKey}_index", out Dictionary<string, ParameterIndexData>? data) || data is null) return paginator.GetPage(configHelper.CacheOptions, pageCacheKey, userCacheKey, action);

        return paginator.GetPage(configHelper.CacheOptions, pageCacheKey, userCacheKey, action, data[action]?.Index ?? 0);
    }
}
