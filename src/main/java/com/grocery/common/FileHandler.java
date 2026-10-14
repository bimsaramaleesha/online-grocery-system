package com.grocery.common;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Shared helper that every module uses to read and write its .txt data file.
 * Any I/O problem is wrapped in DataFileException, which GlobalExceptionHandler
 * turns into a friendly error page.
 */
@Component
public class FileHandler {

    /** Reads every line of a text file. Missing file = empty list. */
    public List<String> readLines(String fileName) {
        Path path = Path.of(fileName);
        try {
            if (!Files.exists(path)) {
                return List.of();
            }
            return Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new DataFileException("Could not read " + fileName, e);
        }
    }

    /** Replaces the whole file with the given lines (creates folders/file if needed). */
    public void writeLines(String fileName, List<String> lines) {
        Path path = Path.of(fileName);
        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            Files.write(path, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new DataFileException("Could not write " + fileName, e);
        }
    }
}
