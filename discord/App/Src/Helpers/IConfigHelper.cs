using Discord;
using Microsoft.Extensions.Caching.Memory;

namespace Cogmaster.Src.Helpers;

public interface IConfigHelper
{
    MemoryCacheEntryOptions CacheOptions { get; }
    Task CreateConfigPagesAsync(string url, string cacheKey, string item);
    MessageComponent GetComponents(string pagesKey, string userKey, string baseId);
}
