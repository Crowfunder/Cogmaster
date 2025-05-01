using Cogmaster.Src.Data.Classes;
using Cogmaster.Src.Extensions;
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

    public Embed GetAndBuildEmbed(string title) =>
        GetEmbed(title).Build();
}
