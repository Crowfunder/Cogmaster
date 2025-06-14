package com.crowfunder.cogmaster.Translations;

import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.*;

@Service
public class TranslationsService {

    private final TranslationsRepository translationsRepository;

    public TranslationsService(TranslationsRepository translationsRepository) {
        this.translationsRepository = translationsRepository;
    }

    public Optional<List<String>> searchByValue(String value) {
        return translationsRepository.reverseSearchTranslation(value);
    }

    public Optional<String> searchByKey(String property) {
        return translationsRepository.searchTranslation(property);
    }

    public Optional<String> parseTranslationString(String translation) {

        // We do not care about qualified keys as we toss all bundles
        // into one dictionary anyway
        if (translation.startsWith("%")) {
            translation = translation.substring(translation.indexOf(':') + 1);
        }

        String[] args = translation.split("\\|");
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
                var parsed = parseTranslationString(arg.replace("\\!", "|"));
                argsValues.add(parsed.orElseGet(() -> null));
                continue;
            }

            // Tainted translation, do not resolve, insert as-is
            if (arg.contains("~")) {
                argsValues.add(arg.replace("\\~", ""));
                continue;
            }

            argsValues.add(searchByKey(arg).orElseGet(() -> "(Invalid Key)"));
        }

        return keyVal.map(x -> MessageFormat.format(x, argsValues.toArray()));
    }

    public Set<String> getAllTranslationKeys() {
        return translationsRepository.getAllTranslationKeys();
    }

}
