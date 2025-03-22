package com.crowfunder.cogmaster.Configs;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

// Generic path for all purposes
public class Path {

    private String path;
    private final String delimiter = "/";

    // Returns a new object of a rotated path (remove 1st element)
    public Path rotatePath() {
        String[] pathParts = path.split(delimiter);
        String[] newPathParts = Arrays.copyOfRange(pathParts, 1, pathParts.length);
        return new Path(String.join(delimiter, newPathParts));
    }

    public String toString() {
        return path;
    }

    public int hashCode() {
        return path.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof Path other) {
            return Objects.equals(path, other.path);
        }
        return false;
    }

    // Generates various iterations of nextpath
    @JsonIgnore
    public List<String> getNextPathFlex() {
        String nextPath = getNextPath();
        List<String> variants = new ArrayList<>();
        variants.add(nextPath);
        variants.add(nextPath.replace(" ", ""));
        variants.add(Character.toLowerCase(nextPath.charAt(0)) + nextPath.substring(1));
        variants.add(variants.get(1).substring(0, 1).toLowerCase() + variants.get(1).substring(1));
        variants.add(nextPath.toLowerCase().replace(" ", ""));
        return variants;
    }

    public String getPath() {
        if (path == null || path.isEmpty()) {
            return null;
        }
        return path;
    }

    // Get first element of the path
    @JsonIgnore
    public String getNextPath() {
        if (Objects.equals(path, "")) {
            return null;
        }
        return path.split(delimiter)[0];
    }

    public String getDelimiter() {
        return delimiter;
    }

    public void setPath(String newPath) {
        this.path = newPath;
    }

    public void setPath(Path newPath) { this.path = newPath.getPath(); }

    public Path prependedPath(String newPath) {
        return new Path(newPath + this.delimiter + this.path);
    }

    public Path prependedPath(Path newPath) {
        return prependedPath(newPath.getPath());
    }

    public Path appendedPath(String newPath) {
        return new Path(this.path + this.delimiter + newPath);
    }

    public Path appendedPath(Path newPath) {
        return appendedPath(newPath.getPath());
    }

    public Path() {
        this.path = "";
    }

    public Path(String path) {
        this.path = path;
    }
}
