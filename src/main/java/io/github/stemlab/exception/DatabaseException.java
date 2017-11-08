package io.github.stemlab.exception;

import java.sql.SQLException;

/**
 * Created by Azamat on 6/29/2017.
 */
public class DatabaseException extends SQLException {
    public DatabaseException() {
        super();
    }

    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
