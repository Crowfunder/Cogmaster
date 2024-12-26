package com.crowfunder.cogmaster.Configs;

import java.util.Objects;

public class ConfigEntry implements Exportable {

    // Own identifier config path
    private final Path path;

    // Overriden/Own parameters
    private final ParameterArray parameters;

    // If the config is a derived config, this path points to derivative (parent) config
    private final Path derivedPath;

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

    public boolean isDerived() {
        return this.derivedPath.getPath() != null;
    }

    public String toJSONString() {
        StringBuilder out = new StringBuilder();
        out.append("{");
        out.append("\"path\": \"").append(this.path).append("\",");
        if (!Objects.equals(derivedPath, "")) {
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
    public ConfigEntry() {
        this.path = new Path("");
        this.parameters = new ParameterArray();
        this.derivedPath = new Path("");   // Empty string for no derivation
        this.derivedParameters = new ParameterArray();
    }

    // Base (Non-derived) class constructor
    public ConfigEntry(Path path, ParameterArray parameters) {
        this.path = path;
        this.parameters = parameters;
        this.derivedPath = new Path("");   // Empty string for no derivation
        this.derivedParameters = new ParameterArray();
    }

    public ConfigEntry(Path path, Path derivedPath, ParameterArray parameters, ParameterArray derivedParameters) {
        this.path = path;
        this.parameters = parameters;
        this.derivedPath = derivedPath;
        this.derivedParameters = derivedParameters;
    }


}
