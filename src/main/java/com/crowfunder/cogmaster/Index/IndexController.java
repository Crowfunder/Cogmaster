package com.crowfunder.cogmaster.Index;

import com.crowfunder.cogmaster.Configs.ConfigEntry;
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
    public Optional<ConfigEntry> resolveConfigByPath(@PathVariable("configName") String configName, @RequestParam String path) {
        return Optional.ofNullable(indexRepository.resolveConfig(configName, path));
    }

}
