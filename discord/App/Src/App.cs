
using Discord;
using Discord.WebSocket;

namespace Cogmaster.Src;

public class App : IApp, IDisposable
{
    public DiscordSocketClient Client { get; private set; }
    public long ReadyTimeStamp { get; private set; }
    private bool _disposed;

    public App()
    {
        DiscordSocketConfig intents = new()
        {
            GatewayIntents = GatewayIntents.None
        };

        Client = new DiscordSocketClient(intents);
        ReadyTimeStamp = new DateTimeOffset(DateTime.UtcNow).ToUnixTimeSeconds();
    }

    public async Task StartAsync()
    {
        await Client.LoginAsync(TokenType.Bot, DotNetEnv.Env.GetString("botToken"));
        await Client.StartAsync();
    }

    public void Dispose()
    {
        Dispose(true);
        GC.SuppressFinalize(this);
    }

    protected virtual void Dispose(bool disposing)
    {
        if (!_disposed)
        {
            if (disposing)
            {
                Client?.Dispose();
            }

            _disposed = true;
        }
    }
}
