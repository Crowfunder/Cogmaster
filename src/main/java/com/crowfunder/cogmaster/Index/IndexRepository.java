package com.crowfunder.cogmaster.Index;

import com.crowfunder.cogmaster.Configs.ConfigEntry;
import com.crowfunder.cogmaster.Configs.ConfigReference;
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
        index.update(parserService.parse());
        System.out.println("Finished Parsing");
    }

}