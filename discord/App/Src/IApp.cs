using Discord.WebSocket;

namespace Cogmaster.Src;

public interface IApp
{
    DiscordSocketClient Client { get; }
    long ReadyTimeStamp { get; }
    Task StartAsync();
    void Dispose();
}
