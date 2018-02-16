package org.fpwei.line.server.enums;

public enum ImageParameter implements Parameter {
    POST("p"), PAGE("n");

    private String value;

    ImageParameter(String value) {
        this.value = value;
    }

    public static Parameter getParameter(String value) {
        switch (value) {
            case "p":
                return POST;
            case "n":
                return PAGE;
            default:
                throw new UnsupportedOperationException();
        }
    }

    public String getValue() {
        return "-" + value;
    }
}
