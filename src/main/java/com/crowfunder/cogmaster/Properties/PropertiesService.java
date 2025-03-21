package com.crowfunder.cogmaster.Properties;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static com.crowfunder.cogmaster.Utils.ListUtil.checkOrderedSetOverlap;

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

    public static List<String> propertyKeyPad(String key) {
        List<String> result = new ArrayList<>();
        result.add(key);
        result.add("{0} " + key);
        result.add(key + " {0}");
        result.add("{0} " + key + " {1}");
        return result;
    }

    // Recursive search through properties map
    // Checks shifting blocks of words of decreasing length
    // For now IGNORES dispersed subkeys (i.e "I like {0} trains") as
    // it will be used mostly for resolving items, with simpler names
    // Works by generating arrays of indices that extract
    // values from list of individual words
    // It is a little overcomplicated, but it's done so that in case
    // it's necessary to implement handling dispersed subkeys, it will be easier
    public List<String> recursiveKeySearch(String value) {
        String[] subkeys = value.split(" ");
        int lenKey = subkeys.length;
        List<Integer> spentSubkeyIndices = new ArrayList<>();
        List<String> result = new ArrayList<>();

        for (int lenSubkey = lenKey; lenSubkey > 0; lenSubkey--) {
            for (int startIdx = 0; startIdx < lenKey-lenSubkey+1; startIdx++) {

                // Generate a continuous range of integers
                int[] subkeyIndices = IntStream.range(startIdx, startIdx+lenSubkey).toArray();

                // If any index overlaps, skip entire segment
                if (checkOrderedSetOverlap(spentSubkeyIndices, subkeyIndices)) {
                    continue;
                }

                // Check if the key or any of its "property padded" variants exist
                String subkey = String.join(" ", Arrays.stream(subkeyIndices).mapToObj(i -> subkeys[i]).toArray(String[]::new));
                for (String paddedKey : propertyKeyPad(subkey)) {
                    if (propertiesRepository.containsValue(paddedKey)) {
                        result.add(resolveValue(subkey).get(0));    // ??? the fuck we do????
                        // add spend indices to spent indices!!
                    }
                }
            }
        }
        return result;
    }


}
