package com.crowfunder.cogmaster.Properties;

import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class PropertiesService {

    private final PropertiesRepository propertiesRepository;

    public PropertiesService(PropertiesRepository propertiesRepository) {
        this.propertiesRepository = propertiesRepository;
    }

    public Optional<List<String>> searchByValue(String value) {
        return propertiesRepository.reverseSearchProperty(value);
    }

    public Optional<String> searchByKey(String property) {
        return propertiesRepository.searchProperty(property);
    }

    public Optional<String> parsePropertyString(String property) {

        // We do not care about qualified keys as we toss all bundles
        // into one dictionary anyway
        if (property.startsWith("%")) {
            property = property.substring(property.indexOf(':') + 1);
        }

        String[] args = property.split("\\|");
        String key = args[0];
        var keyVal = searchByKey(key);
        if (keyVal.isEmpty()) {
            return Optional.<String>empty();
        }

        args = args.length > 1 ? Arrays.copyOfRange(args, 1, args.length) : new String[0];

        // No need for resolving if there are no args
        if (args.length == 0) {
            return searchByKey(key);
        }

        List<String> argsValues = new ArrayList<>();
        for (String arg : args) {
            // Recursive resolution
            if (arg.contains("!")) {
                var parsed = parsePropertyString(arg.replace("\\!", "|"));
                argsValues.add(parsed.orElseGet(() -> null));
                continue;
            }

            // Tainted property, do not resolve, insert as-is
            if (arg.contains("~")) {
                argsValues.add(arg.replace("\\~", ""));
                continue;
            }

            argsValues.add(searchByKey(arg).orElseGet(() -> "(Invalid Key)"));
        }

        return keyVal.map(x -> MessageFormat.format(x, argsValues.toArray()));
    }

}
