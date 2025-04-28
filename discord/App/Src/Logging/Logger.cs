using Cogmaster.Src.Data.Classes;
using Cogmaster.Src.Enums;
using Cogmaster.Src.Extensions;
using Cogmaster.Src.Handlers;
using Discord;
using Discord.Interactions;
using Discord.WebSocket;

namespace Cogmaster.Src.Logging;
public class Logger(IEmbedHandler embedHandler) : IAppLogger
{
    public void Log(LogLevel level, string message) =>
        Console.WriteLine($"{level.Color()}[{DateTime.Now:yyyy-MM-dd HH:mm:ss}]\u001b[0m {message}");

    public async Task HandlePostInteractionAsync(ICommandInfo command, IInteractionContext context, IResult result)
    {
        if (!result.IsSuccess)
        {
            await HandleErrorAsync(command.Name, context.Interaction, (ExecuteResult)result);
            return;
        }

        Log(LogLevel.Command, $"{context.Interaction.User.Username} used /{command}");
    }

    public Task HandleDiscordLog(LogMessage msg)
    {
        var message = $"{msg.Source}\t{msg.Message}";
        if (!string.IsNullOrEmpty(msg.Message)) Log(LogLevel.Discord, message);

        return Task.CompletedTask;
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

        Log(LogLevel.Error, result.Exception.InnerException?.Message ?? description);
        await interaction.ModifyOriginalResponseAsync(msg =>
        {
            msg.Embed = userEmbed.Build();
            msg.Components = new ComponentBuilder().Build();
        });
    }
}
