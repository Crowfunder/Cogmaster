package com.crowfunder.cogmaster;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "cogmaster")
public record CogmasterConfig(Translations translations, Routers routers, Parsers parsers, Assets assets) {

    public record Translations(String path) {
    }

    public record Routers(String path) {
    }

    public record Parsers(String path) {
    }

    public record Assets(String path) {
    }
}
