package com.crowfunder.cogmaster.Index;

import com.crowfunder.cogmaster.Configs.ConfigEntry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("api/v1/index")
public class IndexController {

    private final IndexService indexService;

    public IndexController(IndexService indexService) {
        this.indexService = indexService;
    }

    @GetMapping("config/{configName}")
    public ResponseEntity<ConfigEntry> resolveConfigByPath(@PathVariable("configName") String configName, @RequestParam String path) {
        Optional<ConfigEntry> resolvedConfig = Optional.ofNullable(indexService.resolveConfig(configName, path));
        return resolvedConfig.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("search")
    public ResponseEntity<List<ConfigEntry>> resolveConfigByName(@RequestParam String q) {
        Optional<List<ConfigEntry>> resolvedConfigs = Optional.ofNullable(indexService.resolveConfigByName(q));
        return resolvedConfigs.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("info/config/names")
    public ResponseEntity<Set<String>> getAllConfigNames() {
        Optional<Set<String>> resolvedConfigs = Optional.ofNullable(indexService.getAllConfigNames());
        return resolvedConfigs.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("info/config/paths")
    public ResponseEntity<Set<String>> getAllConfigPaths() {
        Optional<Set<String>> resolvedConfigs = Optional.ofNullable(indexService.getAllConfigPaths());
        return resolvedConfigs.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("info/config/map")
    public ResponseEntity<Map<String, Set<String>>> getConfigPathsMap() {
        Optional<Map<String, Set<String>>> resolvedConfigs = Optional.ofNullable(indexService.getConfigPathsMap());
        return resolvedConfigs.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("info/search/names")
    public ResponseEntity<Set<String>> getAllEntryNames(@RequestParam(name= "tradeable", required = false, defaultValue = "false") boolean tradeable) {
        Optional<Set<String>> resolvedConfigs;
        if (tradeable) {
            resolvedConfigs = Optional.ofNullable(indexService.getTradeableEntryNames());
        } else {
            resolvedConfigs = Optional.ofNullable(indexService.getAllEntryNames());
        }
        return resolvedConfigs.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("info/stats")
    public ResponseEntity<Map<String, Integer>> getStats() {
        Optional<Map<String, Integer>> resolvedConfigs = Optional.ofNullable(indexService.getIndexStats());
        return resolvedConfigs.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

}
