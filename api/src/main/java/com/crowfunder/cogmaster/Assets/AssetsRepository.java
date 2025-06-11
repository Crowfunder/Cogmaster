package com.crowfunder.cogmaster.Assets;

import com.crowfunder.cogmaster.CogmasterConfig;
import com.crowfunder.cogmaster.Translations.TranslationsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class AssetsRepository {

    Logger logger = LoggerFactory.getLogger(AssetsRepository.class);
    private final String assetsPath;
    private final Map<String, AssetInfo> assetsIndex = new HashMap<>();

    public AssetsRepository(CogmasterConfig cogmasterConfig) throws IOException {
        logger.info("Populating assets index...");
        this.assetsPath = cogmasterConfig.assets().path();
        loadAssets();
        logger.info("Finished populating");
    }

    private void loadAssets() throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            Resource[] resources = resolver.getResources("classpath*:/" + assetsPath + "/**/*.*");

            for (Resource resource : resources) {
                String fullPath = resource.getURL().getPath();
                int index = fullPath.lastIndexOf(assetsPath);
                if (index != -1) {
                    String relativePath = fullPath.substring(index + assetsPath.length()+1);

                    String contentType = detectContentType(resource);
                    assetsIndex.put(relativePath, new AssetInfo(resource, contentType));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load classpath assets from " + assetsPath, e);
        }
    }


    public AssetInfo getAssetInfo(String relativePath) {
        return assetsIndex.get(relativePath);
    }

    public Set<String> getAllAssetPaths() {
        return assetsIndex.keySet();
    }

    private String detectContentType(Resource resource) {
        try (var is = resource.getInputStream()) {
            String type = URLConnection.guessContentTypeFromStream(is);
            return (type != null) ? type : MediaType.APPLICATION_OCTET_STREAM_VALUE;
        } catch (IOException e) {
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
    }

    public record AssetInfo(Resource resource, String contentType) {}
}

