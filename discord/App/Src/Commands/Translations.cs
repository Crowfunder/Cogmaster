using Cogmaster.Src.Data.Classes;
using Cogmaster.Src.Handlers;
using Cogmaster.Src.Helpers;
using Discord;
using Discord.Interactions;
using Microsoft.Extensions.Caching.Memory;
using System.Text.Json;

namespace Cogmaster.Src.Commands;

[Group(CommandIds.Translations, "Resolves and reverse-searches names from i18n translations.")]
public class Translations(IMemoryCache cache, IEmbedHandler embedHandler, IApiFetcher apiFetcher, IConfigHelper configHelper) : InteractionModuleBase<SocketInteractionContext>
{
    [SlashCommand(CommandIds.TranslationsValue, "Get the translation names that match the given value.")]
    public async Task ValueCommand([Summary(description: "Reverse search for translation value, i.e \"Brandish\""), MinLength(3), MaxLength(69)] string value) =>
        await HandleCommandAsync($"{DotNetEnv.Env.GetString("api")}/translations/value?q={value}", value, "No translations matching the value.");

    [SlashCommand(CommandIds.TranslationsKey, "Translate the given key.")]
    public async Task KeyCommand([Summary(description: "Resolve translation name, i.e \"m.brandish\"."), MinLength(3), MaxLength(69)] string key) =>
        await HandleCommandAsync($"{DotNetEnv.Env.GetString("api")}/translations/key?q={key}", key, "Failed to find translation.");

    private async Task HandleCommandAsync(string url, string searched, string error)
    {
        var cacheKey = $"{CommandIds.Translations}_{searched}";
        var matchFound = await StoreTranslationAsync(url, cacheKey);
        var description = matchFound ? cache.Get<string>(cacheKey) : error;

        await ModifyOriginalResponseAsync(msg => msg.Embed = embedHandler.GetEmbed(searched).WithDescription(description).Build());
    }

    private async Task<bool> StoreTranslationAsync(string url, string cacheKey)
    {
        if (cache.TryGetValue(cacheKey, out string? match) && !string.IsNullOrEmpty(match)) return true;

        var data = await apiFetcher.FetchDocumentAsync(url);
        if (data is null) return false;

        var cached = data.RootElement.ValueKind == JsonValueKind.Array ?
            string.Concat(data.RootElement.EnumerateArray().Select(DecorateElement)) : DecorateElement(data.RootElement);

        cache.Set(cacheKey, cached, configHelper.CacheOptions);
        return true;
    }

    private static string DecorateElement(JsonElement value) =>
        $"- {Format.Bold(value.GetProperty("value").GetString())}\n";
}
