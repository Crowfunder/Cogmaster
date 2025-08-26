package com.crowfunder.cogmaster.Parseable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.crowfunder.cogmaster.Configs.ParameterValue;
import com.crowfunder.cogmaster.Configs.Path;
import com.crowfunder.cogmaster.Translations.TranslationsService;

import jakarta.annotation.PostConstruct;

@Service
public class NameIndexService {

    private final ParseableGraphRepository graphRepo;
    private final TranslationsService translationsService;

    // Name Index mapping properties keys found in <name> node to specific config
    // paths
    // names are stored in lowercase and looked up as lowercase
    private final Map<String, List<Path>> nameIndex = new HashMap<>();

    // Name index keys preserving their original case
    // for use in front-end autocomplete
    private final Set<String> nameIndexKeysPretty = new HashSet<>();

    public NameIndexService(ParseableGraphRepository graphRepository, TranslationsService translationsService) {
        this.graphRepo = graphRepository;
        this.translationsService = translationsService;
    }

    public Map<String, List<Path>> getNameIndex() {
        return nameIndex;
    }

    
    public List<Path> readNameIndex(String key) {
        return nameIndex.getOrDefault(key.toLowerCase(), new ArrayList<>());
    }

    public Set<String> getNameIndexKeysPretty() {
        return nameIndexKeysPretty;
    }

    @PostConstruct
    private void resolveNamesAndPrettyNames() {
        var parsedResources = graphRepo.getAll();
        for (String configFileName : parsedResources.keySet()) {
            for (Path entryPath : parsedResources.get(configFileName).keySet()) {
                var configEntry = parsedResources.get(configFileName).get(entryPath);

                // Populate name index
                ParameterValue name = configEntry.routedParameters.resolveParameterPath("name");
                var nameString = name == null ? null : name.toString();

                if (nameString != null && !nameString.isEmpty()) {
                    var prependedPath = entryPath.prependedPath(configFileName);
                    var translatedName = translationsService.parseTranslationString(nameString).orElseGet(() -> null);
                    addNameIndexEntry(translatedName, prependedPath);
                }
            }
        }
    }

    private void addNameIndexEntry(String name, Path path) {
        if (name == null) {
            return;
        }
        nameIndexKeysPretty.add(name);
        name = name.toLowerCase();
        if (nameIndex.get(name) == null) {
            initializeNameIndex(name);
        }

        nameIndex.get(name).add(path);
    }

    private void initializeNameIndex(String name) {
        this.nameIndex.put(name, new ArrayList<>());
    }

}
