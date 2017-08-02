package io.github.stemlab.exception;

/**
 * Created by Azamat on 8/1/2017.
 */
public class GeneralException extends Exception{

    public GeneralException() {
        super();
    }

    public GeneralException(String message) {
        super(message);
    }

    public GeneralException(String message, Throwable cause) {
        super(message, cause);
    }
}