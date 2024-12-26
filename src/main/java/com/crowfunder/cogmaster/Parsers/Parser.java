package com.crowfunder.cogmaster.Parsers;

import com.crowfunder.cogmaster.Configs.ConfigEntry;
import com.crowfunder.cogmaster.Configs.ParameterArray;
import com.crowfunder.cogmaster.Configs.ParameterValue;
import com.crowfunder.cogmaster.Configs.Path;
import com.crowfunder.cogmaster.Index.Index;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Parser {

    // Path to parsed xml config file
    private final String xmlFilePath;

    // Config name, should also correspond to root node of parameters in derived entries
    private final String configName;

    // List of paths leading to parameters to index into ParameterIndex
    private final List<Path> indexableParameterPaths;

    // Returnable index
    // I want the exported index to be accessible in other methods of this class, without constraints
    // Is this the right way? No idea.
    // Do I care? No idea.
    Index index = new Index();

    public Parser(String configName, String xmlFilePath, List<Path> indexableParameterPaths) {
        this.xmlFilePath = xmlFilePath;
        this.indexableParameterPaths = indexableParameterPaths;
        this.configName = configName;
    }

    // Temporary constructor until reverse indexing works
    public Parser(String configName, String xmlFilePath) {
        this.xmlFilePath = xmlFilePath;
        this.indexableParameterPaths = new ArrayList<>();
        this.configName = configName;

    }

    public String getXmlFilePath() {
        return xmlFilePath;
    }

    public List<Path> getIndexableParameterPaths() {
        return indexableParameterPaths;
    }


    public Index parse() {

        // Restart the index for parsing
        index = new Index();

        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(new File(xmlFilePath));
            doc.getDocumentElement().normalize();

            // Start reading
            // All configs start at object node
            Node rootNode = doc.getElementsByTagName("object").item(0);

            if (rootNode == null) {
                throw new RuntimeException("Unable to locate root \"<object>\" node");
            }

            // Start iterating over entries
            NodeList entries = rootNode.getChildNodes();
            for (int i = 0; i < entries.getLength(); i++) {
                Node entry = entries.item(i);

                // Typechecking may take eons, please don't take eons
                if (entry.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                ConfigEntry configEntry = parseEntry(entry);

                index.addPathIndexEntry(configEntry.getPath(), configEntry);

                // Todo Add derivedParameter resolving
                // We want it done when parsing, not on runtime since it may be a heavy operation
                // Theoretically speaking IndexRepository can resolve them on-read and they will be cached
                // until the next restart, but we want to reduce runtime overhead as much as possible
                // or we can resolve derivations not in the Parser, but in the IndexRepository, once
                // parsing is complete
                // though that collides with populating the parameterindex
                // It probably has to go before parameterindex parsing,
                // honestly, whether this parser is O(2n) or O(10n) doesn't matter much,
                // startup can be as long as it's needed
                // it should just be as responsive as possible

                // About derivedParameter resolving:
                // create empty hashmap
                // while deriverPath != ""
                // get parent parameters
                // add all parameters to the hashmap
                // when done, update configEntry derivedParameters with new parameters

                // HERE GOES THE CODE THAT READS THROUGH THE CONFIGENTRY
                // TO ADD ENTRIES INTO PARAMETERINDEX
                // unfinished, Todo when I decide to take care of reverse search
//                for (Path path : indexableParameterPaths) {
//                    ParameterValue val = configEntry.getParameters().resolveParameterPath(path);
//                    index.addParameterIndexEntry(path, );
//                }



            }


        }
        catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return index;
    }

    private ConfigEntry parseEntry(Node entry) {
        ConfigEntry configEntry = new ConfigEntry();

        NodeList implementationNodes = entry.getChildNodes();

        Node parametersRoot;

        for (int i = 0; i < implementationNodes.getLength(); i++) {
            Node implementationNode = implementationNodes.item(i);
            if (implementationNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            switch (implementationNode.getNodeName()) {
                case "name" -> configEntry.getPath().setPath(implementationNode.getTextContent());
                case "implementation" -> {
                    // jeśli derived, parameterroot to subnode tego o nazwie configname
                    // + name w parameterroot to derivedPath
                    // jeśli nie, parameterroot to ten node
                    if (implementationNode.getAttributes().getNamedItem("class").getNodeValue().contains("Derived")) {
                        parametersRoot = implementationNode.getFirstChild();
                        if (!parametersRoot.getNodeName().equals(configName)) {
                            System.out.printf(parametersRoot.getNodeName());
                            System.out.printf(configName);
                            throw new RuntimeException("A fine punishment for laziness, somehow parameterroot wasn't the first subnode of implementation.")
                        }
                    } else {
                        parametersRoot = implementationNode;
                    }
                } case "parameters" -> {
                    // Parameters are unnecessary for now.
                    continue;
                }
                default -> {
                    continue;
                }
            }
        }

        configEntry.getParameters().update(parseParameters(parametersRoot));

        return configEntry;
    }


    // This method holds some heuristics for parsing parameters
    // There are some cases when it's not a simple name and value of node read
    // Notably:
    // - key/value node pairs
    // - repeated nodes of the same name (concealed lists)
    // - values as config references
    private ParameterArray parseParameters(Node parametersRoot) {
        ParameterArray parameterArray = new ParameterArray();

        return parameterArray;
    }

}
