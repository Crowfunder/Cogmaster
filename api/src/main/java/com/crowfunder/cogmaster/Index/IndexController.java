package com.crowfunder.cogmaster.Index;

import com.crowfunder.cogmaster.Configs.ConfigEntry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

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
}
