package org.fpwei.line.server.enums;

public enum BeautyParameter implements Parameter {
    NUMBER("n"), RECENT("r");

    private final String value;

    BeautyParameter(String value) {
        this.value = value;
    }

    public static Parameter getParameter(String value) {
        switch (value) {
            case "n":
                return NUMBER;
            case "r":
                return RECENT;
            default:
                throw new UnsupportedOperationException();
        }
    }

}
