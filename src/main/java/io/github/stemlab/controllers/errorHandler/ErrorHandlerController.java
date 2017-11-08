package io.github.stemlab.controllers.errorHandler;

import io.github.stemlab.exception.OSMToolException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.sql.SQLException;

/**
 * @brief Advice controller to wrap error messages before sending to front-end
 *
 * @author Bolat Azamat.
 */
@ControllerAdvice
public class ErrorHandlerController {

    /**
     * @param exception caught SQL exception
     * @return new response entity with code 500
     */
    @ExceptionHandler({SQLException.class})
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<String> handleGenericException(SQLException exception) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * @param exception caught custom OSMToolException
     * @return new response entity with code 500
     * @see OSMToolException
     */
    @ExceptionHandler({OSMToolException.class})
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<String> handleOwnException(OSMToolException exception) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
