package com.crowfunder.cogmaster.Parsers;

import com.crowfunder.cogmaster.CogmasterConfig;
import com.crowfunder.cogmaster.Index.Index;
import com.crowfunder.cogmaster.Properties.PropertiesRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ParserService {

    Logger logger = LoggerFactory.getLogger(ParserService.class);
    private final List<Parser> parsers;
    private final String parseablePath;

    public ParserService(CogmasterConfig cogmasterConfig) {
        // Initialize existing parsers, probably not the best idea to do it like that
        // but oh well
        this.parsers = new ArrayList<>();
        this.parseablePath = cogmasterConfig.parsers().path();

        try {
            PathMatchingResourcePatternResolver r = new PathMatchingResourcePatternResolver();
            var parserResources = r.getResources("classpath*:/" + this.parseablePath + "/*.xml");

            for (Resource resource : parserResources) {
                var parserName = resource.getFilename().split("\\.")[0];
                this.parsers.add(new Parser(parserName, resource));
            }

        } catch (IOException e) {
            logger.error("Failed to load properties from specified path: /{}/*", this.parseablePath);
            throw new RuntimeException("Failed to load properties", e);
        }
    }

    public Index populateConfigIndex() {
        Index index = new Index();
        for (Parser parser : parsers) {
            index.update(parser.populateConfigIndex());
        }
        return index;
    }
}
