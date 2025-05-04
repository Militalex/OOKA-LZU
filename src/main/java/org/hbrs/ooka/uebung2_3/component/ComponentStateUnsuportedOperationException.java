package org.hbrs.ooka.uebung2_3.component;

public class ComponentStateUnsuportedOperationException extends RuntimeException{
    public ComponentStateUnsuportedOperationException() {
    }

    public ComponentStateUnsuportedOperationException(String message) {
        super(message);
    }

    public ComponentStateUnsuportedOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ComponentStateUnsuportedOperationException(Throwable cause) {
        super(cause);
    }

    public ComponentStateUnsuportedOperationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
