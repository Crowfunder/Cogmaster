package com.crowfunder.cogmaster.Index;

import com.crowfunder.cogmaster.Configs.ConfigEntry;
import com.crowfunder.cogmaster.Configs.ConfigReference;
import com.crowfunder.cogmaster.Configs.Path;
import com.crowfunder.cogmaster.Properties.PropertiesService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class IndexService {

    IndexRepository indexRepository;
    PropertiesService propertiesService;

    // Get ConfigEntry object by its config path
    public ConfigEntry resolveConfig(String configName, Path path) {
        return indexRepository.readPathIndex(configName, path);
    }

    // Get ConfigEntry object by its config path
    public ConfigEntry resolveConfig(String configName, String path) {
        return resolveConfig(configName, new Path(path));
    }

    // Get ConfigEntry by path that leads both to the correct index and entry within it
    public ConfigEntry resolveConfig(Path path) {
        return indexRepository.readPathIndex(path.getNextPath(), path.rotatePath());
    }

    // Get ConfigEntry by path that leads both to the correct index and entry within it
    public ConfigEntry resolveConfig(String path) {
        return resolveConfig(new Path(path));
    }

    // Get ConfigEntry object by resolving a ConfigReference object
    public ConfigEntry resolveConfig(ConfigReference configReference) {
        return indexRepository.readPathIndex(configReference.getSourceConfig(), configReference.getPath());
    }

    // Get multiple ConfigEntry objects by paths
    // Works only for full paths (indicating the exact PathIndex entry)
    public List<ConfigEntry> resolveConfigsFullPath(List<Path> paths) {
        List<ConfigEntry> configs = new ArrayList<>();
        for (Path path : paths) {
            configs.add(resolveConfig(path));
        }
        return configs;
    }

    // Resolve one or more ConfigEntry objects by
    // querying the propertiesService for name mappings
    // that can be used in nameIndex
    public List<ConfigEntry> resolveConfigByName(String name) {
        List<Path> paths = new ArrayList<>();
        if (propertiesService.resolveValue(name) == null) {
            return null;
        }
        for (String key : propertiesService.resolveValue(name)) {
            Optional.ofNullable(indexRepository.readNameIndex(key)).ifPresent(paths::addAll);
        }
        if (paths.isEmpty()) { return null; }
        return resolveConfigsFullPath(paths);
    }

    public IndexService(IndexRepository indexRepository, PropertiesService propertiesService) {
        this.indexRepository = indexRepository;
        this.propertiesService = propertiesService;
    }

}
