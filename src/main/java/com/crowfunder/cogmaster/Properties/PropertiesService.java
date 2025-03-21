package com.crowfunder.cogmaster.Properties;

import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class PropertiesService {

    private final PropertiesRepository propertiesRepository;

    public List<String> resolveValue(String value) {
        return propertiesRepository.reverseSearchProperty(value);
    }

    public String resolveKey(String property) {
        return propertiesRepository.searchProperty(property);
    }

    public PropertiesService(PropertiesRepository propertiesRepository) {
        this.propertiesRepository = propertiesRepository;
    }


    public String parsePropertyString(String property) {

        // We do not care about qualified keys as we toss all bundles
        // into one dictionary anyway
        if (property.startsWith("%")) {
            property = property.substring(property.indexOf(':') + 1);
        }

        String[] args = property.split("\\|");
        String key = args[0];
        args = args.length > 1 ? Arrays.copyOfRange(args, 1, args.length) : new String[0];

        // No need for resolving if there are no args
        if (args.length == 0) {
            return resolveKey(key);
        }

        List<String> argsValues = new ArrayList<>();
        for (String arg : args) {

            // Recursive resolution
            if (arg.contains("!")) {
                argsValues.add(parsePropertyString(arg.replace("\\!", "|")));
            }

            // Tainted property, do not resolve, insert as-is
            else if (arg.contains("~")) {
                argsValues.add(arg.replace("\\~",""));
            }

            else {
                argsValues.add(resolveKey(arg));
            }
        }

        String keyVal = resolveKey(key);
        if (keyVal == null) {
            return property;
        }
        return MessageFormat.format(keyVal, argsValues.toArray());
    }

}
