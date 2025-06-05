package com.crowfunder.cogmaster.Index;

import com.crowfunder.cogmaster.Configs.ConfigEntry;
import com.crowfunder.cogmaster.Configs.ParameterArray;
import com.crowfunder.cogmaster.Configs.Path;
import com.crowfunder.cogmaster.Parsers.ParserService;
import com.crowfunder.cogmaster.Properties.PropertiesService;
import com.crowfunder.cogmaster.Routers.RouterService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
class IndexRepository {

    Logger logger = LoggerFactory.getLogger(IndexRepository.class);
    private final ParserService parserService;
    private final RouterService routerService;
    private final PropertiesService propertiesService;
    // The actual index
    private final Index index = new Index();

    public IndexRepository(ParserService parserService, RouterService routerService,
            PropertiesService propertiesService) {
        this.parserService = parserService;
        this.routerService = routerService;
        this.propertiesService = propertiesService;
    }

    @PostConstruct
    public void populateIndex() {
        logger.info("Parsing the configs, populating ConfigIndex...");
        index.update(parserService.populateConfigIndex());
        logger.info("Finished parsing");

        logger.info("Resolving derivations...");
        resolveConfigDependencies();
        logger.info("Finished resolving");
    }

    public ConfigEntry readConfigIndex(String configName, Path path) {
        return Optional.ofNullable(index.getPathIndex(configName))
                .map(pathIndex -> pathIndex.get(path))
                .orElse(null);
    }

    public List<Path> readParameterIndex(Path paramPath, String paramValue) {
        return index.getParameterIndex().get(paramPath).get(paramValue);
    }

    public List<Path> readNameIndex(String key) {
        return index.getNameIndex().getOrDefault(key, new ArrayList<>());
    }

    // Resolve the derivation of a config in-place
    // We want to cache the resolved derivation in the index
    private void resolveDerivation(ConfigEntry configEntry) {

        // We only resolve derivations of derived configs
        if (!configEntry.isDerived()) {
            return;
        }

        ParameterArray derivedParameters = new ParameterArray();
        ConfigEntry derivedConfig = readConfigIndex(configEntry.getSourceConfig(), configEntry.getDerivedPath());
        while (derivedConfig != null) {
            derivedParameters.update(derivedConfig.getParameters());
            if (!derivedConfig.isDerived()) {
                configEntry.setDerivedImplementationType(derivedConfig.getImplementationType());
            }
            derivedConfig = readConfigIndex(configEntry.getSourceConfig(), derivedConfig.getDerivedPath());
        }
        configEntry.updateDerivedParameters(derivedParameters);
        index.addConfigIndexEntry(configEntry.getSourceConfig(), configEntry.getPath(), configEntry);
    }

    // Resolve and cache ALL derivations from ConfigIndex
    // Populate name index
    // Populate routed parameters
    public void resolveConfigDependencies() {
        for (String configName : index.getConfigIndex().keySet()) {
            for (Path path : index.getConfigIndex().get(configName).keySet()) {
                ConfigEntry configEntry = readConfigIndex(configName, path);

                // Resolve derivations
                resolveDerivation(configEntry);

                // Populate routed parameters
                configEntry.populateRoutedParameters(routerService.getRouter(configEntry));

                // Populate name index
                String name = configEntry.getName();
                if (name != null && !name.isEmpty()) {
                    index.addNameIndexEntry(propertiesService.parsePropertyString(name).orElseGet(() -> null), path,
                            configName);
                }
            }
        }
    }

    @Cacheable("indexRepository")
    public Set<String> getAllIndexKeys() {
        return index.getConfigIndex().keySet();
    }

    @Cacheable("indexCache")
    public Set<String> getAllNameIndexKeys() {
        return index.getNameIndex().keySet();
    }

    // Returns all config index keys, joint into a single set
    @Cacheable("indexCache")
    public Set<String> getAllConfigIndexKeysJoint() {
        Set<String> result = new HashSet<>();
        for (Map<Path,ConfigEntry> subIndex : index.getConfigIndex().values()) {
            for (Path path : subIndex.keySet()) {
                result.add(path.toString());
            }
        }
        return result;
    }

    // Returns all config index keys, except as a dictionary mapping ConfigIndex keys to Sets
    @Cacheable("indexCache")
    public Map<String, Set<String>> getAllConfigIndexKeysMapped() {
        Map<String, Set<String>> result = new HashMap<>();

        for (Map.Entry<String, Map<Path, ConfigEntry>> entry : index.getConfigIndex().entrySet()) {
            String outerKey = entry.getKey();
            Set<Path> innerKeys = entry.getValue().keySet();
            Set<String> innerKeysString = new HashSet<>();
            for (Path path : innerKeys) {
                innerKeysString.add(path.toString());
            }

            result.put(outerKey, innerKeysString);
        }

        return result;
    }
}