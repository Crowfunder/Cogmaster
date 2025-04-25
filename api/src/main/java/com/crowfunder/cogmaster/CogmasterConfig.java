package com.crowfunder.cogmaster;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "cogmaster")
public record CogmasterConfig(Properties properties, Routers routers, Parsers parsers) {

    public record Properties(String path) {
    }

    public record Routers(String path) {
    }

    public record Parsers(String path, List<String> list) {
    }
}
