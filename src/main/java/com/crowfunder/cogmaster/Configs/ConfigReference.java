package com.crowfunder.cogmaster.Configs;

import java.util.HashMap;

public class ConfigReference implements Exportable {

    // Path to derived config
    // References don't have their own path identifiers!
    private final String derivedPath;

    // Overridden parameters
    private HashMap<String, Parameter> parameters;

    public String getPath() {
        return this.derivedPath;
    }

    public HashMap<String, Parameter> getParameters() {
        return this.parameters;
    }

    public String toJSONString() {
        return "";
    }

    public ConfigReference(String derivedPath) {
        this.derivedPath = derivedPath;
    }

    public ConfigReference(String derivedPath, HashMap<String, Parameter> parameters) {
        this.derivedPath = derivedPath;
        this.parameters = parameters;
    }

}
