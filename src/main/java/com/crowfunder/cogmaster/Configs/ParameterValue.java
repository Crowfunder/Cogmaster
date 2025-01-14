package com.crowfunder.cogmaster.Configs;


import java.util.List;

public class ParameterValue {

    private final Object value;

    public boolean isNested() {
        return value instanceof ParameterArray;
    }

    @Override
    public String toString() {

        if (value instanceof String) {
            return (String) value;
        }

        else if (value instanceof Integer) {
            return ((Integer) value).toString();
        }

        else if (value instanceof Double) {
            return ((Double) value).toString();
        }

        else if (value instanceof Float) {
            return ((Float) value).toString();
        }

        else if (value instanceof Boolean) {
            return ((Boolean) value).toString();
        }

        else if (value instanceof List) {
            return value.toString();
        }

        else if (value instanceof ParameterArray) {
            return ((ParameterArray) value).toString();
        }

        else if (value instanceof ConfigReference) {
            return ((ConfigReference) value).toString();
        }
        return "";
    }

    public Object getValue() {
        return value;
    }

    public ParameterValue(Object value) {
        this.value = value;
    }
}
