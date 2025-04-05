package com.crowfunder.cogmaster.Routers;

import com.crowfunder.cogmaster.Configs.Path;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;

public class Router {
    private final String implementation;
    private final HashMap<String, Path> routes;

    public String getImplementation() {
        return implementation;
    }

    public HashMap<String, Path> getRoutes() {
        return routes;
    }

    public Router(@JsonProperty("implementation") String implementation, @JsonProperty("routes") HashMap<String, Path> routes) {
        this.implementation = implementation;
        this.routes = routes;
    }


}
