package com.crowfunder.cogmaster.Configs;

import java.util.HashMap;

public class ConfigEntry implements Exportable {

    // Own identifier config path
    private final String path;

    // Overriden/Own parameters
    private HashMap<String, Parameter> parameters;

    // If the config is a derived config, this path points to derivative (parent) config
    private final String derivedPath;

    // Non-overriden parameters pulled from all derivative (parent) configs
    // Parameters that were overriden will not be placed in this HashMap
    private HashMap<String, Parameter> derivedParameters;

    public String getPath() {
        return this.derivedPath;
    }

    public HashMap<String, Parameter> getParameters() {
        return this.parameters;
    }

    // Method for cascading tostring for returning to api.
    public String toJSONString() {
        return "";
    }

    // Base (Non-derived) class constructor
    public ConfigEntry(String path, HashMap<String, Parameter> parameters) {
        this.path = path;
        this.parameters = parameters;
        this.derivedPath = "";   // Empty string for no derivation 
        this.derivedParameters = new HashMap<>();
    }

    public ConfigEntry(String path, String derivedPath, HashMap<String, Parameter> parameters, HashMap<String, Parameter> derivedParameters) {
        this.path = path;
        this.parameters = parameters;
        this.derivedPath = derivedPath;
        this.derivedParameters = derivedParameters;
    }

}
