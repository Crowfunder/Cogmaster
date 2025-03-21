package com.crowfunder.cogmaster.Utils;

import java.util.List;

public class ListUtil {

    // Based on foreach, effective for short arrays
    // https://stackoverflow.com/questions/17863319/java-find-intersection-of-two-arrays
    public static boolean checkOrderedSetOverlap(int[] set1, int[] set2) {
        for (int k : set1) {
            for (int m : set2) {
                // If we go out of any arrays range, they cannot overlap as they are ordered
                if (m > set1[set1.length-1] || k > set2[set2.length-1]) {
                    return false;
                }
                if (k == m) {
                    return true;
                }
            }
        }
        return false;
    }

    // Based on foreach, effective for short arrays
    // https://stackoverflow.com/questions/17863319/java-find-intersection-of-two-arrays
    public static boolean checkOrderedSetOverlap(List<Integer> set1, int[] set2) {
        for (int k : set1) {
            for (int m : set2) {
                // If we go out of any arrays range, they cannot overlap as they are ordered
                if (m > set1.get(set1.size()-1) || k > set2[set2.length-1]) {
                    return false;
                }
                if (k == m) {
                    return true;
                }
            }
        }
        return false;
    }

}
