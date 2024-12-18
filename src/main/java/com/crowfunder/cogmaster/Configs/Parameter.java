package com.crowfunder.cogmaster.Configs;


public class Parameter implements Exportable {

    private Object value;

    public String toJSONString() {

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
