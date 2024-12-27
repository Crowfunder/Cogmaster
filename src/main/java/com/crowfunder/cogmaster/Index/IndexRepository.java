package com.crowfunder.cogmaster.Index;

import com.crowfunder.cogmaster.Configs.ConfigEntry;
import com.crowfunder.cogmaster.Configs.ConfigReference;
import com.crowfunder.cogmaster.Configs.ParameterArray;
import com.crowfunder.cogmaster.Configs.Path;
import com.crowfunder.cogmaster.Parsers.ParserService;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
class IndexRepository {

    private final ParserService parserService;

    // The actual index
    private final Index index = new Index();

    // Get ConfigEntry object by its config path
    public ConfigEntry resolveConfig(Path path) {
        return index.getPathIndex().get(path);
    }

    // Get ConfigEntry object by resolving a ConfigReference object
    public ConfigEntry resolveConfig(ConfigReference configReference) {
        return index.getPathIndex().get(configReference.getPath());
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
        System.out.println("Parsing the configs, populating PathIndex...");
        index.update(parserService.populatePathIndex());
        System.out.println("Finished parsing");

        System.out.println("Resolving derivations...");
        resolvePathIndexDerivations();
        System.out.println("Finished resolving");

//        System.out.println("Populating ParameterIndex...");
//        index.update(parserService.populateParameterIndex(index));
//        System.out.println("Finished populating");
    }

    // Resolve the derivation of a config in-place
    // We want to cache the resolved derivation in the index
    private void resolveDerivation(Path path) {
        ConfigEntry configEntry = resolveConfig(path);

        // We only resolve derivations of derived configs
        if (!configEntry.isDerived()) {
            return;
        }

        ParameterArray derivedParameters = new ParameterArray();
        ConfigEntry derivedConfig = resolveConfig(configEntry.getDerivedPath());
        while (derivedConfig != null) {
            derivedParameters.update(derivedConfig.getParameters());
            derivedConfig = resolveConfig(derivedConfig.getDerivedPath());
        }
        configEntry.updateDerivedParameters(derivedParameters);

        index.addPathIndexEntry(path, configEntry);
    }

    // Resolve and cache ALL derivations from PathIndex
    public void resolvePathIndexDerivations() {
        for (Path path : index.getPathIndex().keySet()) {
            resolveDerivation(path);
        }
    }

}