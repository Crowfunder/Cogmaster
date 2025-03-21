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
import java.util.Properties;

import static com.crowfunder.cogmaster.Utils.HashMapUtil.invertHashMap;
import static com.crowfunder.cogmaster.Utils.HashMapUtil.propertiesToHashMap;

@Repository
public class PropertiesRepository {

    // I'm begging you, I'm begging you please just some proper settings file
    private final String propertiesPath;
    private Map<String, String> properties;
    private Map<String, List<String>> reverseProperties;

    Logger logger = LoggerFactory.getLogger(PropertiesRepository.class);
    CogmasterConfig cogmasterConfig;

    public boolean containsKey(String key) {
        return properties.containsKey(key);
    }

    public boolean containsValue(String value) {
        return reverseProperties.containsKey(value);
    }

    public String searchProperty(String property) {
        return properties.get(property);
    }

    public List<String> reverseSearchProperty(String propertyValue) {
        return reverseProperties.get(propertyValue);
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

    public PropertiesRepository(CogmasterConfig cogmasterConfig) {
        this.cogmasterConfig = cogmasterConfig;
        this.propertiesPath = cogmasterConfig.getProperties().getPath();
    }

    // We interface with the properties through Map because we want to utilize inverting util
    @PostConstruct
    private void PopulateProperties() {
        logger.info("Populating properties repository...");
        properties = propertiesToHashMap(loadAllProperties());
        reverseProperties = invertHashMap(properties);
        logger.info("Finished populating");
    }

}
