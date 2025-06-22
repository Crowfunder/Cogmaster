using Cogmaster.Src.Data.Classes;
using Cogmaster.Src.Enums;
using Cogmaster.Src.Extensions;
using Cogmaster.Src.Handlers;
using Discord;
using Discord.Interactions;
using Discord.WebSocket;

namespace Cogmaster.Src.Logging;

public class Logger(IApp app, IEmbedHandler embedHandler) : IAppLogger
{
    private const ulong LogsChannelId = 1382404390714871870;
    private const ulong ownerId = 214787913097936896;

    public void Log(LogLevel level, string message) =>
        Console.WriteLine($"{level.Color()}[{DateTime.Now:yyyy-MM-dd HH:mm:ss}]\u001b[0m {message}");

    public async Task HandlePostInteractionAsync(ICommandInfo command, IInteractionContext context, IResult result)
    {
        if (!result.IsSuccess)
        {
            await HandleErrorAsync(command.Name, context.Interaction, (ExecuteResult)result);
            return;
        }

        switch (context.Interaction.Type)
        {
            case InteractionType.ApplicationCommand: await HandleCommandAsync(command.Name, context.Interaction); break;
            case InteractionType.MessageComponent: Log(LogLevel.Button, $"{context.Interaction.User.Username} used {((IComponentInteraction)context.Interaction).Data.CustomId}"); break;
            default: return;
        }
    }

    public Task HandleDiscordLog(LogMessage msg)
    {
        var message = $"{msg.Source}\t{msg.Message}";
        if (!string.IsNullOrEmpty(msg.Message)) Log(LogLevel.Discord, message);

        return Task.CompletedTask;
    }

    private async Task LogAsync(string? message = null, Embed? embed = null, bool pingOwner = false)
    {
        if (string.IsNullOrEmpty(message) && embed is null) return;
        if (await app.Client.GetChannelAsync(LogsChannelId) is not IMessageChannel channel) return;
        var msg = pingOwner ? string.Join(" ", MentionUtils.MentionUser(ownerId), message) : message;

        await channel.SendMessageAsync(msg, embed: embed);
    }

    private async Task HandleCommandAsync(string command, IDiscordInteraction interaction)
    {
        var data = (SocketSlashCommandData)interaction.Data;
        var cmd = data.Options.Count > 0 ? data.Options.First() : null;
        var desc = cmd?.Options.Count > 0 ? ExtractOptions(cmd.Options) : string.Empty;

        var embed = embedHandler.GetEmbed($"/{data.Name} {(data.Name == command ? string.Empty : command)}")
            .WithAuthor(new EmbedAuthorBuilder().WithName(interaction.User.Username).WithIconUrl(interaction.User.GetDisplayAvatarUrl()))
            .WithFooter(new EmbedFooterBuilder().WithText($"ID: {interaction.User.Id}"))
            .WithDescription(desc)
            .WithCurrentTimestamp();

        Log(LogLevel.Command, $"{interaction.User.Username} used /{data.Name} {command}");
        await LogAsync(embed: embed.Build());
    }

    private async Task HandleErrorAsync(string command, IDiscordInteraction interaction, ExecuteResult result)
    {
        var interactionName = interaction.Type switch
        {
            InteractionType.ApplicationCommand => (interaction as SocketCommandBase)?.CommandName,
            InteractionType.MessageComponent => (interaction as IComponentInteraction)?.Data.CustomId,
            _ => command
        };
        if (string.IsNullOrEmpty(interactionName)) interactionName = command;

        var description = result.Error switch
        {
            InteractionCommandError.UnmetPrecondition => "Unmet Precondition.",
            InteractionCommandError.UnknownCommand => "It looks like this command is missing.",
            InteractionCommandError.BadArgs => "Invalid number of arguments given.",
            InteractionCommandError.Exception => "An exception was thrown during execution.",
            InteractionCommandError.Unsuccessful => "Command could not be executed.",
            InteractionCommandError.ConvertFailed => "Failed to convert one or more parameters.",
            InteractionCommandError.ParseFailed => "Failed to parse the command.",
            _ => "Unknown reason."
        };
        var userEmbed = embedHandler.GetEmbed("Something went wrong while executing this command.")
            .WithDescription(description)
            .WithColor(Colors.Error);

        var stackTrace = result.Exception.InnerException?.StackTrace;
        var errorEmbed = embedHandler.GetEmbed($"Error while executing {Format.Underline(interactionName)} for {Format.Underline(interaction.User.Username)}")
            .WithColor(Colors.Error)
            .WithDescription(string.Join("\n\n", result.Exception.InnerException?.Message, stackTrace?.Substring(0, Math.Min(stackTrace.Length, ExtendedDiscordConfig.MaxEmbedDescChars))))
            .WithFooter(new EmbedFooterBuilder().WithText($"ID: {interaction.User.Id}"))
            .WithCurrentTimestamp();

        Log(LogLevel.Error, result.Exception.InnerException?.Message ?? description);
        await LogAsync(embed: errorEmbed.Build(), pingOwner: true);

        await interaction.ModifyOriginalResponseAsync(msg =>
        {
            msg.Embed = userEmbed.Build();
            msg.Components = new ComponentBuilder().Build();
        });
    }

    private static string ExtractOptions(IReadOnlyCollection<SocketSlashCommandDataOption> options) =>
        string.Join("\n", options.Select(o => $"- {Format.Bold(o.Name)}: {o.Value}"));
}
