package com.crowfunder.cogmaster.Translations;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.crowfunder.cogmaster.Utils.StringResult;

import java.util.stream.Stream;

@RestController
@RequestMapping("api/v1/translations")
public class TranslationsController {

    private final TranslationsService translationsService;

    public TranslationsController(TranslationsService translationsService) {
        this.translationsService = translationsService;
    }

    @GetMapping(path = "key")
    public ResponseEntity<StringResult> getValue(@RequestParam("q") String q) {
        var value = translationsService.parseTranslationString(q).map(x -> new StringResult(x));
        return value.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping(path = "value")
    public ResponseEntity<Stream<StringResult>> getKey(@RequestParam("q") String q) {
        var keys = translationsService.searchByValue(q);
        return keys.map(list -> ResponseEntity.ok(list.stream().map(str -> new StringResult(str))))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
