package com.crowfunder.cogmaster.Parseable;

import com.crowfunder.cogmaster.CogmasterConfig;
import com.crowfunder.cogmaster.Configs.ConfigReference;
import com.crowfunder.cogmaster.Configs.ParameterArray;
import com.crowfunder.cogmaster.Configs.ParameterValue;
import com.crowfunder.cogmaster.Configs.Path;
import com.crowfunder.cogmaster.Routers.RouterService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Repository;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static com.crowfunder.cogmaster.Utils.DOMUtil.getFirstChild;
import static com.crowfunder.cogmaster.Utils.DOMUtil.getNextNode;

@Repository
public class ParseableGraphRepository {

    Logger logger = LoggerFactory.getLogger(ParseableGraphRepository.class);
    private final RouterService routerService;
    private ArrayList<ParseableResource> parseableResources;
    private Map<String, Map<Path, NewConfigEntry>> parsedResources;

    public ParseableGraphRepository(CogmasterConfig cogmasterConfig, RouterService routerService) {
        this.routerService = routerService;
        var parseablePath = cogmasterConfig.parsers().path();
        parsedResources = new HashMap<String, Map<Path, NewConfigEntry>>();

        try {
            PathMatchingResourcePatternResolver r = new PathMatchingResourcePatternResolver();
            var parserResources = r.getResources("classpath*:/" + parseablePath + "/*.xml");
            this.parseableResources = new ArrayList<ParseableResource>();

            for (Resource resource : parserResources) {
                var parserName = resource.getFilename().split("\\.")[0];
                this.parseableResources.add(new ParseableResource(parserName, resource));
            }

        } catch (IOException e) {
            logger.error("Failed to load properties from specified path: /{}/*", parseablePath);
            throw new RuntimeException("Failed to load properties", e);
        }
    }

    @PostConstruct
    public void populateRepo() {
        logger.info("Parsing the configs, populating ConfigIndex...");
        parseResources();
        logger.info("Finished parsing");

        logger.info("Resolving derivations...");
        resolveEntryDependencies();
        logger.info("Finished resolving");

        logger.info("Populating routed params...");
        populateRoutedParameters();
        logger.info("Finished resolving");

    }

    public void parseResources() {
        var counter = 0;
        for (var parseableResource : parseableResources) {
            logger.debug("File: " + parseableResource.name);
            var parsedEntries = parseResource(parseableResource);
            parsedResources.put(parseableResource.name, parsedEntries);
            counter++;
            logger.debug("Parsed " + counter + " entires.");
        }
    }

    // Parse a resource file from parseable
    public Map<Path, NewConfigEntry> parseResource(ParseableResource parseableResource) {
        var parsedEntries = new HashMap<Path, NewConfigEntry>();

        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(parseableResource.resource.getInputStream());
            doc.getDocumentElement().normalize();

            // All config entires are contained within the object node
            Node rootNode = doc.getElementsByTagName("object").item(0);

            if (rootNode == null) {
                throw new RuntimeException("Unable to locate root \"<object>\" node");
            }

            // Start iterating over entries
            NodeList entries = rootNode.getChildNodes();
            logger.debug("Parsing \"{}\" resource file.", parseableResource.name);
            logger.debug("Found {} nodes.", entries.getLength());
            for (int i = 0; i < entries.getLength(); i++) {
                Node entry = entries.item(i);

                if (entry.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                var parsedEntry = parseEntry(parseableResource.name, entry);
                if (parsedEntries.containsKey(parsedEntry.path)) {
                    logger.error("Already registered entry with path {0} from file {1}.", parsedEntry.configFileName,
                            parsedEntry.path);
                }
                parsedEntries.put(parsedEntry.path, parsedEntry);
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            logger.error(e.toString());
            throw new RuntimeException(e);
        }
        return parsedEntries;
    }

    // Parses <entry> node into a NewConfigEntry object
    private NewConfigEntry parseEntry(String configFileName, Node entry) {
        NewConfigEntry parsedEntry = new NewConfigEntry(configFileName);

        NodeList implementationNodes = entry.getChildNodes();

        for (int i = 0; i < implementationNodes.getLength(); i++) {
            Node implementationNode = implementationNodes.item(i);
            if (implementationNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            switch (implementationNode.getNodeName()) {
                case "name" -> parsedEntry.path.setPath(implementationNode.getTextContent());
                case "implementation" -> {

                    // Handle derived ConfigEntries
                    String implementationType = implementationNode.getAttributes().getNamedItem("class").getNodeValue();
                    if (implementationType == null) {
                        logger.debug("Unable to locate implementation of \"<implementation>\" node");
                        implementationType = "ConfigEntry";
                    }
                    parsedEntry.implementationType = implementationType;
                    if (implementationType.contains("$Derived")) {
                        Node parentReferenceRootNode = getFirstChild(implementationNode);
                        if (parentReferenceRootNode == null) {
                            continue;
                        }
                        if (!parentReferenceRootNode.getNodeName().equals(configFileName)) {
                            logger.debug("Derived config parameter root node name different from config file name.");
                        }

                        var parsedReference = parseReference(configFileName, parentReferenceRootNode);
                        parsedEntry.parentReference = parsedReference;

                    } else {
                        var parsedParameterArray = parseParameterArray(implementationNode);
                        parsedEntry.entryParameters.update(parsedParameterArray);
                    }
                }
                case "parameters" -> {
                    // Parameters are unnecessary for now.
                    continue;
                }
                default -> {
                    continue;
                }
            }

        }
        return parsedEntry;
    }

    // parses the parent reference info from an <implementation> node
    private NewConfigEntryReference parseReference(String configFileName, Node referenceRoot) {
        NewConfigEntryReference reference = new NewConfigEntryReference(configFileName);
        NodeList implementationNodes = referenceRoot.getChildNodes();
        Node parameterRoot;
        for (int i = 0; i < implementationNodes.getLength(); i++) {
            Node implementationNode = implementationNodes.item(i);
            if (implementationNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            switch (implementationNode.getNodeName()) {
                case "name" -> reference.getPath().setPath(implementationNode.getTextContent());
                case "arguments" -> {
                    parameterRoot = implementationNode;
                    ParameterArray parameterArray = parseParameterArray(parameterRoot);
                    reference.getParameters().update(parameterArray);
                }
                default -> {
                    continue;
                }
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

                    // Heuristic 3 - newConfigReference value
                    // if (nextNode.getAttributes().getNamedItem("class") != null &&
                    // nextNode.getAttributes().getNamedItem("class").getNodeValue().contains("newConfigReference"))
                    // {
                    // value = new ParameterValue(parseReference(nextNode));
                    // } else {
                    // }
                    value = parseParameterValue(nextNode); // newConfigReference values are too confusing, to be
                                                           // consulted

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

    public void resolveEntryDependencies() {
        var counter = 0;
        for (String configFileName : parsedResources.keySet()) {
            logger.debug("File: " + configFileName);
            for (Path entryPath : parsedResources.get(configFileName).keySet()) {
                var configEntry = parsedResources.get(configFileName).get(entryPath);
                resolveParent(configEntry);
                logger.debug("Resolved parent for entry " + counter + ":" + configEntry.path);
                // name index and pretty name index now cached by {@link NameIndexService}
            }
        }
    }

    public void populateRoutedParameters() {
        var counter = 0;
        for (String configFileName : parsedResources.keySet()) {
            logger.debug("File: " + configFileName);
            for (Path entryPath : parsedResources.get(configFileName).keySet()) {
                var configEntry = parsedResources.get(configFileName).get(entryPath);

                var sourceRouter = routerService.getRouter(configEntry.getRootImplementationType());

                if (sourceRouter != null) {
                    for (Map.Entry<String, Path> e : sourceRouter.getRoutes().entrySet()) {
                        var effectiveParameterFlex = configEntry.getEffectiveParameters()
                                .resolveParameterPathFlex(e.getValue());
                        configEntry.routedParameters.addParameter(e.getKey(), effectiveParameterFlex);
                    }
                }
                logger.debug("Resolved rooted params for entry " + counter + ":" + configEntry.path);
            }
        }
    }

    // find and set the parent of this entry
    private void resolveParent(NewConfigEntry configEntry) {
        if (configEntry.parentReference == null) {
            return;
        }

        NewConfigEntry parentConfigEntry = parsedResources.get(configEntry.configFileName)
                .get(configEntry.parentReference.getPath());
        if (parentConfigEntry == null) {
            logger.error("Config entry's parent not found.Source File: {0} Entry: {1} Reference: {2} ",
                    configEntry.configFileName, configEntry.parentReference.getPath(), configEntry.configFileName);
        } else {
            configEntry.parentReference.referencedEntry = parentConfigEntry;
            parentConfigEntry.childEntries.add(configEntry);
        }
    }

    public Map<String, Map<Path, NewConfigEntry>> getAll() {
        return parsedResources;
    }

    // Get ConfigEntry object by its config path
    public Optional<NewConfigEntry> resolveConfig(String configFileName, String entryPath) {
        return resolveConfig(configFileName, new Path(entryPath));
    }

    // Get ConfigEntry object by its config path
    public Optional<NewConfigEntry> resolveConfig(String configFileName, Path entryPath) {
        return readConfigIndex(configFileName, entryPath);
    }

    public Optional<NewConfigEntry> readConfigIndex(String configFileName, Path entryPath) {
        return Optional.ofNullable(parsedResources.get(configFileName))
                .map(entryMap -> entryMap.get(entryPath));
    }

    // Get ConfigEntry by path that leads both to the correct index and entry
    public Optional<NewConfigEntry> resolveConfigFullPath(Path fileAndEntryPath) {
        return readConfigIndex(fileAndEntryPath.getNextPath(), fileAndEntryPath.rotatePath());
    }

    // Get ConfigEntry by path that leads both to the correct index and entry within
    public Optional<NewConfigEntry> resolveConfigFullPath(String fileAndEntryPath) {
        return resolveConfigFullPath(new Path(fileAndEntryPath));
    }

    // Get ConfigEntry object by resolving a ConfigReference object
    public Optional<NewConfigEntry> resolveConfig(ConfigReference configReference) {
        return readConfigIndex(configReference.getSourceConfig(), configReference.getPath());
    }

    // Get multiple ConfigEntry objects by paths
    // Works only for full paths (indicating the exact PathIndex entry)
    public List<NewConfigEntry> resolveConfigsFullPath(List<Path> paths) {
        List<NewConfigEntry> configs = new ArrayList<>();
        for (Path path : paths) {
            resolveConfigFullPath(path).ifPresent(entry -> configs.add(entry));
        }
        return configs;
    }

}