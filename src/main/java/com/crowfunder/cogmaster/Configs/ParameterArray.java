package com.crowfunder.cogmaster.Configs;

import java.util.HashMap;
import java.util.Map;

public class ParameterArray {
    private final Map<String, ParameterValue> hashmap;

    public ParameterArray update(ParameterArray parameterArray) {
        this.hashmap.putAll(parameterArray.getHashMap());
        return this;
    }

    // Perform parameter derivation, return effective parameters (own parameters overwriting derived params)
    // when deriving from inputted derivedParameterArray
    // This does not modify any of the objects, it's done "on-the-fly"
    public ParameterArray derive(ParameterArray derivedParameterArray) {
        ParameterArray effectiveParameterArray = new ParameterArray();
        effectiveParameterArray.update(derivedParameterArray).update(this);
        return effectiveParameterArray;
    }

    public ParameterValue resolveParameterPath(Path path) {

        ParameterValue val = hashmap.get(path.getNextPath());

        if (val == null) {
            return null;
        }

        // If value is yet another ParameterArray, recurse into it until it's not
        if (val.isNested()) {
            return ((ParameterArray) val.getValue()).resolveParameterPath(path.rotatePath());
        }

        return val;
    }

    public boolean isEmpty() {
        return hashmap.isEmpty();
    }

    public void addParameter(String key, ParameterValue value) {
        hashmap.put(key, value);
    }

    public Map<String, ParameterValue> getHashMap() {
        return hashmap;
    }

    public ParameterArray(HashMap<String, ParameterValue> hashmap) {
        this.hashmap = hashmap;
    }

    public ParameterArray() {
        this.hashmap = new HashMap<>();
    }
}
