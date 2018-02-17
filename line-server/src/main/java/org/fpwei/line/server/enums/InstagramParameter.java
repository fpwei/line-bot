package org.fpwei.line.server.enums;

public enum InstagramParameter implements Parameter{
    ACCOUNT("a"), RECENT("r"), COLLECTION("c");

    private String value;

    InstagramParameter(String value) {
        this.value = value;
    }

    public static Parameter getParameter(String value) {
        switch (value) {
            case "a":
                return ACCOUNT;
            case "r":
                return RECENT;
            case "c":
                return COLLECTION;
            default:
                throw new UnsupportedOperationException();
        }
    }

    public String getValue() {
        return "-" + value;
    }
}
