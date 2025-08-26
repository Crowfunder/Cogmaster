package com.crowfunder.cogmaster.Parseable;

import com.crowfunder.cogmaster.Configs.ParameterArray;
import com.crowfunder.cogmaster.Configs.Path;

public class NewConfigEntryReference {

    private final String implementationType = "com.threerings.config.ConfigReference";
    // name of config file from where the reference was parsed
    private final String sourceConfigFileName;
    // comes from <name></name>
    private final Path path;
    // Overridden parameters
    private final ParameterArray parameters;
    // entry pointed to by this config. populated after creation
    public NewConfigEntry referencedEntry;

    public NewConfigEntryReference(String sourceConfig) {
        this.path = new Path();
        this.parameters = new ParameterArray();
        this.sourceConfigFileName = sourceConfig;
    }

    public Path getPath() {
        return this.path;
    }

    public ParameterArray getParameters() {
        return this.parameters;
    }

    public String getSourceConfigFileName() {
        return this.sourceConfigFileName;
    }

    public String getImplementationType() {
        return this.implementationType;
    }
}
