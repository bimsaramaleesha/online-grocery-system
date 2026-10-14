package com.grocery.common;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Shared helper for ALL text-file reading and writing.
 * Every module must use this class instead of writing its own file code.
 *
 * Files live in the "data" folder (created automatically).
 * Suggested line format: fields separated by "|"  e.g.  ITM001|Milk|250.00|40
 *
 * Safety built in:
 *  - creates the folder and file if they are missing
 *  - skips blank lines
 *  - synchronized, so two requests cannot write at the same moment
 *  - writeLines() writes to a temp file first, so a crash cannot leave a half-written file
 */
public final class FileHandler {

    private static final Path DATA_DIR = Paths.get("data");

    private FileHandler() {
        // utility class: no objects needed
    }

    /** Reads every non-blank line of the file. */
    public static synchronized List<String> readLines(String fileName) {
        Path file = prepare(fileName);
        List<String> result = new ArrayList<>();
        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                if (!line.isBlank()) {
                    result.add(line.trim());
                }
            }
        } catch (IOException e) {
            throw new DataFileException("Could not read " + fileName, e);
        }
        return result;
    }

    /** Adds one line to the end of the file (use for Create). */
    public static synchronized void appendLine(String fileName, String line) {
        Path file = prepare(fileName);
        try {
            Files.writeString(file, line + System.lineSeparator(), StandardCharsets.UTF_8,
                    StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new DataFileException("Could not write to " + fileName, e);
        }
    }

    /** Replaces the whole file with these lines (use for Update and Delete). */
    public static synchronized void writeLines(String fileName, List<String> lines) {
        Path file = prepare(fileName);
        Path temp = DATA_DIR.resolve(fileName + ".tmp");
        try {
            Files.write(temp, lines, StandardCharsets.UTF_8);
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new DataFileException("Could not update " + fileName, e);
        }
    }

    /** Removes the "|" character so user text cannot break the line format. */
    public static String clean(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("|", " ").replace("\r", " ").replace("\n", " ").trim();
    }

    private static Path prepare(String fileName) {
        try {
            Files.createDirectories(DATA_DIR);
            Path file = DATA_DIR.resolve(fileName);
            if (!Files.exists(file)) {
                Files.createFile(file);
            }
            return file;
        } catch (IOException e) {
            throw new DataFileException("Could not prepare " + fileName, e);
        }
    }
}
