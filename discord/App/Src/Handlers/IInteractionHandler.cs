using Discord.WebSocket;

namespace Cogmaster.Src.Handlers;

public interface IInteractionHandler
{
    Task InitializeAsync();
    Task RegisterCommandsAsync();
    Task HandleInteractionAsync(SocketInteraction interaction);
}
