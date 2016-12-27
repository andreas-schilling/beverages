package org.kiirun.beverages.domain;

public class BeverageException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public BeverageException(final String message) {
        super(message);
    }
}
