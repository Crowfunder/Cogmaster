using Cogmaster.Src.Data.Classes;
using Cogmaster.Src.Enums;
using Cogmaster.Src.Handlers;
using Cogmaster.Src.Helpers;
using Discord;
using Discord.Interactions;

namespace Cogmaster.Src.Commands;

[Group(CommandIds.Configs, "Get the configs from any item.")]
public class Configs(IEmbedHandler embedHandler, IDiscordPaginator paginator, IConfigHelper configHelper) : InteractionModuleBase<SocketInteractionContext>
{
    [SlashCommand(CommandIds.ConfigsName, "Get the configs from any item by in-game name.")]
    public async Task NameCommand([Summary(description: "In-game name of the config entry, i.e \"Brandish\"."), Autocomplete(), MinLength(3), MaxLength(69)] string name) =>
        await HandleCommandAsync($"{DotNetEnv.Env.GetString("api")}/index/search?q={name}", name);

    [SlashCommand(CommandIds.ConfigsPath, "Get the configs from any item by path.")]
    public async Task PathCommand(
        [Summary(name: "config-entry-path", description: "Path of the entry in the selected config, i.e \"Weapon/Sword/Troika\"."), Autocomplete(), MinLength(3), MaxLength(69)] string path,
        [Summary(name: "config-name", description: "Name of the config in which we are searching for, i.e \"item\", \"actor\"."), Autocomplete(), MinLength(3), MaxLength(69)] string name) =>
            await HandleCommandAsync($"{DotNetEnv.Env.GetString("api")}/index/config/{name}?path={path}", $"{name}_{path}");

    private async Task HandleCommandAsync(string url, string title)
    {
        var cacheKey = $"{CommandIds.Configs}_{title}";
        var matchFound = await configHelper.CreateConfigPagesAsync(url, cacheKey, title);

        switch (matchFound)
        {
            case ConfigResult.Success: await RespondAsync(cacheKey); break;
            case ConfigResult.Menu: await RespondAsync($"{cacheKey}_{ComponentIds.Menu}", forMenu: true); break;
            default:
                await ModifyOriginalResponseAsync(msg =>
                {
                    msg.Embed = embedHandler.GetEmbed(title).WithDescription("Failed to find config or something went wrong.").Build();
                    msg.Components = new ComponentBuilder().Build();
                });
                break;
        }
    }

    private async Task RespondAsync(string cacheKey, bool forMenu = false)
    {
        var userCacheKey = $"{cacheKey}_{Context.User.Id}";
        var (Page, Icon) = paginator.GetPage(configHelper.CacheOptions, cacheKey, userCacheKey, string.Empty);
        var files = Icon == string.Empty ? new List<FileAttachment>() : [new(Icon)];

        await ModifyOriginalResponseAsync(msg =>
        {
            msg.Embed = Page;
            msg.Components = forMenu ? configHelper.GetMenuComponents(cacheKey, userCacheKey, $"{ComponentIds.Menu}{ComponentIds.ConfigBase}") : configHelper.GetComponents(cacheKey, userCacheKey, ComponentIds.ConfigBase);
            msg.Attachments = files;
        });
    }
}
