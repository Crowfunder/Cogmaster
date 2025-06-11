package com.crowfunder.cogmaster.Assets;

import com.crowfunder.cogmaster.Utils.StringResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/assets")
public class AssetController {

    private final AssetsService assetsService;

    public AssetController(AssetsService assetsService) {
        this.assetsService = assetsService;
    }

    @GetMapping(path = "{assetPath}")
    public ResponseEntity<StringResult> getAsset(@PathVariable("assetPath") String assetPath) {
        var value = assetsService.getAsset(assetPath).map(x -> new StringResult(x));
        return value.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
