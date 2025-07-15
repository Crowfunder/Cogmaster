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
    // names are stored in lowercase and looked up as lowercase
    private final Map<String, List<Path>> nameIndex = new HashMap<>();

    // Name index keys preserving their original case
    // for use in front-end autocomplete
    private final Set<String> nameIndexKeysPretty = new HashSet<>();

    private void initializePathIndex(String configName) {
        this.configIndex.put(configName, new HashMap<>());
    }

    private void initializeNameIndex(String name) {
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

    public Set<String> getNameIndexKeysPretty() { return this.nameIndexKeysPretty; }

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
        if (name == null) {
            return;
        }
        nameIndexKeysPretty.add(name);
        name = name.toLowerCase();
        if (nameIndex.get(name) == null) {
            initializeNameIndex(name);
        }

        nameIndex.get(name).add(path);
    }
}
