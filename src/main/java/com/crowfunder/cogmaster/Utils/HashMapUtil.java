package com.crowfunder.cogmaster.Utils;

import java.util.*;
import java.util.stream.Collectors;

public class HashMapUtil {

    // Inverts a hashmap into a hashmap mapping values to one or more keys (in case of duplicates)
    public static Map<?, ?> invertHashMap(Map<?, ?> hashMap) {
        return hashMap.entrySet()
                .stream()
                .collect(Collectors.groupingBy(
                        Map.Entry::getValue,  // Use Map.Entry here
                        Collectors.mapping(
                                Map.Entry::getKey,  // And here
                                Collectors.toList()
                        )
                ));
    }

    // Converts Properties to HashMap
    public static Map<String, String> propertiesToHashMap(Properties prop) {
        return prop.entrySet().stream().collect(
                Collectors.toMap(
                        e -> String.valueOf(e.getKey()),
                        e -> String.valueOf(e.getValue()),
                        (prev, next) -> next, HashMap::new
                ));
    }

}
