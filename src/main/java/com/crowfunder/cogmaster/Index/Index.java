package com.crowfunder.cogmaster.Index;

import com.crowfunder.cogmaster.Configs.ConfigEntry;
import com.crowfunder.cogmaster.Configs.Path;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Index {
    // The base ConfigIndex that maps config names to hashmaps containing mappings of paths to ConfigEntry objects
    private final Map<String, Map<Path, ConfigEntry>> configIndex = new HashMap<>();

    // Index mapping specific parameter values to ConfigEntry paths from configIndex
    private final Map<Path, Map<String, List<Path>>> parameterIndex = new HashMap<>();

    // Return index for one config
    public Map<Path, ConfigEntry> getPathIndex(String configName) {
        return configIndex.get(configName);
    }

    private void initializePathIndex(String configName) {
        this.configIndex.put(configName, new HashMap<>());
    }

    // Return the entire ConfigIndex
    public Map<String, Map<Path, ConfigEntry>> getConfigIndex() {
        return configIndex;
    }

    public Set<String> getAllConfigNames() {
        return configIndex.keySet();
    }

    public Map<Path, Map<String, List<Path>>> getParameterIndex() {
        return parameterIndex;
    }

    public void update(Index newIndex) {
        configIndex.putAll(newIndex.getConfigIndex());
        parameterIndex.putAll(newIndex.getParameterIndex());
    }

    public void addConfigIndexEntry(String configName, Path path, ConfigEntry entry) {
        if (configIndex.get(configName) == null) {
            initializePathIndex(configName);
        }
        configIndex.get(configName).put(path, entry);
    }
//    public void addParameterIndexEntry(String configName, Path path, HashMap<String, List<Path>> parameterIndexEntry) {
//        if (parameterIndex.get(configName) != null) {
//            parameterIndex.get(configName).put(path, parameterIndexEntry);
//        }
//    }
}
