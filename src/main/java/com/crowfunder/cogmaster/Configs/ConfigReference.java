package com.crowfunder.cogmaster.Configs;

import java.util.HashMap;
import java.util.Objects;

public class ConfigReference implements Exportable {

    // Path to derived config
    // References don't have their own path identifiers!
    private final Path derivedPath;

    // Overridden parameters
    private final ParameterArray parameters;

    public Path getPath() {
        return this.derivedPath;
    }

    public ParameterArray getParameters() {
        return this.parameters;
    }

    public String toJSONString() {
        StringBuilder out = new StringBuilder();
        out.append("{");
        out.append("\"type\": \"").append("ConfigReference").append("\",");
        out.append("\"path\": \"").append(derivedPath).append("\",");
        out.append("\"parameters\": ");
        out.append(getParameters().toJSONString());
        return out.append("}").toString();
    }

    public ConfigReference(Path derivedPath) {
        this.derivedPath = derivedPath;
        this.parameters = new ParameterArray();
    }

    public ConfigReference(Path derivedPath, ParameterArray parameters) {
        this.derivedPath = derivedPath;
        this.parameters = parameters;
    }

}
