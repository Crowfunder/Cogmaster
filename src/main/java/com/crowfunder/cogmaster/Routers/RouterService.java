package com.crowfunder.cogmaster.Routers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class RouterService {

    private final Map<String, Router> routers = new HashMap<String, Router>();
    private final String routersPath = "routers";

    Logger logger = LoggerFactory.getLogger(RouterService.class);

    private void loadRouters() {
        YAMLMapper yamlMapper = new YAMLMapper();
        try {
            PathMatchingResourcePatternResolver r = new PathMatchingResourcePatternResolver();
            Resource[] resources = r.getResources("/" + routersPath + "/*");
            for (Resource resource : resources) {
                Router router = yamlMapper.readValue(resource.getInputStream(), Router.class);
                routers.put(router.getImplementation(), router);
            }
        } catch(IOException e) {
            logger.error("Failed to load routers from specified path: " + "/" + routersPath +"/*");
            throw new RuntimeException("Failed to load routers");
        }
    }

    public RouterService() {
        logger.info("Loading routers...");
        loadRouters();
        logger.info("Finished loading");
    }
}
