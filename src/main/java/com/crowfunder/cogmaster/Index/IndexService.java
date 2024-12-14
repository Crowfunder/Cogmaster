package com.crowfunder.cogmaster.Index;

import com.crowfunder.cogmaster.Configs.ConfigEntry;
import com.crowfunder.cogmaster.Configs.ConfigReference;

import java.util.HashMap;
import java.util.List;

class IndexService {

    // The base PathIndex that maps config paths to their respective ConfigEntry objects.
    private HashMap<String, ConfigEntry> pathIndex;

    // Index mapping specific parameter values to ConfigEntry paths from pathIndex
    private HashMap<String, HashMap<String, List<String>>> parameterIndex;

    // Get ConfigEntry object by its config path
    public ConfigEntry resolveConfig(String path) {
        return this.pathIndex.get(path);
    }

    // Get ConfigEntry object by resolving a ConfigReference object
    public ConfigEntry resolveConfig(ConfigReference configReference) {
        return this.pathIndex.get(configReference.getPath());
    }

    // Reverse search by specific parameter names
    public List<String> getConfigsByParameter(String paramName, String paramValue) {
        return this.parameterIndex.get(paramName).get(paramValue);
    }

    public IndexService(HashMap<String, ConfigEntry> pathIndex, HashMap<String, HashMap<String, List<String>>> parameterIndex) {
        this.pathIndex = pathIndex;
        this.parameterIndex = parameterIndex;
    }

}