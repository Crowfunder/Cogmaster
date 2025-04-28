using Cogmaster.Src.Handlers;
using Discord.Interactions;

namespace Cogmaster.Src.Commands;

public class Parameters(IEmbedHandler embedHandler) : InteractionModuleBase<SocketInteractionContext>
{
    [SlashCommand("parameters", "Get the parameters from any item.")]
    public async Task ExecuteAsync([Summary(description: "Item the app should return the parameters for."), Autocomplete(), MinLength(3), MaxLength(69)] string item)
    {
        await ModifyOriginalResponseAsync(msg => msg.Embed = embedHandler.GetEmbed(item).Build());
    }
}
