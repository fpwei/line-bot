package org.fpwei.line.server.enums;

public enum BeautyParameter implements Parameter {
    NUMBER("n"), RECENT("r"), POST("p");

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
            case "p":
                return POST;
            default:
                throw new UnsupportedOperationException();
        }
    }

}
