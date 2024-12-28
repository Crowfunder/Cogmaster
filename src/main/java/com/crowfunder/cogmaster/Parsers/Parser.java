package com.crowfunder.cogmaster.Parsers;

import com.crowfunder.cogmaster.Configs.*;
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

    private Node getNextNode(Node node) {
        Node nextNode = node.getNextSibling();
        if (nextNode == null) {
            return null;
        }
        while (nextNode.getNodeType() != Node.ELEMENT_NODE) {
            nextNode = nextNode.getNextSibling();
            if (nextNode == null) {
                return null;
            }
        }
        return nextNode;
    }

    private Node getFirstChild(Node node) {
        Node childNode = node.getFirstChild();
        if (childNode == null) {
            return null;
        }
        while (childNode.getNodeType() != Node.ELEMENT_NODE) {
            childNode = childNode.getNextSibling();
        }
        return childNode;
    }

    public Index populatePathIndex() {

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

            }
        }
        catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return index;
    }

    // Parses <entry> node into a ConfigEntry object
    private ConfigEntry parseEntry(Node entry) {
        ConfigEntry configEntry = new ConfigEntry(configName);

        NodeList implementationNodes = entry.getChildNodes();
        Node parametersRoot = null;

        for (int i = 0; i < implementationNodes.getLength(); i++) {
            Node implementationNode = implementationNodes.item(i);
            if (implementationNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            switch (implementationNode.getNodeName()) {
                case "name" -> configEntry.getPath().setPath(implementationNode.getTextContent());
                case "implementation" -> {

                    // Handle derived ConfigEntries
                    if (implementationNode.getAttributes().getNamedItem("class").getNodeValue().contains("$Derived")) {

                        // <item> bierzesz jako rootnode do parseReference
                        // potem robisz loadReference na configEntry i zostawiasz
                        Node derivedRoot = getFirstChild(implementationNode);
                        if (!derivedRoot.getNodeName().equals(configName)) {
                            System.out.printf(derivedRoot.getNodeName());
                            System.out.printf(configName);
                            throw new RuntimeException("A fine punishment for laziness, somehow parameterroot wasn't the first subnode of implementation.");
                        }
                        configEntry.loadReference(parseReference(derivedRoot));
                    } else {
                        configEntry.updateOwnParameters(parseParameters(implementationNode));
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
        ConfigReference reference = new ConfigReference();
        NodeList implementationNodes = referenceRoot.getChildNodes();
        Node parameterRoot = null;
        for (int i = 0; i < implementationNodes.getLength(); i++) {
            Node implementationNode = implementationNodes.item(i);
            if (implementationNode.getNodeType() != Node.ELEMENT_NODE) { continue; }

            switch (implementationNode.getNodeName()) {
                case "name" -> reference.getPath().setPath(implementationNode.getTextContent());
                case "arguments" -> {
                    parameterRoot = implementationNode;
                    ParameterArray parameterArray = parseParameters(parameterRoot);
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
    private ParameterArray parseParameters(Node parametersRoot) {
        ParameterArray parameterArray = new ParameterArray();

        NodeList parameterNodes = parametersRoot.getChildNodes();
        for (int i = 0; i < parameterNodes.getLength(); i++) {
            Node parameterNode = parameterNodes.item(i);
            if (parameterNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            // Apply heuristics
            String key;
            ParameterValue value;

            // Heuristic 1 - Repeated nodes of the same name (concealed list)
            Node nextNode = getNextNode(parameterNode);
            if (nextNode != null) {
                if (parameterNode.getNodeName().equals(nextNode.getNodeName())) {
                    List<String> listValue = new ArrayList<>();
                    while (parameterNode.getNodeName().equals(nextNode.getNodeName())) {
                        i++;
                        if (nextNode.getNodeType() == Node.ELEMENT_NODE) {
                            listValue.add(nextNode.getTextContent());
                        }
                        nextNode = nextNode.getNextSibling();
                        while (nextNode.getNodeType() != Node.ELEMENT_NODE) {
                            nextNode = nextNode.getNextSibling();
                            i++;
                        }
                    }
                    key = parameterNode.getNodeName();
                    value = new ParameterValue(listValue);
                    parameterArray.addParameter(key, value);
                    continue;
                }
            }

            switch (parameterNode.getNodeName()) {

                // Heuristic 2 - key/value pair
                case "key" -> {
                    key = parameterNode.getTextContent();
                    Node valueNode = nextNode;

                    // In case it somehow isn't the next node
                    while (!valueNode.getNodeName().equals("value")) {
                        valueNode = getNextNode(valueNode);
                    }

                    // Heuristic 3 - ConfigReference value
                    if (valueNode.getAttributes().getNamedItem("class") != null && valueNode.getAttributes().getNamedItem("class").getNodeValue().contains("ConfigReference")) {
                        value = new ParameterValue(parseReference(valueNode));
                    } else {
                        value = new ParameterValue(valueNode.getTextContent());
                    }

                }
                case "value" -> {
                    // skip, we already took care of it. If it's orphaned - shame.
                    continue;
                }
                default -> {
                    // TODO: Handle nested node values on default
                    key = parameterNode.getNodeName();
                    value = new ParameterValue(parameterNode.getTextContent());
                }
            }
            parameterArray.addParameter(key, value);
        }
        return parameterArray;
    }



    // Populates parameter index
//    public Index populateParameterIndex(Index index) {
//
//    }

}
