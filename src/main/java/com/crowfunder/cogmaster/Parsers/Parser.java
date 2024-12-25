package com.crowfunder.cogmaster.Parsers;

import com.crowfunder.cogmaster.Configs.ConfigEntry;
import com.crowfunder.cogmaster.Configs.Path;
import com.crowfunder.cogmaster.Index.Index;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Parser {

    // Path to parsed xml config file
    private final String xmlFilePath;

    // List of paths leading to parameters to index into ParameterIndex
    private final List<Path> indexableParameterPaths;

    // The base PathIndex that maps config paths to their respective ConfigEntry objects.
    private final HashMap<Path, ConfigEntry> pathIndex = new HashMap<>();

    // Index mapping specific parameter values to ConfigEntry paths from pathIndex
    private final HashMap<Path, HashMap<String, List<Path>>> parameterIndex = new HashMap<>();

    public Parser(String xmlFilePath, List<Path> indexableParameterPaths) {
        this.xmlFilePath = xmlFilePath;
        this.indexableParameterPaths = indexableParameterPaths;
    }

    public Parser(String xmlFilePath) {
        this(xmlFilePath, new ArrayList<>());
    }

    public String getXmlFilePath() {
        return xmlFilePath;
    }

    public List<Path> getIndexableParameterPaths() {
        return indexableParameterPaths;
    }

    public Index parse() {
        Index index = new Index();

        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(new File(xmlFilePath));
            doc.getDocumentElement().normalize();
        }
        catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }



        return index;
    }
}
