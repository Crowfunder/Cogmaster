package com.crowfunder.cogmaster.Configs;

import java.util.Arrays;

// Generic path for all purposes
public class Path {

    private final String path;
    private final String delimiter = "/";

    public Path(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    // Get first element of the path
    public String getNextPath() {
        return path.split(delimiter)[0];
    }

    // Returns a new object of a rotated path (remove 1st element)
    public Path rotatePath() {
        String[] pathParts = path.split(delimiter);
        String[] newPathParts = Arrays.copyOfRange(pathParts, 1, pathParts.length);
        return new Path(String.join(delimiter, newPathParts));
    }

    public String toString() {
        return path;
    }

}
