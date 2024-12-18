package com.crowfunder.cogmaster.Configs;

import java.util.HashMap;

public class ParameterArray implements Exportable {
    private HashMap<String, Parameter> hashmap;

    public HashMap<String, Parameter> getHashMap() {
        return hashmap;
    }

    public String toJSONString() {
        StringBuilder out = new StringBuilder();
        for (String key : hashmap.keySet()) {
            out.append(key).append(": ").append(hashmap.get(key).toJSONString()).append("\n");

        }
        return out.toString();
    }

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

    public ParameterArray(HashMap<String, Parameter> hashmap) {
        this.hashmap = hashmap;
    }

    public ParameterArray() {
        this.hashmap = new HashMap<>();
    }
}
