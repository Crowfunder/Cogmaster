package com.crowfunder.cogmaster.Index;

import com.crowfunder.cogmaster.Configs.ConfigEntry;
import com.crowfunder.cogmaster.Routers.RouterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/v1/index")
public class IndexController {

    private final IndexService indexService;
    private final RouterService routerService;

    public IndexController(IndexService indexService, RouterService routerService) {
        this.indexService = indexService;
        this.routerService = routerService;
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
