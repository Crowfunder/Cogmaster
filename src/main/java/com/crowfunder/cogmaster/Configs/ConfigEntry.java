package com.crowfunder.cogmaster.Configs;

import java.util.HashMap;
import java.util.Objects;

public class ConfigEntry implements Exportable {

    // Own identifier config path
    private final String path;

    // Overriden/Own parameters
    private ParameterArray parameters;

    // If the config is a derived config, this path points to derivative (parent) config
    private final String derivedPath;

    // Non-overriden parameters pulled from all derivative (parent) configs
    private ParameterArray derivedParameters;

    public String getPath() {
        return this.path;
    }

    public String getDerivedPath() {
        return this.derivedPath;
    }

    // This getter returns effective (derived) (not just parameters field)
    // parameters as it's the default behavior
    public ParameterArray getParameters() {
        return this.parameters.derive(derivedParameters);
    }


    public String toJSONString() {
        StringBuilder out = new StringBuilder();
        out.append("{");
        out.append("\"path\": \"").append(this.path).append("\",");
        if (!Objects.equals(derivedPath, "")) {
            out.append("\"derivedPath\": \"").append(this.derivedPath).append("\",");
        }
        out.append("\"parameters\": ");
        out.append(getParameters().toJSONString());
        return out.append("}").toString();
    }

    // Base (Non-derived) class constructor
    public ConfigEntry(String path, ParameterArray parameters) {
        this.path = path;
        this.parameters = parameters;
        this.derivedPath = "";   // Empty string for no derivation 
        this.derivedParameters = new ParameterArray();
    }

    public ConfigEntry(String path, String derivedPath, ParameterArray parameters, ParameterArray derivedParameters) {
        this.path = path;
        this.parameters = parameters;
        this.derivedPath = derivedPath;
        this.derivedParameters = derivedParameters;
    }

}
