package com.grocery.common;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.ErrorResponse;
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
    public String handleAny(Exception e, Model model, HttpServletResponse response) {
        // Errors raised by Spring itself (unknown URL = 404, wrong method = 405 ...)
        // carry their own status, so a missing page is not reported as a server crash.
        if (e instanceof ErrorResponse spring) {
            int status = spring.getStatusCode().value();
            response.setStatus(status);
            model.addAttribute("message", status == 404
                    ? "That page does not exist (it may not be built yet)."
                    : "That request is not allowed.");
            return "error";
        }
        log.error("Unexpected error", e);
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        model.addAttribute("message", "Something went wrong. Please go back and try again.");
        return "error";
    }
}
