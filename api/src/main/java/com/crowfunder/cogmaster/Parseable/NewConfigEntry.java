package com.crowfunder.cogmaster.Parseable;

import java.util.ArrayList;

import com.crowfunder.cogmaster.Configs.ParameterArray;
import com.crowfunder.cogmaster.Configs.ParameterValue;
import com.crowfunder.cogmaster.Configs.Path;

public class NewConfigEntry {
    // name of config file from where the entry was parsed
    public final String configFileName;
    // comes from <name></name>
    public final Path path;
    public String implementationType;

    public NewConfigEntryReference parentReference;
    // public final ArrayList<NewConfigEntry> childEntries; // temporarily disable populating parent to child paths

    // does not contain parent parameters
    public final ParameterArray entryParameters;
    public final ParameterArray routedParameters;
    // // Non-overriden parameters pulled from all derivative (parent) configs
    // public final ParameterArray derivedParameters;

    // Parameterless
    public NewConfigEntry(String configFileName) {
        this.configFileName = configFileName;
        this.path = new Path();
        this.implementationType = "";
        // this.childEntries = new ArrayList<NewConfigEntry>();
        this.entryParameters = new ParameterArray();
        this.routedParameters = new ParameterArray();
        // this.derivedParameters = new ParameterArray();
    }

    // get the implementation type of the very first parent(root) node
    public String getRootImplementationType() {
        if (parentReference == null) {
            return implementationType;
        }
        return parentReference.referencedEntry.getRootImplementationType();
    }

    // returns parameters, overriding parent parameter with child's if they exist
    public ParameterArray getEffectiveParameters() {
        if (parentReference == null)
            return entryParameters;

        // accumulate params:
        // parent's effective parameters get overwritten by any parameters included in the child reference
        // resulting parameters get overwritten by any parameters directly contained in the child
        // Note: as of now, most of the time the parameters are defined in the reference 
        return entryParameters.derive(parentReference.getParameters()).derive(parentReference.referencedEntry.getEffectiveParameters());
    }

    // Return effective name using routes
    public String getName() {
        ParameterValue name = routedParameters.resolveParameterPath("name");
        if (name != null) {
            return name.toString();
        }
        return null;
    }

    public ParameterArray getRoutedParameters() {
        return this.routedParameters;
    }
}
