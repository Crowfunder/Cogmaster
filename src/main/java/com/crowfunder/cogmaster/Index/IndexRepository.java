package com.crowfunder.cogmaster.Index;

import com.crowfunder.cogmaster.Configs.ConfigEntry;
import com.crowfunder.cogmaster.Configs.ConfigReference;
import com.crowfunder.cogmaster.Configs.Path;
import com.crowfunder.cogmaster.Parsers.ParserService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Repository
class IndexRepository {

    private final ParserService parserService;

    // The base PathIndex that maps config paths to their respective ConfigEntry objects.
    private final HashMap<Path, ConfigEntry> pathIndex = new HashMap<>();

    // Index mapping specific parameter values to ConfigEntry paths from pathIndex
    private final HashMap<Path, HashMap<String, List<Path>>> parameterIndex = new HashMap<>();

    // Get ConfigEntry object by its config path
    public ConfigEntry resolveConfig(String path) {
        return this.pathIndex.get(path);
    }

    // Get ConfigEntry object by resolving a ConfigReference object
    public ConfigEntry resolveConfig(ConfigReference configReference) {
        return this.pathIndex.get(configReference.getPath());
    }

    // Reverse search by specific parameter names and values,
    public List<Path> getConfigsByParameter(String paramName, String paramValue) {
        return this.parameterIndex.get(paramName).get(paramValue);
    }

    public IndexRepository(ParserService parserService) {
        this.parserService = parserService;
    }

    @PostConstruct
    public void populateIndex() {
        // run parsers
    }

}