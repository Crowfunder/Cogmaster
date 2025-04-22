using Discord.WebSocket;

namespace Cogmaster.Src;

public interface IApp
{
    DiscordSocketClient Client { get; }
    Task StartAsync();
    void Dispose();
}
