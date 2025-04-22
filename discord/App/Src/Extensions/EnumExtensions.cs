using Cogmaster.Src.Enums;

namespace Cogmaster.Src.Extensions;
public static class EnumExtensions
{
    // Logcolor extensions
    private static readonly Dictionary<LogLevel, string> LogLevelMapping = new()
    {
        { LogLevel.Command, "\u001b[34m" },
        { LogLevel.Discord, "\u001b[90m" },
        { LogLevel.Error, "\u001b[31m" }
    };

    public static string Color(this LogLevel level) =>
        LogLevelMapping.TryGetValue(level, out var color) ? color : "\u001b[37m";
}
