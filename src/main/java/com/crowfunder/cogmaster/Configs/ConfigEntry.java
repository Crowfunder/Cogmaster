package com.crowfunder.cogmaster.Configs;

import com.crowfunder.cogmaster.Routers.Router;

import java.util.Map;
import java.util.Objects;

public class ConfigEntry {

    private String implementationType;

    // Derived implementation
    private String derivedImplementationType;

    // Own identifier config path
    private final Path path;

    // If the config is a derived config, this path points to derivative (parent) config
    private final Path derivedPath;

    // Source config name
    private final String sourceConfig;

    // Overriden/Own parameters
    private final ParameterArray parameters;

    private final ParameterArray routedParameters;

    // Non-overriden parameters pulled from all derivative (parent) configs
    private final ParameterArray derivedParameters;

    public Path getPath() {
        return this.path;
    }

    public Path getDerivedPath() {
        return this.derivedPath;
    }

    // This getter returns effective (derived) (not just parameters field)
    // parameters as it's the default behavior
    public ParameterArray getParameters() {
        return this.parameters.derive(derivedParameters);
    }

    public ParameterArray getOwnParameters() {
        return this.parameters;
    }

    public ParameterArray getDerivedParameters() {
        return this.derivedParameters;
    }

    public ParameterArray getRoutedParameters() {
        return this.routedParameters;
    }

    public String getImplementationType() { return this.implementationType; }

    public String getSourceConfig() { return this.sourceConfig; }

    public String getDerivedImplementationType() { return this.derivedImplementationType; }

    public void setImplementationType(String implementationType) { this.implementationType = implementationType; }

    public void setDerivedImplementationType(String derivedImplementationType) { this.derivedImplementationType = derivedImplementationType; }

    public void updateOwnParameters(ParameterArray newParameters) {
        parameters.update(newParameters);
    }

    public void updateDerivedParameters(ParameterArray newParameters) {
        derivedParameters.update(newParameters);
    }

    public boolean isDerived() {
        return this.derivedPath.getPath() != null;
    }

    // Return effective name using routes
    public String getName() {
        ParameterValue name = routedParameters.resolveParameterPath("name");
        if (name != null) {
            return name.toString();
        }
        return null;
    }

    public void loadReference(ConfigReference reference) {
        this.derivedPath.setPath(reference.getPath());
        this.parameters.update(reference.getParameters());
    }

    // Populate parameters array according to routes in Router
    public void populateRoutedParameters(Router sourceRouter) {
        if (sourceRouter == null) {
            return;
        }
        for (Map.Entry<String, Path> e: sourceRouter.getRoutes().entrySet()) {
            routedParameters.addParameter(e.getKey(), getParameters().resolveParameterPathFlex(e.getValue()));
        }
    }

    // Parameterless
    public ConfigEntry(String sourceConfig) {
        this.sourceConfig = sourceConfig;
        this.path = new Path();
        this.parameters = new ParameterArray();
        this.derivedPath = new Path();   // Empty string for no derivation
        this.derivedParameters = new ParameterArray();
        this.implementationType = "";
        this.derivedImplementationType = "";
        this.routedParameters = new ParameterArray();
    }

}
