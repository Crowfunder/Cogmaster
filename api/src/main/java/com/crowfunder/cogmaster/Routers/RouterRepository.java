package com.crowfunder.cogmaster.Routers;

import com.crowfunder.cogmaster.CogmasterConfig;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Repository
public class RouterRepository {

    Logger logger = LoggerFactory.getLogger(RouterRepository.class);
    private final Map<String, Router> routers = new HashMap<>();
    private final String routersPath;

    public RouterRepository(CogmasterConfig cogmasterConfig) {
        this.routersPath = cogmasterConfig.routers().path();
        logger.info("Loading routers...");
        loadRouters();
        logger.info("Finished loading");
    }

    private void loadRouters() {
        YAMLMapper yamlMapper = new YAMLMapper();
        try {
            PathMatchingResourcePatternResolver r = new PathMatchingResourcePatternResolver();
            Resource[] resources = r.getResources("classpath*:/" + routersPath + "/**/*.yml");
            for (Resource resource : resources) {
                Router router = yamlMapper.readValue(resource.getInputStream(), Router.class);
                routers.put(router.getImplementation(), router);
            }
        } catch (IOException e) {
            logger.error("Failed to load routers from specified path: /{}/*", routersPath);
            throw new RuntimeException("Failed to load routers");
        }
    }

    public String getRoutersPath() {
        return routersPath;
    }

    public Map<String, Router> getRouters() {
        return routers;
    }
}
