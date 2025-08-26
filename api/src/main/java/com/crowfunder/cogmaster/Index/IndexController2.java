package com.crowfunder.cogmaster.Index;

import com.crowfunder.cogmaster.Parseable.NameIndexService;
import com.crowfunder.cogmaster.Parseable.NewConfigEntry;
import com.crowfunder.cogmaster.Parseable.ParseableGraphRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("api/v2/index")
public class IndexController2 {

    private final ParseableGraphRepository graphRepo;
    private final IndexService2 indexService;
    private final NameIndexService nameIndexService;

    public IndexController2(ParseableGraphRepository graphRepo, IndexService2 indexService,
            NameIndexService nameIndexService) {
        this.graphRepo = graphRepo;
        this.indexService = indexService;
        this.nameIndexService = nameIndexService;
    }

    @GetMapping("config/{configName}")
    public ResponseEntity<NewConfigEntry> resolveConfigByPath(@PathVariable("configName") String configName,
            @RequestParam String path) {
        return graphRepo.resolveConfig(configName, path).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("search")
    public ResponseEntity<List<NewConfigEntry>> resolveConfigByName(@RequestParam String q) {
        return ResponseEntity.ok(indexService.resolveConfigByName(q));
    }

    @GetMapping("info/config/names")
    public ResponseEntity<Set<String>> getAllConfigNames() {
        return ResponseEntity.ok(indexService.getAllConfigFileNames());
    }

    @GetMapping("info/config/paths")
    public ResponseEntity<Set<String>> getAllConfigPaths() {
        return ResponseEntity.ok(indexService.getAllConfigEntryKeys());
    }

    @GetMapping("info/config/map")
    public ResponseEntity<Map<String, Set<String>>> getConfigPathsMap() {
        Optional<Map<String, Set<String>>> resolvedConfigs = Optional
                .ofNullable(indexService.getAllConfigIndexKeysMapped());
        return resolvedConfigs.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("info/search/names")
    public ResponseEntity<Set<String>> getAllEntryNames(
            @RequestParam(name = "tradeable", required = false, defaultValue = "false") boolean tradeable) {
        Optional<Set<String>> resolvedConfigs;
        if (tradeable) {
            resolvedConfigs = Optional.ofNullable(indexService.getTradeableEntryNames());
        } else {
            resolvedConfigs = Optional.ofNullable(nameIndexService.getNameIndexKeysPretty());
        }
        return resolvedConfigs.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("info/stats")
    public ResponseEntity<Map<String, Integer>> getStats() {
        return ResponseEntity.ok(indexService.getIndexStats());
    }

}
