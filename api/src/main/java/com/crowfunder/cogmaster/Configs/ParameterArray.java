package com.crowfunder.cogmaster.Configs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.crowfunder.cogmaster.Utils.StringUtil.generateStringVariants;

public class ParameterArray {

    private final Map<String, ParameterValue> hashmap;

    public ParameterArray() {
        this.hashmap = new HashMap<>();
    }

    public ParameterArray(HashMap<String, ParameterValue> hashmap) {
        this.hashmap = hashmap;
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

    public ParameterValue resolveParameterPath(Path path) {
        ParameterValue val = hashmap.get(path.getNextPath());
        if (val == null) {
            return null;
        }

        // If value is yet another ParameterArray, recurse into it until it's not
        if (val.isNested() && path.rotatePath().getPath() != null) {
            return ((ParameterArray) val.getValue()).resolveParameterPath(path.rotatePath());
        }
        return val;
    }

    // Checks numerous variants of nextpath, i.e "Something Something", "SomethingSomething", "somethingSomething" etc.
    public ParameterValue resolveParameterPathFlex(Path path) {
        List<String> nextPaths = generateStringVariants(path.getNextPath());
        for (String nextPath : nextPaths) {
            ParameterValue val = hashmap.get(nextPath);
            if (val != null) {
                // If value is yet another ParameterArray, recurse into it until it's not
                if (val.isNested() && path.rotatePath().getPath() != null) {
                    return ((ParameterArray) val.getValue()).resolveParameterPathFlex(path.rotatePath());
                }
                return val;
            }
        }
        return null;
    }

    public ParameterValue resolveParameterPath(String strPath) {
        Path path = new Path(strPath);
        return resolveParameterPath(path);
    }

    // same as equals, but for parameter path value, null-safe
    public boolean parameterValueEquals(Path path, Object val) {
        ParameterValue param = resolveParameterPath(path);
        if (param == null) {
            return false;
        }

        return param.getValue().equals(val);
    }

    public boolean parameterValueEquals(String strPath, Object val) {
        Path path = new Path(strPath);
        return parameterValueEquals(path, val);
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
}
