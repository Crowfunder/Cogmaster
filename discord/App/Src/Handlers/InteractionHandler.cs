﻿using Cogmaster.Src.Enums;
using Cogmaster.Src.Helpers;
using Cogmaster.Src.Logging;
using Discord;
using Discord.Interactions;
using Discord.WebSocket;
using Microsoft.Extensions.Caching.Memory;
using System.Reflection;

namespace Cogmaster.Src.Handlers;

public class InteractionHandler(IApp app, IAppLogger logger, IMemoryCache cache, IFileReader jsonFileReader, IServiceProvider services, InteractionService service) : IInteractionHandler
{
    public async Task InitializeAsync()
    {
        await service.AddModulesAsync(Assembly.GetEntryAssembly(), services);
        service.InteractionExecuted += logger.HandlePostInteractionAsync;
    }

    public async Task RegisterCommandsAsync()
    {
        await service.RegisterCommandsGloballyAsync();
        logger.Log(LogLevel.Discord, "Commands have been registered");
    }

    public async Task HandleInteractionAsync(SocketInteraction interaction)
    {
        if (interaction.Type == InteractionType.ApplicationCommandAutocomplete)
        {
            await HandleAutocompleteAsync((SocketAutocompleteInteraction)interaction);
            return;
        }

        await interaction.DeferAsync(ephemeral: true);

        var context = new SocketInteractionContext(app.Client, interaction);
        await service.ExecuteCommandAsync(context, services);
    }

    private async Task HandleAutocompleteAsync(SocketAutocompleteInteraction interaction)
    {
        var fileName = interaction.Data.Current.Name switch
        {
            "name" => "Items.json",
            _ => throw new InvalidOperationException($"Unknown autocomplete option: {interaction.Data.Current.Name}")
        };
        var cacheKey = $"Autocomplete_{fileName}";

        if (!cache.TryGetValue(cacheKey, out List<AutocompleteResult>? suggestions) || suggestions is null)
        {
            var items = await jsonFileReader.ReadAsync<IReadOnlyList<string>>(Path.Combine("Data", "Autocomplete", fileName));
            suggestions = items.Select(x => new AutocompleteResult(x, x)).ToList();
            cache.Set(cacheKey, suggestions, new MemoryCacheEntryOptions() { AbsoluteExpirationRelativeToNow = TimeSpan.FromDays(7) });
        }

        var input = (interaction.Data.Current.Value as string)?.Split(' ', StringSplitOptions.RemoveEmptyEntries | StringSplitOptions.TrimEntries) ?? [];
        var matches = new List<AutocompleteResult>(25);

        foreach (var s in suggestions)
        {
            if (input.All(word => s.Name.Contains(word, StringComparison.OrdinalIgnoreCase)))
            {
                matches.Add(s);
                if (matches.Count == 25) break;
            }
        }

        await interaction.RespondAsync(matches);
    }
}
