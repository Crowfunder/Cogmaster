using Cogmaster.Src.Data.Classes;
using Cogmaster.Src.Handlers;
using Discord;
using Discord.Interactions;

namespace Cogmaster.Src.Commands;

[Group(CommandIds.Help, "Information about the app & commands.")]
public class Help(IEmbedHandler embedHandler) : InteractionModuleBase<SocketInteractionContext>
{
    [SlashCommand(CommandIds.HelpOverview, "Overview of the Cogmaster app.")]
    public async Task HelpOverviewCommand()
    {
        var fields = new List<EmbedFieldBuilder>
        {
            new() {
                Name = "Commands",
                Value = "For now, Cogmaster supports two commands:\n" +
                $"- {Format.Code("/configs")} - Read config entry. Get item/enemy/particle information.\n" +
                $"- {Format.Code("/translate")} - Translate a string key into value (or other way round) extracted from projectx-config\n" +
                $"Check out {Format.Code("/help configs")} and {Format.Code("/help translate")} respectively for more info!"
            },
            new() {
                Name = "Links",
                Value = Format.Bold(Format.Url("Official Github Repository", "https://github.com/Crowfunder/Cogmaster"))
            },
            new() {
                Name = "Credits",
                Value = $"- {MentionUtils.MentionUser(291952806926090240)}\n - {MentionUtils.MentionUser(214787913097936896)}\n - {MentionUtils.MentionUser(224586010271547393)}\n - {MentionUtils.MentionUser(265691931097432064)}\n - {MentionUtils.MentionUser(857941871833251881)}"
            }
        };

        var embed = embedHandler.GetEmbed("Help")
            .WithDescription("Cogmaster is a bot providing info extracted from Spiral Knights configs. " +
            " Are you looking for an item model file? Are you looking for Glacius explosion color? " +
            "Do you find decompiling .dats and scrambling through .xmls tedious? Cogmaster is here to help!")
            .WithFields(fields);

        await ModifyOriginalResponseAsync(msg => msg.Embed = embed.Build());
    }

    [SlashCommand(CommandIds.HelpConfig, "Information about the /configs command.")]
    public async Task HelpConfigCommand()
    {
        var info = "Command retrieving data from specific config entry. Contains things like entries in-game name, location of its .dat model, location of its inventory icon, damage values, particle preferences etc.\n" +
            Format.Bold($"Note: All file paths are relative to {Format.Code("Spiral Knights/rsrc")}, so if you see a path like {Format.Code("ui/icon/file.png")}\nit actually is {Format.Code("Spiral Knights/rsrc/ui/icon/file.png")}.");
        var commands = $"{Format.Header("Commands"),2}\n{Format.Header(Format.Code("/configs name"), 3)}\n{Format.Quote("Searches for config entry by its in-game name. " + Format.Bold("Not all entries support this search method."))}\n" +
            $"{Format.Bold("Parameters:")}\n- name - In-game name of an item, i.e {Format.Code("Brandish")}\n{Format.Header(Format.Code("/configs path"), 3)}\n{Format.Quote("Searches for config entry by specifying the config it's located within, and the path within that config.")}\n" +
            $"{Format.Bold("Parameters:")}\n- config-name - Name of the config where the entry is located, i.e {Format.Code("item")}\n- config-entry-path - Path within the config specified in config-name, i.e {Format.Code("Weapon/Sword/Brandish")}";
        var views = $"{Format.Header("Views", 2)}\nThe returned config entries can be shown in four (or less) separate views. All of them provide different info about the retrieved entry.\n" +
            $"- {Format.Bold("Basic")} - Bare minimum, most important info about the entry. Usually all you need. Config Path denotes where this entry is located in config specified by Source Config.\n" +
            $"- {Format.Bold("Full")} - All effective entry configurations.\n- {Format.Bold("Parent")} - If the entry derives from (is a child of) another entry, displays the parent's info.\n" +
            $"- {Format.Bold("Own")} - If the entry derives from another entry, display all info not taken from parent.";
        var embed = embedHandler.GetEmbed("Help Configs")
            .WithDescription($"{info}\n{commands}\n{views}");

        await ModifyOriginalResponseAsync(msg => msg.Embed = embed.Build());
    }

    [SlashCommand(CommandIds.HelpTranslate, "Information about the /translate command.")]
    public async Task HelpTranslateCommand()
    {
        var info = $"Command translating or reverse searching string keys used in game and defined in {Format.Code("projectx-config.jar")}. Turns \"m.brandish\" into \"Brandish\" and the other way around. " +
            $"It also supports parsing concatenated translation keys, such as {Format.Code("a.shd|a.sniped\\!m.buccaneer_bicorne")} into \"Shadow Sniped Buccaneer Bicorne\".";
        var commands = $"{Format.Header("Commands"),2}\n{Format.Header(Format.Code("/translate key"), 3)}\n{Format.Quote("Parses and translates a translation key into a string.")}\n" +
            $"{Format.Bold("Parameters:")}\n- key - Translation key, i.e {Format.Code("m.brandish")}\n" +
            $"{Format.Header(Format.Code("/translate value"), 3)}\n{Format.Quote($"Attempts to reverse search value for matching keys. {Format.Bold("Does not work for values resulting from concatenated translation keys, i.e \"Shadow Sniped Buccaneer Bicorne\" will not work!")}")}\n" +
            $"{Format.Bold("Parameters:")}\n- value - Translation key value, i.e {Format.Code("Brandish")}";
        var embed = embedHandler.GetEmbed("Help Translate")
            .WithDescription($"{info}\n{commands}");

        await ModifyOriginalResponseAsync(msg => msg.Embed = embed.Build());
    }
}
