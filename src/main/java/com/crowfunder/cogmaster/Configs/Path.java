package com.crowfunder.cogmaster.Configs;

import java.util.Arrays;
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

    public String getPath() {
        if (path == null || path.isEmpty()) {
            return null;
        }
        return path;
    }

    // Get first element of the path
    public String getNextPath() {
        if (Objects.equals(path, "")) {
            return null;
        }
        return path.split(delimiter)[0];
    }

    public void setPath(String newPath) {
        this.path = newPath;
    }

    public void setPath(Path newPath) { this.path = newPath.getPath(); }
    
    public Path() {
        this.path = "";
    }

    public Path(String path) {
        this.path = path;
    }
}
