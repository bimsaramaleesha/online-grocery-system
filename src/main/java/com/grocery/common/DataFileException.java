package com.grocery.common;

/**
 * Thrown when a data (.txt) file cannot be read or written.
 * It is a RuntimeException, so you do not need try/catch everywhere.
 * GlobalExceptionHandler shows a friendly error page for it.
 */
public class DataFileException extends RuntimeException {

    public DataFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
