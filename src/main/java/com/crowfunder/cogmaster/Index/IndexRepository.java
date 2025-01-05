package com.crowfunder.cogmaster.Index;

import com.crowfunder.cogmaster.Configs.ConfigEntry;
import com.crowfunder.cogmaster.Configs.ConfigReference;
import com.crowfunder.cogmaster.Configs.ParameterArray;
import com.crowfunder.cogmaster.Configs.Path;
import com.crowfunder.cogmaster.Parsers.ParserService;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;

@Repository
class IndexRepository {

    private final ParserService parserService;

    // The actual index
    private final Index index = new Index();

    // Get ConfigEntry object by its config path
    public ConfigEntry resolveConfig(String configName, Path path) {
        return index.getPathIndex(configName).get(path);
    }

    // Get ConfigEntry object by resolving a ConfigReference object
    public ConfigEntry resolveConfig(String configName, ConfigReference configReference) {
        return index.getPathIndex(configName).get(configReference.getPath());
    }

    // Reverse search by specific parameter names and values,
    public List<Path> getConfigsByParameter(Path paramPath, String paramValue) {
        return index.getParameterIndex().get(paramPath).get(paramValue);
    }

    public IndexRepository(ParserService parserService) {
        this.parserService = parserService;
    }

    @PostConstruct
    public void populateIndex() {
        System.out.println("Parsing the configs, populating ConfigIndex...");
        index.update(parserService.populateConfigIndex());
        System.out.println("Finished parsing");

        System.out.println("Resolving derivations...");
        resolveConfigIndexDerivations();
        System.out.println("Finished resolving");

//        System.out.println("Populating ParameterIndex...");
//        index.update(parserService.populateParameterIndex(index));
//        System.out.println("Finished populating");
    }

    // Resolve the derivation of a config in-place
    // We want to cache the resolved derivation in the index
    private void resolveDerivation(String configName, Path path) {
        ConfigEntry configEntry = resolveConfig(configName, path);

        // We only resolve derivations of derived configs
        if (!configEntry.isDerived()) {
            return;
        }

        ParameterArray derivedParameters = new ParameterArray();
        ConfigEntry derivedConfig = resolveConfig(configName, configEntry.getDerivedPath());
        while (derivedConfig != null) {
            derivedParameters.update(derivedConfig.getParameters());
            derivedConfig = resolveConfig(configName, derivedConfig.getDerivedPath());
        }
        configEntry.updateDerivedParameters(derivedParameters);

        index.addConfigIndexEntry(configEntry.getSourceConfig(), path, configEntry);
    }

    // Resolve and cache ALL derivations from ConfigIndex
    public void resolveConfigIndexDerivations() {
        for (String configName : index.getConfigIndex().keySet() ) {
            for (Path path : index.getConfigIndex().get(configName).keySet()) {
                resolveDerivation(configName, path);
            }
        }
    }

}