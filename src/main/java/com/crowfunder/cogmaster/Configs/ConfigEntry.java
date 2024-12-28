package com.crowfunder.cogmaster.Configs;

public class ConfigEntry implements Exportable {

    // Own identifier config path
    private final Path path;

    // Overriden/Own parameters
    private final ParameterArray parameters;

    // If the config is a derived config, this path points to derivative (parent) config
    private final Path derivedPath;

    // Non-overriden parameters pulled from all derivative (parent) configs
    private final ParameterArray derivedParameters;

    // Source config name
    private final String sourceConfig;

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

    public void updateOwnParameters(ParameterArray newParameters) {
        parameters.update(newParameters);
    }

    public void updateDerivedParameters(ParameterArray newParameters) {
        derivedParameters.update(newParameters);
    }

    public boolean isDerived() {
        return this.derivedPath.getPath() != null;
    }

    public String toJSONString() {
        StringBuilder out = new StringBuilder();
        out.append("{");
        out.append("\"path\": \"").append(this.path).append("\",");
        out.append("\"sourceConfig\": \"").append(this.sourceConfig).append("\",");
        if (isDerived()) {
            out.append("\"type\": \"").append("DerivedConfig").append("\",");
            out.append("\"derivedPath\": \"").append(this.derivedPath).append("\",");
        }
        else {
            out.append("\"type\": \"").append("BaseConfig").append("\",");
        }
        out.append("\"parameters\": ");
        out.append(getParameters().toJSONString());
        return out.append("}").toString();
    }

    // Parameterless
    public ConfigEntry(String sourceConfig) {
        this.sourceConfig = sourceConfig;
        this.path = new Path();
        this.parameters = new ParameterArray();
        this.derivedPath = new Path();   // Empty string for no derivation
        this.derivedParameters = new ParameterArray();
    }

    // Base (Non-derived) class constructor
    public ConfigEntry(String sourceConfig, Path path, ParameterArray parameters) {
        this.sourceConfig = sourceConfig;
        this.path = path;
        this.parameters = parameters;
        this.derivedPath = new Path();   // Empty string for no derivation
        this.derivedParameters = new ParameterArray();
    }

    public ConfigEntry(String sourceConfig, Path path, Path derivedPath, ParameterArray parameters, ParameterArray derivedParameters) {
        this.sourceConfig = sourceConfig;
        this.path = path;
        this.parameters = parameters;
        this.derivedPath = derivedPath;
        this.derivedParameters = derivedParameters;
    }

    public void loadReference(ConfigReference reference) {
        this.derivedPath.setPath(reference.getPath());
        this.parameters.update(reference.getParameters());
    }
}
