using Cogmaster.Src.Enums;
using Discord;
using Microsoft.Extensions.Caching.Memory;

namespace Cogmaster.Src.Helpers;

public interface IConfigHelper
{
    MemoryCacheEntryOptions CacheOptions { get; }
    Task<ConfigResult> CreateConfigPagesAsync(string url, string cacheKey, string item, int index = -1);
    MessageComponent GetComponents(string pagesKey, string userKey, string baseId);
}
