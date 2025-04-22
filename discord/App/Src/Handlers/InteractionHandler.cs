using Discord.Interactions;
using Discord.WebSocket;
using System.Reflection;

namespace Cogmaster.Src.Handlers;

public class InteractionHandler(IServiceProvider services, InteractionService service) : IInteractionHandler
{
    public async Task InitializeAsync()
    {
        await service.AddModulesAsync(Assembly.GetEntryAssembly(), services);
    }

    public async Task RegisterCommandsAsync()
    {
        await service.RegisterCommandsGloballyAsync();
    }

    public Task HandleInteractionAsync(SocketInteraction interaction)
    {
        throw new NotImplementedException();
    }
}
