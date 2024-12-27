package com.crowfunder.cogmaster.Configs;


public class ParameterValue {

    private Object value;

    public boolean isNested() {
        return value instanceof ParameterArray;
    }

    public Object getValue() {
        return value;
    }

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

        else if (value instanceof ParameterArray) {
            return ((ParameterArray) value).toJSONString();
        }

        else if (value instanceof ConfigReference) {
            return ((ConfigReference) value).toJSONString();
        }
        return "";
    }
}
