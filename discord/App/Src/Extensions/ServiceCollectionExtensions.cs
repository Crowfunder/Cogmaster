using Cogmaster.Src.Handlers;
using Cogmaster.Src.Helpers;
using Cogmaster.Src.Logging;
using Discord.Interactions;
using DotNetEnv;
using Microsoft.Extensions.DependencyInjection;

namespace Cogmaster.Src.Extensions;

public static class ServiceCollectionExtensions
{
    public static IServiceCollection ConfigureCoreServices(this IServiceCollection services)
    {
        Env.TraversePath().Load();

        return services
            .AddMemoryCache()
            .AddSingleton<IApp, App>()
            .AddSingleton<IAppLogger, Logger>()
            .AddSingleton(x => new InteractionService(x.GetRequiredService<IApp>().Client));
    }

    public static IServiceCollection ConfigureHandlers(this IServiceCollection services)
    {
        return services
            .AddSingleton<IInteractionHandler, InteractionHandler>()
            .AddSingleton<IEmbedHandler, EmbedHandler>();
    }

    public static IServiceCollection ConfigureHelpers(this IServiceCollection services)
    {
        return services
            .AddSingleton<IApiFetcher, ApiFetcher>()
            .AddSingleton<IDiscordPaginator, DiscordPaginator>()
            .AddSingleton<IConfigHelper, ConfigHelper>();
    }
}
