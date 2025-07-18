using Cogmaster.Src.Data.Classes;
using Cogmaster.Src.Enums;
using Cogmaster.Src.Extensions;
using Cogmaster.Src.Handlers;
using Cogmaster.Src.Models;
using Discord;
using Microsoft.Extensions.Caching.Memory;
using System.Text;
using System.Text.Json;

namespace Cogmaster.Src.Helpers;

public class ConfigHelper(IMemoryCache cache, IEmbedHandler embedHandler, IDiscordPaginator paginator, IApiFetcher apiFetcher) : IConfigHelper
{
    public MemoryCacheEntryOptions CacheOptions { get; private set; } = new() { AbsoluteExpirationRelativeToNow = TimeSpan.FromDays(1) };

    private sealed record ParamInfo(string Type, string Title, string Info, string Id);
    private static readonly List<ParamInfo> _paramTypes =
    [
        new ParamInfo("routedParameters", "Basic Info", string.Empty, ComponentIds.Basic),
        new ParamInfo("parameters", "Full Info", "Full list of effective parameters for this config", ComponentIds.Full),
        new ParamInfo("derivedParameters", "Parent's Parameters", "Parameters derived from parents, that were not overridden by this config's own parameters.", ComponentIds.Parent),
        new ParamInfo("ownParameters", "Own Info", "This config's own parameters, without parameters derived from parents.", ComponentIds.Own)
    ];

    public async Task<ConfigResult> CreateConfigPagesAsync(string url, string cacheKey, string item, int index = -1)
    {
        if (cache.TryGetValue(cacheKey, out PageData? itemParameters) && itemParameters is not null) return ConfigResult.Success;

        var data = await apiFetcher.FetchDocumentAsync(url);
        if (data is null) return ConfigResult.Error;

        var filePath = GetIconUrl(data);
        var author = new EmbedAuthorBuilder().WithName(item).WithIconUrl($"attachment://{Path.GetFileName(filePath)}");

        switch (data.RootElement.ValueKind)
        {
            case JsonValueKind.Array when data.RootElement.GetArrayLength() > 1 && index > -1: CreatePages(cacheKey, data.RootElement[index], filePath, author); return ConfigResult.Success;
            case JsonValueKind.Array when data.RootElement.GetArrayLength() > 1: CreateMenuPages(cacheKey, data.RootElement, filePath, author); return ConfigResult.Menu;
            case JsonValueKind.Array: CreatePages(cacheKey, data.RootElement[0], filePath, author); return ConfigResult.Success;
            default: CreatePages(cacheKey, data.RootElement, filePath, author); return ConfigResult.Success;
        }
    }

    private void CreatePages(string cacheKey, JsonElement data, string filePath, EmbedAuthorBuilder author)
    {
        var pages = new List<EmbedBuilder>();
        var indexes = new Dictionary<string, ParameterIndexData>();

        foreach (var param in _paramTypes)
        {
            var paramIndex = pages.Count;
            var paramData = data.GetProperty(param.Type);
            var chunks = SplitIntoChunks(FormatParameters(paramData), chunkSize: ExtendedDiscordConfig.MaxEmbedDescChars - 1000);

            if (chunks.Count > 0) chunks[0] = $"{GetExtraProperties(data, param.Id)}{chunks[0]}";
            indexes.Add(param.Id, new ParameterIndexData(paramIndex, param.Id.ToTitleCase(), param.Id, Disabled: chunks.Count == 0));
            pages.AddRange(chunks.Select(page => embedHandler.GetEmbed(param.Title).WithAuthor(author).WithDescription($"{param.Info}\n\n{page}")));
        }

        cache.Set($"{cacheKey}_index", indexes, CacheOptions);
        paginator.AddPageCounterAndSaveToCache(CacheOptions, [.. pages], cacheKey, filePath, addTitle: true);
    }

    private void CreateMenuPages(string cacheKey, JsonElement data, string filePath, EmbedAuthorBuilder author)
    {
        var param = _paramTypes.First();
        var pages = new List<EmbedBuilder>();

        foreach (var item in data.EnumerateArray())
        {
            var page = $"{GetExtraProperties(item, param.Id)}{FormatParameters(item.GetProperty(param.Type))}";

            pages.Add(embedHandler.GetEmbed("Found Multiple Entries").WithAuthor(author).WithDescription($"{param.Info}\n\n{page}"));
        }

        paginator.AddPageCounterAndSaveToCache(CacheOptions, [.. pages], $"{cacheKey}_{ComponentIds.Menu}", filePath, addTitle: true);
    }

    private static string GetIconUrl(JsonDocument data)
    {
        var fallbackIcon = Path.Combine("Src", "Assets", "ui/icon/icon_128.png");
        var paramData = data.RootElement.ValueKind == JsonValueKind.Array ? data.RootElement[0] : data.RootElement;
        if (!paramData.GetProperty("routedParameters").TryGetProperty("hashMap", out JsonElement hashMap)) return fallbackIcon;
        if (!hashMap.TryGetProperty("icon", out JsonElement icon)) return fallbackIcon;

        var path = Path.Combine("Src", "Assets", icon.GetProperty("value").ToString());

        if (File.Exists(path)) return path;

        return fallbackIcon;
    }

    private static string GetExtraProperties(JsonElement data, string id)
    {
        return id switch
        {
            ComponentIds.Basic => $"{Format.Bold("Config Path")}: {data.GetProperty("path").GetProperty("path").GetString()}\n{Format.Bold("SourceConfig")}: {data.GetProperty("sourceConfig").GetString()}\n\n",
            ComponentIds.Parent => $"{Format.Bold("Derived Path")}: {data.GetProperty("derivedPath").GetProperty("path").GetString()}\n{Format.Bold("SourceConfig")}: {data.GetProperty("sourceConfig").GetString()}\n\n",
            _ => string.Empty
        };
    }

    public MessageComponent GetComponents(string pagesKey, string userKey, string baseId)
    {
        var components = paginator.GetComponents(pagesKey, userKey, baseId);

        if (cache.TryGetValue($"{pagesKey}_index", out Dictionary<string, ParameterIndexData>? data) && data is not null)
        {
            var row = new ActionRowBuilder();

            foreach (var info in data.Values)
            {
                row.AddComponent(new ButtonBuilder()
                    .WithLabel(info.Label).WithCustomId(ComponentIds.ConfigBase + info.Id).WithStyle(ButtonStyle.Secondary).WithDisabled(info.Disabled).Build());
            }

            components.AddRow(row);
        }

        return components.Build();
    }

    public MessageComponent GetMenuComponents(string pagesKey, string userKey, string baseId)
    {
        var components = paginator.GetComponents(pagesKey, userKey, baseId);

        components.AddRow(new ActionRowBuilder().WithButton("More Info", $"{baseId}{ComponentIds.More}", ButtonStyle.Secondary));
        return components.Build();
    }

    private static string FormatParameters(JsonElement parameters, int indentLevel = 0)
    {
        var builder = new StringBuilder();
        var indent = new string(' ', indentLevel * 2);

        if (parameters.ValueKind != JsonValueKind.String && parameters.TryGetProperty("hashMap", out JsonElement hashMap))
        {
            foreach (var property in hashMap.EnumerateObject())
            {
                var key = property.Name;
                var valueWrapper = property.Value;

                if (valueWrapper.ValueKind == JsonValueKind.Null)
                {
                    builder.AppendLine($"{Format.Bold(key)}: Null");
                    continue;
                }

                var isNested = valueWrapper.GetProperty("nested").GetBoolean();
                var value = valueWrapper.GetProperty("value");

                if (isNested)
                {
                    builder.AppendLine($"{indent}{key}:");
                    builder.Append(FormatParameters(value, indentLevel + 1));
                }
                else
                {
                    if (value.ValueKind == JsonValueKind.Array && value.GetArrayLength() > 0)
                    {
                        builder.AppendLine($"{indent}{key}: [");

                        foreach (var item in value.EnumerateArray())
                        {
                            var itemValue = item.GetProperty("value");
                            builder.Append(FormatParameters(itemValue, indentLevel + 2));
                        }

                        builder.AppendLine($"{indent}]");
                    }
                    else
                    {
                        var formatted = value.ValueKind switch
                        {
                            JsonValueKind.String when key.Equals("rarity", StringComparison.InvariantCultureIgnoreCase) => value.GetString()?.ConvertRarity() ?? string.Empty,
                            JsonValueKind.String when key.Equals("itemprop", StringComparison.InvariantCultureIgnoreCase) => value.GetString()?.ToUpperInvariant().ConvertToIcon() ?? string.Empty,
                            JsonValueKind.String when key.Equals("location", StringComparison.InvariantCultureIgnoreCase) => value.GetString()?.ToUpperInvariant().ConvertIfAccessorySlot() ?? string.Empty,
                            JsonValueKind.String => value.GetString(),
                            JsonValueKind.Number => value.ToString(),
                            JsonValueKind.True => "true",
                            JsonValueKind.False => "false",
                            _ => value.ToString()
                        };

                        builder.AppendLine($"{Format.Bold($"{indent}{key}")}: {formatted}");
                    }
                }
            }
        }
        else
        {
            builder.AppendLine(parameters.GetString()?.ConvertIfAccessorySlot() ?? string.Empty);
        }

        return builder.ToString();
    }

    private static List<string> SplitIntoChunks(string input, int chunkSize)
    {
        var chunks = new List<string>();
        int currentIndex = 0;

        while (currentIndex < input.Length)
        {
            int length = Math.Min(chunkSize, input.Length - currentIndex);

            int lastNewline = input.LastIndexOf('\n', currentIndex + length - 1, length);
            if (lastNewline > currentIndex)
            {
                length = lastNewline - currentIndex + 1;
            }

            chunks.Add(input.Substring(currentIndex, length));
            currentIndex += length;
        }

        return chunks;
    }
}
