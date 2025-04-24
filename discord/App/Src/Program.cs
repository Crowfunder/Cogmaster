using Cogmaster.Src.Extensions;
using Cogmaster.Src.Handlers;
using Cogmaster.Src.Logging;
using Microsoft.Extensions.DependencyInjection;

namespace Cogmaster.Src;

internal sealed class Program
{
    public static async Task Main()
    {
        var services = new ServiceCollection()
            .ConfigureCoreServices()
            .ConfigureHandlers()
            .ConfigureHelpers()
            .BuildServiceProvider();

        await StartAppAsync(services);
    }

    private static async Task StartAppAsync(ServiceProvider services)
    {
        var app = services.GetRequiredService<IApp>();
        using (services)
        {
            await services.GetRequiredService<IInteractionHandler>().InitializeAsync();
            AttachClientEvents(services);
            await app.StartAsync();
            await Task.Delay(-1);
        }

        app.Dispose();
    }

    private static void AttachClientEvents(ServiceProvider services)
    {
        var client = services.GetRequiredService<IApp>().Client;
        var interactionHandler = services.GetRequiredService<IInteractionHandler>();

        client.Log += services.GetRequiredService<IAppLogger>().HandleDiscordLog;
        //client.Ready += interactionHandler.RegisterCommandsAsync;
        client.InteractionCreated += interactionHandler.HandleInteractionAsync;
    }
}
