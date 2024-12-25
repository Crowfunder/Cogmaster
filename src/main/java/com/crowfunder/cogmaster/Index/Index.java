package com.crowfunder.cogmaster.Index;

import com.crowfunder.cogmaster.Configs.ConfigEntry;
import com.crowfunder.cogmaster.Configs.Path;

import java.util.HashMap;
import java.util.List;

public class Index {
    // The base PathIndex that maps config paths to their respective ConfigEntry objects.
    private final HashMap<Path, ConfigEntry> pathIndex = new HashMap<>();

    // Index mapping specific parameter values to ConfigEntry paths from pathIndex
    private final HashMap<Path, HashMap<String, List<Path>>> parameterIndex = new HashMap<>();

    public HashMap<Path, ConfigEntry> getPathIndex() {
        return pathIndex;
    }

    public HashMap<Path, HashMap<String, List<Path>>> getParameterIndex() {
        return parameterIndex;
    }

    public void update(Index newIndex) {
        pathIndex.putAll(newIndex.getPathIndex());
        parameterIndex.putAll(newIndex.getParameterIndex());
    }

}
