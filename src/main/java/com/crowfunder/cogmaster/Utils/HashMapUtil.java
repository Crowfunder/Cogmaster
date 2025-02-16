package com.crowfunder.cogmaster.Utils;

import java.util.*;
import java.util.stream.Collectors;

public class HashMapUtil {

    // Inverts a hashmap into a hashmap mapping values to one or more keys (in case of duplicates)
    public static HashMap<?, ?> invertHashMap(Map<?, ?> hashMap) {
        return hashMap.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getValue,
                        entry -> new ArrayList<>(Collections.singletonList(entry.getKey())),
                        (list1, list2) -> {
                            list1.addAll(list2);
                            return list1;
                        },
                        HashMap::new
                ));
    }

}
