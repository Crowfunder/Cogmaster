package com.crowfunder.cogmaster.Assets;

import io.micrometer.observation.ObservationFilter;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

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
}
