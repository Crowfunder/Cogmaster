package com.crowfunder.cogmaster.Assets;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class AssetsService {

    AssetsRepository assetsRepository;

    public AssetsService(AssetsRepository assetsRepository) {
        this.assetsRepository = assetsRepository;
    }

    public Resource getAsset(String relativePath) {
        AssetsRepository.AssetInfo info = assetsRepository.getAssetInfo(relativePath);
        return (info != null) ? info.resource() : null;
    }

    public String getContentType(String relativePath) {
        AssetsRepository.AssetInfo info = assetsRepository.getAssetInfo(relativePath);
        return (info != null) ? info.contentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }

    public Set<String> getAvailableAssets() {
        return assetsRepository.getAllAssetPaths();
    }

    @Cacheable("getAssetsStats")
    public Map<String, Integer> getAssetsStats() {
        Map<String, Integer> assetsStats = new HashMap<>();
        assetsStats.put("Loaded Assets", assetsRepository.getNumberAssets());
        return assetsStats;
    }
}
