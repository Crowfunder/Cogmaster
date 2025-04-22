using Cogmaster.Src.Enums;
using Cogmaster.Src.Extensions;
using Discord;

namespace Cogmaster.Src.Logging;
public class Logger : IAppLogger
{
    public void Log(LogLevel level, string message) =>
        Console.WriteLine($"{level.Color()}[{DateTime.Now:yyyy-MM-dd HH:mm:ss}]\u001b[0m {message}");

    public Task HandleDiscordLog(LogMessage msg)
    {
        var message = $"{msg.Source}\t{msg.Message}";
        if (!string.IsNullOrEmpty(msg.Message)) Log(LogLevel.Discord, message);

        return Task.CompletedTask;
    }
}
