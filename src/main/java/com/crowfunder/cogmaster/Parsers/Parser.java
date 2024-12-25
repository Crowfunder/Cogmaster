package com.crowfunder.cogmaster.Parsers;

import java.util.List;

public class Parser {

    // Path to parsed xml config file
    private String xmlPath;

    // List of paths leading to parameters to index into ParameterIndex
    private List<String> indexableParameterPaths;

    public Parser(String xmlPath, List<String> indexableParameterPaths) {
        this.xmlPath = xmlPath;
        this.indexableParameterPaths = indexableParameterPaths;
    }

    public String getXmlPath() {
        return xmlPath;
    }

    public List<String> getIndexableParameterPaths() {
        return indexableParameterPaths;
    }
}
