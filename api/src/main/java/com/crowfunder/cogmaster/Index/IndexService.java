package com.crowfunder.cogmaster.Index;

import com.crowfunder.cogmaster.Configs.ConfigEntry;
import com.crowfunder.cogmaster.Configs.ConfigReference;
import com.crowfunder.cogmaster.Configs.Path;
import com.crowfunder.cogmaster.Translations.TranslationsService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.crowfunder.cogmaster.Utils.StringUtil.uppercaseFirstLetters;

@Service
public class IndexService {

    IndexRepository indexRepository;
    TranslationsService translationsService;

    public IndexService(IndexRepository indexRepository, TranslationsService translationsService) {
        this.indexRepository = indexRepository;
        this.translationsService = translationsService;
    }

    // Get ConfigEntry object by its config path
    public ConfigEntry resolveConfig(String configName, Path path) {
        return indexRepository.readConfigIndex(configName, path);
    }

    // Get ConfigEntry object by its config path
    public ConfigEntry resolveConfig(String configName, String path) {
        return resolveConfig(configName, new Path(path));
    }

    // Get ConfigEntry by path that leads both to the correct index and entry within it
    public ConfigEntry resolveConfig(Path path) {
        return indexRepository.readConfigIndex(path.getNextPath(), path.rotatePath());
    }

    // Get ConfigEntry by path that leads both to the correct index and entry within it
    public ConfigEntry resolveConfig(String path) {
        return resolveConfig(new Path(path));
    }

    // Get ConfigEntry object by resolving a ConfigReference object
    public ConfigEntry resolveConfig(ConfigReference configReference) {
        return indexRepository.readConfigIndex(configReference.getSourceConfig(), configReference.getPath());
    }

    // Get multiple ConfigEntry objects by paths
    // Works only for full paths (indicating the exact PathIndex entry)
    public List<ConfigEntry> resolveConfigsFullPath(List<Path> paths) {
        List<ConfigEntry> configs = new ArrayList<>();
        for (Path path : paths) {
            configs.add(resolveConfig(path));
        }
        if (configs.isEmpty()) {
            return null;
        }
        return configs;
    }

    // Resolve one or more ConfigEntry objects by
    // querying the propertiesService for name mappings
    // that can be used in nameIndex
    // Ignores case (always searches by first character of word uppercase)
    // Commented out case ignorance, because it is faulty and we use Autocomplete regardless
    public List<ConfigEntry> resolveConfigByName(String name) {
//        name = uppercaseFirstLetters(name.toLowerCase());
        return resolveConfigsFullPath(indexRepository.readNameIndex(name));
    }

    // Get a list of all available configs
    public Set<String> getAllConfigNames() {
        return indexRepository.getAllIndexKeys();
    }

    public Set<String> getAllEntryNames() {
        return indexRepository.getAllNameIndexKeys();
    }

    public Set<String> getAllConfigPaths() {
        return indexRepository.getAllConfigIndexKeysJoint();
    }

    public Map<String, Set<String>> getConfigPathsMap() {
        return indexRepository.getAllConfigIndexKeysMapped();
    }

    @Cacheable("getIndexStats")
    public Map<String, Integer> getIndexStats() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("Parsed Configs", indexRepository.getNumberIndexKeys());
        stats.put("Config Entries", indexRepository.getNumberConfigIndexKeys());
        stats.put("Named Config Entries", indexRepository.getNumberNameConfigKeys());
        return stats;
    }
}
