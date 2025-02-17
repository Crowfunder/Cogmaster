package com.crowfunder.cogmaster.Properties;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static com.crowfunder.cogmaster.Utils.HashMapUtil.invertHashMap;
import static com.crowfunder.cogmaster.Utils.HashMapUtil.propertiesToHashMap;

@Repository
public class PropertiesRepository {

    // I'm begging you, I'm begging you please just some proper settings file
    private final String propertiesFilesPath = "src/main/resources/properties";
    private Map<String, String> properties;
    private Map<String, List<String>> reverseProperties;

    Logger logger = LoggerFactory.getLogger(PropertiesRepository.class);

    public String searchProperty(String property) {
        return properties.get(property);
    }

    public List<String> reverseSearchProperty(String propertyValue) {
        return reverseProperties.get(propertyValue);
    }

    private Properties loadAllProperties() {
        Properties properties = new Properties();
        File dir = new File(propertiesFilesPath);
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    properties.putAll(loadProperties(file));
                }
            }
        }
        return properties;
    }

    private Properties loadProperties(File file) {
        try (InputStream input = new FileInputStream(file)) {
            Properties newProperties = new Properties();
            newProperties.load(input);
            return newProperties;

        } catch (IOException e) {
            logger.error("Failed to read properties file.");
            throw new RuntimeException(e);
        }
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
