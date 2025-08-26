package com.crowfunder.cogmaster.Parseable;

import org.springframework.core.io.Resource;

public class ParseableResource {
    public String name;
    public Resource resource;

    public ParseableResource(String name, Resource resource) {
        this.name = name;
        this.resource = resource;
    }
}
