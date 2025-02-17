package com.crowfunder.cogmaster.Configs;

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

    // Return effective name, may be replaced by implementation routes in the future
    // Name gets priority as it's often used as a parameter overriding name in derivation
    public String getName() {
        if (this.getParameters().resolveParameterPath("Name") != null) {
            return this.getParameters().resolveParameterPath("Name").toString();
        } else if (this.getParameters().resolveParameterPath("name") != null) {
            return this.getParameters().resolveParameterPath("name").toString();
        } else {
            return null;
        }


    }

    public void loadReference(ConfigReference reference) {
        this.derivedPath.setPath(reference.getPath());
        this.parameters.update(reference.getParameters());
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
    }

}
