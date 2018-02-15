package org.fpwei.line.core.common;

public class CommonRuntimeException extends RuntimeException {

    public CommonRuntimeException(Throwable cause) {
        super(cause);
    }

    public CommonRuntimeException(String message) {
        super(message);
    }
}
