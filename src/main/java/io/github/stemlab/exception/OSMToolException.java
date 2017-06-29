package io.github.stemlab.exception;

/**
 * Created by Azamat on 6/29/2017.
 */
public class OSMToolException extends Exception {
    public OSMToolException() {
        super();
    }

    public OSMToolException(String message) {
        super(message);
    }

    public OSMToolException(String message, Throwable cause) {
        super(message, cause);
    }
}
