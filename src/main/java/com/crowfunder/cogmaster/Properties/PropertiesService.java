package com.crowfunder.cogmaster.Properties;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PropertiesService {

    private PropertiesRepository propertiesRepository;

    public List<String> resolveValue(String value) {
        return propertiesRepository.reverseSearchProperty(value);
    }

    public String resolveKey(String property) {
        return propertiesRepository.searchProperty(property);
    }

    public PropertiesService(PropertiesRepository propertiesRepository) {
        this.propertiesRepository = propertiesRepository;
    }

}
