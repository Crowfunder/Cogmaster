package com.crowfunder.cogmaster.Index;

import com.crowfunder.cogmaster.Configs.ConfigEntry;
import com.crowfunder.cogmaster.Configs.Path;

import java.util.*;

public class Index {
    // The base ConfigIndex that maps config names to hashmaps containing mappings of paths to ConfigEntry objects
    private final Map<String, Map<Path, ConfigEntry>> configIndex = new HashMap<>();

    // Index mapping specific parameter values to ConfigEntry paths from configIndex
    private final Map<Path, Map<String, List<Path>>> parameterIndex = new HashMap<>();

    // Name Index mapping properties keys found in <name> node to specific config paths
    private final Map<String, List<Path>> nameIndex = new HashMap<>();

    private void initializePathIndex(String configName) {
        this.configIndex.put(configName, new HashMap<>());
    }

    private void initalizeNameIndex(String name) {
        this.nameIndex.put(name, new ArrayList<>());
    }

    // Return the entire ConfigIndex
    public Map<String, Map<Path, ConfigEntry>> getConfigIndex() {
        return configIndex;
    }

    public Map<Path, Map<String, List<Path>>> getParameterIndex() {
        return parameterIndex;
    }

    public Map<String, List<Path>> getNameIndex() {
        return nameIndex;
    }

    // Return index for one config
    public Map<Path, ConfigEntry> getPathIndex(String configName) {
        return configIndex.get(configName);
    }

    public void update(Index newIndex) {
        configIndex.putAll(newIndex.getConfigIndex());
        parameterIndex.putAll(newIndex.getParameterIndex());
        nameIndex.putAll(newIndex.getNameIndex());
    }

    public void addConfigIndexEntry(String configName, Path path, ConfigEntry entry) {
        if (configIndex.get(configName) == null) {
            initializePathIndex(configName);
        }
        configIndex.get(configName).put(path, entry);
    }

    public void addNameIndexEntry(String name, Path path, String configName) {
        addNameIndexEntry(name, path.prependedPath(configName));
    }

    public void addNameIndexEntry(String name, Path path) {
        if (nameIndex.get(name) == null) {
            initalizeNameIndex(name);
        }
        nameIndex.get(name).add(path);
    }
}
