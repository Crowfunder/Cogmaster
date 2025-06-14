using Discord;
using Microsoft.Extensions.Caching.Memory;

namespace Cogmaster.Src.Helpers;

public interface IDiscordPaginator
{
    (Embed Page, string Icon) GetPage(MemoryCacheEntryOptions cacheOptions, string pagesKey, string userKey, string action, int index = 0);
    ComponentBuilder GetComponents(string pagesKey, string userKey, string baseId);
    void AddPageCounterAndSaveToCache(MemoryCacheEntryOptions cacheOptions, IList<EmbedBuilder> pages, string pagesKey, string filePath, bool addTitle = false);
}
