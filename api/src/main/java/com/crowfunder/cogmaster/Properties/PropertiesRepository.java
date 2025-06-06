package com.crowfunder.cogmaster.Properties;

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
public class PropertiesRepository {

    Logger logger = LoggerFactory.getLogger(PropertiesRepository.class);
    // I'm begging you, I'm begging you please just some proper settings file
    private final String propertiesPath;
    private Map<String, String> properties;
    private Map<String, List<String>> reverseProperties;

    public PropertiesRepository(CogmasterConfig cogmasterConfig) {
        this.propertiesPath = cogmasterConfig.properties().path();
    }

    // We interface with the properties through Map because we want to utilize
    // inverting util
    @PostConstruct
    private void PopulateProperties() {
        logger.info("Populating properties repository...");
        properties = propertiesToHashMap(loadAllProperties());
        reverseProperties = invertHashMap(properties);
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
            logger.error("Failed to load properties from specified path: /{}/*", propertiesPath);
            throw new RuntimeException("Failed to load properties", e);
        }
    }

    private Properties loadProperties(InputStream inputStream) throws IOException {
        Properties newProperties = new Properties();
        newProperties.load(inputStream);
        return newProperties;
    }

    public Optional<String> searchProperty(String property) {
        var result = properties.get(property);
        return Optional.ofNullable(result);
    }

    public Optional<List<String>> reverseSearchProperty(String propertyValue) {
        var results = reverseProperties.get(propertyValue);
        return Optional.ofNullable(results);
    }

}
