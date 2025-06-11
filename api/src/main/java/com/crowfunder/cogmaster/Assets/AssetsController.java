package com.crowfunder.cogmaster.Assets;

import com.crowfunder.cogmaster.Utils.StringResult;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Paths;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("api/v1/assets")
public class AssetsController {

    private final AssetsService assetsService;

    public AssetsController(AssetsService assetsService) {
        this.assetsService = assetsService;
    }

    @GetMapping(path = "index")
    public ResponseEntity<Resource> getAsset(@RequestParam String path) {
        return Optional.ofNullable(assetsService.getAsset(path))
                .map(resource -> {
                    String contentType = assetsService.getContentType(path);
                    return ResponseEntity.ok()
                            .contentType(MediaType.parseMediaType(contentType))
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + Paths.get(path).getFileName() + "\"")
                            .body(resource);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping(path = "info/available")
    public ResponseEntity<Set<String>> getAvailableAssets() {
        Optional<Set<String>> availableConfigs = Optional.ofNullable(assetsService.getAvailableAssets());
        return availableConfigs.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
