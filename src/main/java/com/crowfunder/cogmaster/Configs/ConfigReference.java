package com.crowfunder.cogmaster.Configs;

public class ConfigReference {

    private final String implementationType = "com.threerings.config.ConfigReference";

    // Path to derived config
    // References don't have their own path identifiers!
    private final Path derivedPath;

    private final String sourceConfig;

    // Overridden parameters
    private final ParameterArray parameters;

    public Path getPath() {
        return this.derivedPath;
    }

    public ParameterArray getParameters() {
        return this.parameters;
    }

    public String getSourceConfig() { return this.sourceConfig; }

    public String getImplementationType() { return this.implementationType; }

    public ConfigReference(String sourceConfig) {
        this.derivedPath = new Path();
        this.parameters = new ParameterArray();
        this.sourceConfig = sourceConfig;
    }

    public ConfigReference(String sourceConfig, Path derivedPath) {
        this.derivedPath = derivedPath;
        this.parameters = new ParameterArray();
        this.sourceConfig = sourceConfig;
    }

    public ConfigReference(String sourceConfig, Path derivedPath, ParameterArray parameters) {
        this.derivedPath = derivedPath;
        this.parameters = parameters;
        this.sourceConfig = sourceConfig;
    }

    public ConfigReference(ConfigEntry sourceEntry) {
        this.derivedPath = sourceEntry.getPath();
        this.sourceConfig = sourceEntry.getSourceConfig();

        // By default, reference does not alter any parameters of the config
        this.parameters = new ParameterArray();
    }

}
