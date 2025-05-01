using Discord;

namespace Cogmaster.Src.Handlers;

public interface IEmbedHandler
{
    EmbedBuilder GetEmbed(string title);
    Embed GetAndBuildEmbed(string title);
}
