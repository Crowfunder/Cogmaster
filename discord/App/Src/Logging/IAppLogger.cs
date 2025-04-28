using Cogmaster.Src.Enums;
using Discord;
using Discord.Interactions;

namespace Cogmaster.Src.Logging;

public interface IAppLogger
{
    void Log(LogLevel level, string message);
    Task HandlePostInteractionAsync(ICommandInfo command, IInteractionContext context, IResult result);
    Task HandleDiscordLog(LogMessage msg);
}
