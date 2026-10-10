package com.grocery.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Catches any error that escapes a controller and shows templates/error.html
 * instead of a crash page. The full error is printed in the IntelliJ console.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DataFileException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleDataFile(DataFileException e, Model model) {
        log.error("Data file problem", e);
        model.addAttribute("message", "We could not read or save data. Please try again.");
        return "error";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleAny(Exception e, Model model) {
        log.error("Unexpected error", e);
        model.addAttribute("message", "Something went wrong. Please go back and try again.");
        return "error";
    }
}
