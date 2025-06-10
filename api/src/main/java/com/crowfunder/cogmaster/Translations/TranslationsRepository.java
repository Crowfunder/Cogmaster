package com.crowfunder.cogmaster.Translations;

import com.crowfunder.cogmaster.CogmasterConfig;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static com.crowfunder.cogmaster.Utils.HashMapUtil.invertHashMap;
import static com.crowfunder.cogmaster.Utils.HashMapUtil.propertiesToHashMap;

@Repository
public class TranslationsRepository {

    Logger logger = LoggerFactory.getLogger(TranslationsRepository.class);
    private final String propertiesPath;
    private Map<String, String> translations;
    private Map<String, List<String>> reverseTranslations;

    public TranslationsRepository(CogmasterConfig cogmasterConfig) {
        this.propertiesPath = cogmasterConfig.translations().path();
    }

    // We interface with the properties through Map because we want to utilize
    // inverting util
    @PostConstruct
    private void PopulateTranslations() {
        logger.info("Populating translations repository...");
        translations = propertiesToHashMap(loadAllProperties());
        reverseTranslations = invertHashMap(translations);
        logger.info("Finished populating");
    }

    private Properties loadAllProperties() {
        Properties properties = new Properties();
        try {
            PathMatchingResourcePatternResolver r = new PathMatchingResourcePatternResolver();
            Resource[] resources = r.getResources("classpath*:/" + propertiesPath + "/*.properties");
            for (Resource resource : resources) {
                properties.putAll(loadProperties(resource.getInputStream()));
            }
            return properties;

        } catch (IOException e) {
            logger.error("Failed to load translations from specified path: /{}/*", propertiesPath);
            throw new RuntimeException("Failed to load translations", e);
        }
    }

    private Properties loadProperties(InputStream inputStream) throws IOException {
        Properties newProperties = new Properties();
        newProperties.load(inputStream);
        return newProperties;
    }

    public Optional<String> searchTranslation(String property) {
        var result = translations.get(property);
        return Optional.ofNullable(result);
    }

    public Optional<List<String>> reverseSearchTranslation(String propertyValue) {
        var results = reverseTranslations.get(propertyValue);
        return Optional.ofNullable(results);
    }

}
