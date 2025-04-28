using Cogmaster.Src.Data.Classes;
using Discord;

namespace Cogmaster.Src.Handlers;

public class EmbedHandler : IEmbedHandler
{
    public EmbedBuilder GetEmbed(string title)
    {
        return new EmbedBuilder
        {
            Title = title[..Math.Min(title.Length, ExtendedDiscordConfig.MaxEmbedTitleChars)],
            Color = Colors.Default
        };
    }
}
