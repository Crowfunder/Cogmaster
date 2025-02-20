package com.crowfunder.cogmaster.Index;

import com.crowfunder.cogmaster.Configs.ConfigEntry;
import com.crowfunder.cogmaster.Configs.ConfigReference;
import com.crowfunder.cogmaster.Configs.ParameterArray;
import com.crowfunder.cogmaster.Configs.Path;
import com.crowfunder.cogmaster.Parsers.ParserService;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
class IndexRepository {

    private final ParserService parserService;
    Logger logger = LoggerFactory.getLogger(IndexRepository.class);

    // The actual index
    private final Index index = new Index();

    public ConfigEntry readConfigIndex(String configName, Path path) {
        return index.getPathIndex(configName).get(path);
    }

    public List<Path> readParameterIndex(Path paramPath, String paramValue) {
        return index.getParameterIndex().get(paramPath).get(paramValue);
    }

    public List<Path> readNameIndex(String key) {
        return index.getNameIndex().get(key);
    }

    // Resolve the derivation of a config in-place
    // We want to cache the resolved derivation in the index
    private void resolveDerivation(String configName, Path path) {
        ConfigEntry configEntry = readConfigIndex(configName, path);

        // We only resolve derivations of derived configs
        if (!configEntry.isDerived()) {
            return;
        }

        ParameterArray derivedParameters = new ParameterArray();
        ConfigEntry derivedConfig = readConfigIndex(configName, configEntry.getDerivedPath());
        while (derivedConfig != null) {
            derivedParameters.update(derivedConfig.getParameters());
            if (!derivedConfig.isDerived()) {
                configEntry.setDerivedImplementationType(derivedConfig.getImplementationType());
            }
            derivedConfig = readConfigIndex(configName, derivedConfig.getDerivedPath());
        }
        configEntry.updateDerivedParameters(derivedParameters);
        index.addConfigIndexEntry(configEntry.getSourceConfig(), path, configEntry);
    }

    // Resolve and cache ALL derivations from ConfigIndex
    // Populate name index
    public void resolveAllDerivations() {
        for (String configName : index.getConfigIndex().keySet() ) {
            for (Path path : index.getConfigIndex().get(configName).keySet()) {
                resolveDerivation(configName, path);
                String name = readConfigIndex(configName, path).getName();
                if (name != null && !name.isEmpty()) {
                    index.addNameIndexEntry(name, path, configName);
                }
            }
        }
    }

    public IndexRepository(ParserService parserService) {
        this.parserService = parserService;
    }

    @PostConstruct
    public void populateIndex() {
        logger.info("Parsing the configs, populating ConfigIndex...");
        index.update(parserService.populateConfigIndex());
        logger.info("Finished parsing");

        logger.info("Resolving derivations...");
        resolveAllDerivations();
        logger.info("Finished resolving");
    }

}