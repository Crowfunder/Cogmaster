package com.crowfunder.cogmaster.Utils;

import java.util.ArrayList;
import java.util.List;

public class StringUtil {

    // Generates capitalization and spacing variants of string
    // "Foo Bar" -> "Foo Bar", "FooBar", "foo Bar", "fooBar", "foobar"
    public static List<String> generateStringVariants(String s) {
        List<String> variants = new ArrayList<>();
        variants.add(s);
        variants.add(s.replace(" ", ""));
        variants.add(Character.toLowerCase(s.charAt(0)) + s.substring(1));
        variants.add(variants.get(1).substring(0, 1).toLowerCase() + variants.get(1).substring(1));
        variants.add(s.toLowerCase().replace(" ", ""));
        return variants;
    }

    // Uppercase first letters
    // "foo bar" -> Foo Bar
    public static String uppercaseFirstLetters(String s) {
        StringBuilder capitalized = new StringBuilder();
        String[] words = s.split(" ");

        for (String word : words) {
            if (!word.isEmpty()) {
                capitalized.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }

        return capitalized.toString().trim();
    }
}
