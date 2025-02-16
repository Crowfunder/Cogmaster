package com.crowfunder.cogmaster.Parsers;

import com.crowfunder.cogmaster.Configs.*;
import com.crowfunder.cogmaster.Index.Index;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
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

import static com.crowfunder.cogmaster.Utils.DOMUtil.getFirstChild;
import static com.crowfunder.cogmaster.Utils.DOMUtil.getNextNode;

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

    Logger logger = LoggerFactory.getLogger(Parser.class);

    public String getXmlFilePath() {
        return xmlFilePath;
    }

    public List<Path> getIndexableParameterPaths() {
        return indexableParameterPaths;
    }


    public Index populateConfigIndex() {

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
            logger.debug("Parsing \"{}\" config.", configName);
            logger.debug("Found {} entries.", entries.getLength());
            for (int i = 0; i < entries.getLength(); i++) {
                Node entry = entries.item(i);

                if (entry.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                ConfigEntry configEntry = parseEntry(entry);
                index.addConfigIndexEntry(configName, configEntry.getPath(), configEntry);

            }
        }
        catch (ParserConfigurationException | SAXException | IOException e) {
            logger.error(e.toString());
            throw new RuntimeException(e);
        }

        return index;
    }


    // Parses <entry> node into a ConfigEntry object
    private ConfigEntry parseEntry(Node entry) {
        ConfigEntry configEntry = new ConfigEntry(configName);

        NodeList implementationNodes = entry.getChildNodes();

        for (int i = 0; i < implementationNodes.getLength(); i++) {
            Node implementationNode = implementationNodes.item(i);
            if (implementationNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            switch (implementationNode.getNodeName()) {
                case "name" -> configEntry.getPath().setPath(implementationNode.getTextContent());
                case "implementation" -> {

                    // Handle derived ConfigEntries
                    String implementationType = implementationNode.getAttributes().getNamedItem("class").getNodeValue();
                    if (implementationType == null) {
                        logger.debug("Unable to locate implementation of \"<implementation>\" node");
                        implementationType = "ConfigEntry";
                    }
                    configEntry.setImplementationType(implementationType);
                    if (implementationType.contains("$Derived")) {
                        Node derivedRoot = getFirstChild(implementationNode);
                        if (derivedRoot == null) { continue; }
                        if (!derivedRoot.getNodeName().equals(configName)) {
                            logger.debug("Derived config parameter root node name different from config name.");
                        }
                        configEntry.loadReference(parseReference(derivedRoot));

                    } else {
                        configEntry.updateOwnParameters(parseParameterArray(implementationNode));
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
        return configEntry;
    }


    private ConfigReference parseReference(Node referenceRoot) {
        ConfigReference reference = new ConfigReference(configName);
        NodeList implementationNodes = referenceRoot.getChildNodes();
        Node parameterRoot;
        for (int i = 0; i < implementationNodes.getLength(); i++) {
            Node implementationNode = implementationNodes.item(i);
            if (implementationNode.getNodeType() != Node.ELEMENT_NODE) { continue; }

            switch (implementationNode.getNodeName()) {
                case "name" -> reference.getPath().setPath(implementationNode.getTextContent());
                case "arguments" -> {
                    parameterRoot = implementationNode;
                    ParameterArray parameterArray = parseParameterArray(parameterRoot);
                    reference.getParameters().update(parameterArray);
                }
                default -> {continue;}
            }
        }

        return reference;
    }


    // This method holds some heuristics for parsing parameters
    // There are some cases when it's not a simple name and value of node read
    // Notably:
    // - key/value node pairs
    // - repeated nodes of the same name (concealed lists)
    // - values as config references
    private ParameterArray parseParameterArray(Node parametersRoot) {
        ParameterArray parameterArray = new ParameterArray();

        NodeList parameterNodes = parametersRoot.getChildNodes();
        for (int i = 0; i < parameterNodes.getLength(); i++) {
            Node parameterNode = parameterNodes.item(i);
            if (parameterNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            String key;
            ParameterValue value;

            // Heuristic 1 - Repeated nodes of the same name (concealed list)
            Node nextNode = getNextNode(parameterNode);
            if (nextNode != null && parameterNode.getNodeName().equals(nextNode.getNodeName())) {
                List<ParameterValue> listValue = new ArrayList<>();
                listValue.add(parseParameterValue(parameterNode));
                while (nextNode != null && parameterNode.getNodeName().equals(nextNode.getNodeName())) {
                    i++;
                    if (nextNode.getNodeType() == Node.ELEMENT_NODE) {
                        listValue.add(parseParameterValue(nextNode));
                    }
                    nextNode = nextNode.getNextSibling();
                    while (nextNode != null && nextNode.getNodeType() != Node.ELEMENT_NODE) {
                        nextNode = nextNode.getNextSibling();
                        i++;
                    }
                }
                key = parameterNode.getNodeName();
                value = new ParameterValue(listValue);
                parameterArray.addParameter(key, value);
                continue;
            }

            switch (parameterNode.getNodeName()) {

                // Heuristic 2 - key/value pair
                case "key" -> {
                    key = parameterNode.getTextContent();

                    // Seldom does it happen, but sometimes key exists without a value node
                    // We can't jump to default so here we go redundancy!
                    if (nextNode == null || !nextNode.getNodeName().equals("value")) {
                        key = parameterNode.getNodeName();
                        value = parseParameterValue(parameterNode);
                        break;
                    }

                    // Heuristic 3 - ConfigReference value
                    if (nextNode.getAttributes().getNamedItem("class") != null && nextNode.getAttributes().getNamedItem("class").getNodeValue().contains("ConfigReference")) {
                        value = new ParameterValue(parseReference(nextNode));
                    } else {
                        value = parseParameterValue(nextNode);
                    }

                }
                case "value" -> {
                    // skip, we already took care of it. If it's orphaned - shame.
                    continue;
                }
                default -> {
                    key = parameterNode.getNodeName();
                    value = parseParameterValue(parameterNode);
                }
            }
            parameterArray.addParameter(key, value);
        }
        return parameterArray;
    }


    private ParameterValue parseParameterValue(Node parameterNode) {
        ParameterValue parameterValue;

        // I genuinely hate you java
        // https://stackoverflow.com/questions/20089661/how-to-get-child-nodes-with-element-node-type-only/20091101
        if (((Element) parameterNode).getElementsByTagName("*").getLength() != 0) {
            parameterValue = new ParameterValue(parseParameterArray(parameterNode));
        } else {
            parameterValue = new ParameterValue(parameterNode.getTextContent());
        }
        return parameterValue;
    }


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

}
