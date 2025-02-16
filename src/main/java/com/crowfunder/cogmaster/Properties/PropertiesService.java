package com.crowfunder.cogmaster.Properties;

import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class PropertiesService {

    private PropertiesRepository propertiesRepository;

    public ArrayList<String> resolveName(String name) {
        return propertiesRepository.reverseSearchProperty(name);
    }

    public String resolveProperty(String property) {
        return propertiesRepository.searchProperty(property);
    }

    public PropertiesService(PropertiesRepository propertiesRepository) {
        this.propertiesRepository = propertiesRepository;
    }

}
