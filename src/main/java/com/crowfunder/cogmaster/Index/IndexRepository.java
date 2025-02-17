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

    // Get ConfigEntry object by its config path
    public ConfigEntry resolveConfig(String configName, Path path) {
        return index.getPathIndex(configName).get(path);
    }

    // Get ConfigEntry object by its config path
    public ConfigEntry resolveConfig(String configName, String path) {
        return resolveConfig(configName, new Path(path));
    }

    // Get ConfigEntry by path that leads both to the correct index and entry within it
    public ConfigEntry resolveConfig(Path path) {
        return index.getPathIndex(path.getNextPath()).get(path.rotatePath());
    }

    // Get ConfigEntry by path that leads both to the correct index and entry within it
    public ConfigEntry resolveConfig(String path) {
        return resolveConfig(new Path(path));
    }

    // Get ConfigEntry object by resolving a ConfigReference object
    public ConfigEntry resolveConfig(ConfigReference configReference) {
        return index.getPathIndex(configReference.getSourceConfig()).get(configReference.getPath());
    }

    // Reverse search by specific parameter names and values,
    public List<Path> getConfigsByParameter(Path paramPath, String paramValue) {
        return index.getParameterIndex().get(paramPath).get(paramValue);
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
            if (!derivedConfig.isDerived()) {
                configEntry.setDerivedImplementationType(derivedConfig.getImplementationType());
            }
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

    public IndexRepository(ParserService parserService) {
        this.parserService = parserService;
    }

    @PostConstruct
    public void populateIndex() {
        logger.info("Parsing the configs, populating ConfigIndex...");
        index.update(parserService.populateConfigIndex());
        logger.info("Finished parsing");

        logger.info("Resolving derivations...");
        resolveConfigIndexDerivations();
        logger.info("Finished resolving");
        // TODO: NameIndex powinien mapować nazwy na listy ścieżek w które wchodzi jaki to config
        // np. Path("item/weapon/sword/brandish")

//        System.out.println("Populating ParameterIndex...");
//        index.update(parserService.populateParameterIndex(index));
//        System.out.println("Finished populating");
    }

}