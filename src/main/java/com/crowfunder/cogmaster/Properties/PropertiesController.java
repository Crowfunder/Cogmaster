package com.crowfunder.cogmaster.Properties;


import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/v1/properties")
public class PropertiesController {

    private final PropertiesService propertiesService;

    public PropertiesController(PropertiesService propertiesService) {
        this.propertiesService = propertiesService;
    }

    @GetMapping(path = "key", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getValue(@RequestParam("q") String q) {
        Optional<String> value = Optional.ofNullable(propertiesService.resolveKey(q));
        return value.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("value")
    public ResponseEntity<List<String>> getKey(@RequestParam("q") String q) {
        Optional<List<String>> key = Optional.ofNullable(propertiesService.resolveValue(q));
        return key.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

}
