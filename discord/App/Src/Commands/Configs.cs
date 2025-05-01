using Cogmaster.Src.Data.Classes;
using Cogmaster.Src.Handlers;
using Cogmaster.Src.Helpers;
using Discord;
using Discord.Interactions;

namespace Cogmaster.Src.Commands;

public class Configs(IEmbedHandler embedHandler, IDiscordPaginator paginator, IConfigHelper configHelper) : InteractionModuleBase<SocketInteractionContext>
{


    [SlashCommand(CommandIds.Configs, "Get the configs from any item.")]
    public async Task ExecuteAsync([Summary(description: "Item the app should return the configs for."), Autocomplete(), MinLength(3), MaxLength(69)] string item)
    {
        var cacheKey = $"{CommandIds.Configs}_{item}";
        var userCacheKey = $"{cacheKey}_{Context.User.Id}";

        try
        {
            await configHelper.CreateConfigPagesAsync($"{DotNetEnv.Env.GetString("api")}/search?q={item}", cacheKey, item);

            await ModifyOriginalResponseAsync(msg =>
            {
                msg.Embed = paginator.GetPage(configHelper.CacheOptions, cacheKey, userCacheKey, string.Empty);
                msg.Components = configHelper.GetComponents(cacheKey, userCacheKey, ComponentIds.ConfigBase);
            });
        }
        catch
        {
            await ModifyOriginalResponseAsync(msg =>
            {
                msg.Embed = embedHandler.GetEmbed(item).WithDescription("Failed to find item or something went wrong.").Build();
                msg.Components = new ComponentBuilder().Build();
            });
        }
    }

}
