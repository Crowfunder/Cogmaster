package com.crowfunder.cogmaster;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "cogmaster")
public class CogmasterConfig {

    private Properties properties;
    private Routers routers;
    private Parsers parsers;


    public static class Properties {
        private String path;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }

    public static class Routers {
        private String path;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }

    public static class Parsers {
        private String path;
        private List<String> list;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public List<String> getList() {
            return list;
        }

        public void setList(List<String> list) {
            this.list = list;
        }
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public Routers getRouters() {
        return routers;
    }

    public void setRouters(Routers routers) {
        this.routers = routers;
    }

    public Parsers getParsers() {
        return parsers;
    }

    public void setParsers(Parsers parsers) {
        this.parsers = parsers;
    }

}
