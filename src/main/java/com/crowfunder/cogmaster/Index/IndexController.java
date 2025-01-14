package com.crowfunder.cogmaster.Index;

import com.crowfunder.cogmaster.Configs.ConfigEntry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("api/v1/config")
public class IndexController {

    private final IndexRepository indexRepository;

    public IndexController(IndexRepository indexRepository) {
        this.indexRepository = indexRepository;
    }

    @GetMapping("{configName}")
    public ResponseEntity<ConfigEntry> resolveConfigByPath(@PathVariable("configName") String configName, @RequestParam String path) {
        Optional<ConfigEntry> resolvedConfig = Optional.ofNullable(indexRepository.resolveConfig(configName, path));
        if (resolvedConfig.isPresent()) {
            System.out.println("ok");
            return ResponseEntity.ok(resolvedConfig.get());
        } else {
            System.out.println("not ok");
            return ResponseEntity.notFound().build();
        }
    }

}
