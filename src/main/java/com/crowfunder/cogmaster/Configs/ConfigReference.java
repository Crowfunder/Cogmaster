package com.crowfunder.cogmaster.Configs;

import java.util.HashMap;
import java.util.Objects;

public class ConfigReference implements Exportable {

    // Path to derived config
    // References don't have their own path identifiers!
    private final String derivedPath;

    // Overridden parameters
    private final ParameterArray parameters;

    public String getPath() {
        return this.derivedPath;
    }

    public ParameterArray getParameters() {
        return this.parameters;
    }

    public String toJSONString() {
        StringBuilder out = new StringBuilder();
        out.append("{");
        out.append("\"type\": \"").append("BaseConfig").append("\",");
        out.append("\"parameters\": ");
        out.append(getParameters().toJSONString());
        return out.append("}").toString();
    }

    public ConfigReference(String derivedPath) {
        this.derivedPath = derivedPath;
        this.parameters = new ParameterArray();
    }

    public ConfigReference(String derivedPath, ParameterArray parameters) {
        this.derivedPath = derivedPath;
        this.parameters = parameters;
    }

}
