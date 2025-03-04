package com.crowfunder.cogmaster.Routers;

import com.crowfunder.cogmaster.Configs.ConfigEntry;
import com.crowfunder.cogmaster.Configs.ConfigReference;
import com.crowfunder.cogmaster.Configs.ParameterArray;
import com.crowfunder.cogmaster.Configs.Path;

import java.util.Map;


public class RoutedConfig {

    private final Router sourceRouter;

    // We won't be referencing the full config in the routed config
    // To reduce bandwidth usage, needs to be resolved from ConfigReference
    private final ConfigReference sourceConfig;
    private final ParameterArray routedParameters;

    public Router getSourceRouter() {
        return sourceRouter;
    }

    public ConfigReference getSourceConfig() {
        return sourceConfig;
    }

    public ParameterArray getRoutedParameters() {
        return routedParameters;
    }

    // Populate parameters array according to routes in Router
    private ParameterArray populateRoutedParameters(ConfigEntry sourceConfig, Router sourceRouter) {
        ParameterArray parameters = new ParameterArray();
        for (Map.Entry<String, Path> e: sourceRouter.getRoutes().entrySet()) {
            parameters.addParameter(e.getKey(), sourceConfig.getParameters().resolveParameterPath(e.getValue()));
        }
        return parameters;
    }

    public RoutedConfig(ConfigEntry sourceConfig, Router sourceRouter) {
        this.sourceConfig = new ConfigReference(sourceConfig);
        this.sourceRouter = sourceRouter;
        this.routedParameters = populateRoutedParameters(sourceConfig, sourceRouter);
    }
}
