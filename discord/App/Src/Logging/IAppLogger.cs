using Cogmaster.Src.Enums;
using Discord;

namespace Cogmaster.Src.Logging;

public interface IAppLogger
{
    void Log(LogLevel level, string message);
    Task HandleDiscordLog(LogMessage msg);
}
