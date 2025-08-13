package com.crowfunder.cogmaster.Parseable;

import com.crowfunder.cogmaster.CogmasterConfig;
import com.crowfunder.cogmaster.Configs.ConfigEntry;
import com.crowfunder.cogmaster.Configs.ParameterArray;
import com.crowfunder.cogmaster.Configs.ParameterValue;
import com.crowfunder.cogmaster.Configs.Path;
import com.crowfunder.cogmaster.Index.Index;
import com.crowfunder.cogmaster.Parsers.ParserService;
import com.crowfunder.cogmaster.Translations.TranslationsService;
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

class ParseableResource {
    public String name;
    public Resource resource;

    public ParseableResource(String name, Resource resource) {
        this.name = name;
        this.resource = resource;
    }
}

class NewConfigEntry {
    // Source config file name
    public final String configFileName;
    // comes from <name>
    public final Path path;
    public String implementationType;

    public NewConfigEntryReference parentReference;
    public final ArrayList<NewConfigEntry> childEntries;

    // // If the config is a derived config, this path points to the derived from
    // // (parent) config
    // public final Path parentPath;

    // does not contain parent parameters
    public final ParameterArray entryParameters;
    // public final ParameterArray routedParameters;
    // // Non-overriden parameters pulled from all derivative (parent) configs
    // public final ParameterArray derivedParameters;

    // Parameterless
    public NewConfigEntry(String configFileName) {
        this.configFileName = configFileName;
        this.path = new Path();
        this.implementationType = "";
        this.childEntries = new ArrayList<NewConfigEntry>();
        // this.parentPath = new Path(); // Empty string for no derivation
        this.entryParameters = new ParameterArray();
        // this.derivedParameters = new ParameterArray();
        // this.routedParameters = new ParameterArray();
    }
}

class NewConfigEntryReference {

    private final String implementationType = "com.threerings.config.ConfigReference";
    // name of config file from where the reference was parsed
    private final String sourceConfigFileName;
    // comes from <name>
    private final Path path;
    // Overridden parameters
    private final ParameterArray parameters;
    // entry pointed to by this config. populated after creation
    public NewConfigEntry referencedEntry;

    public NewConfigEntryReference(String sourceConfig) {
        this.path = new Path();
        this.parameters = new ParameterArray();
        this.sourceConfigFileName = sourceConfig;
    }

    public Path getPath() {
        return this.path;
    }

    public ParameterArray getParameters() {
        return this.parameters;
    }

    public String getSourceConfigFileName() {
        return this.sourceConfigFileName;
    }

    public String getImplementationType() {
        return this.implementationType;
    }
}

@Repository
class ParseableGraphRepository {

    Logger logger = LoggerFactory.getLogger(ParseableGraphRepository.class);
    private ArrayList<ParseableResource> parseableResources;
    private Map<String, Map<Path, NewConfigEntry>> parsedResources;

    public ParseableGraphRepository(CogmasterConfig cogmasterConfig) {
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
    public void populateIndex() {
        logger.info("Parsing the configs, populating ConfigIndex...");
        parseResources();
        logger.info("Finished parsing");

        logger.info("Resolving derivations...");
        resolveEntryDependencies();
        logger.info("Finished resolving");
    }

    public void parseResources() {
        for (var parseableResource : parseableResources) {
            var parsedEntries = parseResource(parseableResource);
            parsedResources.put(parseableResource.name, parsedEntries);
        }
    }

    // Parse a resource file from parseable
    public Map<Path, NewConfigEntry> parseResource(ParseableResource parseableResource) {
        var parsedEntries = new HashMap<Path, NewConfigEntry>();

        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(parseableResource.resource.getInputStream());
            doc.getDocumentElement().normalize();

            // All configs start at object node
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
        for (String configFileName : parsedResources.keySet()) {
            for (Path entryPath : parsedResources.get(configFileName).keySet()) {
                var configEntry = parsedResources.get(configFileName).get(entryPath);

                // Resolve derivations
                resolveParent(configEntry);

                // Populate routed parameters
                // configEntry.populateRoutedParameters(routerService.getRouter(configEntry));

                // Populate name index
                // String name = configEntry.getName();
                // if (name != null && !name.isEmpty()) {
                // index.addNameIndexEntry(translationsService.parseTranslationString(name).orElseGet(()
                // -> null),
                // entryPath,
                // configFileName);
                // }
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
        // ParameterArray derivedParameters = new ParameterArray();
        // while (parentConfigEntry != null) {
        // derivedParameters.update(parentConfigEntry.getParameters()); // would this
        // not mean the parent potentially
        // // overwriting the child parameters?
        // if (!parentConfigEntry.isDerived()) {
        // configEntry.setDerivedImplementationType(parentConfigEntry.getImplementationType());
        // }
        // parentConfigEntry = readConfigIndex(configEntry.getSourceConfig(),
        // parentConfigEntry.getDerivedPath());
        // }
        // configEntry.updateDerivedParameters(derivedParameters);
    }

    public Map<String, Map<Path, NewConfigEntry>> getAll() {
        return parsedResources;
    }
}