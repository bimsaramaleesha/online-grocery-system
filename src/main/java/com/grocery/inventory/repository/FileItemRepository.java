package com.grocery.inventory.repository;

import com.grocery.common.FileHandler;
import com.grocery.common.IdGenerator;
import com.grocery.inventory.model.Item;
import com.grocery.inventory.model.ItemFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Stores items in data/items.txt, one item per line.
 * Simple rule used for every operation: read the whole file -> change the list
 * in memory -> write the whole file back. Fine for a campus project.
 * "synchronized" stops two web requests writing the file at the same moment.
 */
@Repository
public class FileItemRepository implements ItemRepository {

    private static final Logger log = LoggerFactory.getLogger(FileItemRepository.class);

    private final String fileName;

    public FileItemRepository(@Value("${grocery.data.items-file:items.txt}") String fileName) {
        this.fileName = fileName;
        if ("items.txt".equals(fileName) && FileHandler.readLines(fileName).isEmpty()) {
            Path sample = Path.of("sample-data", "items.txt");
            if (Files.exists(sample)) {
                try {
                    FileHandler.writeLines(fileName, Files.readAllLines(sample, StandardCharsets.UTF_8));
                } catch (IOException ignored) { }
            }
        }
    }

    @Override
    public synchronized List<Item> findAll() {
        List<Item> items = new ArrayList<>();
        for (String line : FileHandler.readLines(fileName)) {
            if (line.isBlank() || line.startsWith("#")) {
                continue;                       // ignore empty lines and comments
            }
            try {
                items.add(ItemFactory.fromFileLine(line));
            } catch (RuntimeException e) {      // one bad line must not break the whole page
                log.warn("Skipping bad line in {}: {}", fileName, line);
            }
        }
        return items;
    }

    @Override
    public synchronized Optional<Item> findById(String id) {
        return findAll().stream()
                .filter(item -> item.getId().equals(id))
                .findFirst();
    }

    @Override
    public synchronized Item save(Item item) {
        List<Item> items = findAll();
        if (item.getId() == null || item.getId().isBlank()) {
            item.setId(nextId(items));          // CREATE
            items.add(item);
        } else {                                // UPDATE: replace the old one
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).getId().equals(item.getId())) {
                    items.set(i, item);
                    break;
                }
            }
        }
        writeAll(items);
        return item;
    }

    @Override
    public synchronized boolean deleteById(String id) {
        List<Item> items = findAll();
        boolean removed = items.removeIf(item -> item.getId().equals(id));
        if (removed) {
            writeAll(items);
        }
        return removed;
    }

    // ---------- private helpers ----------

    private void writeAll(List<Item> items) {
        List<String> lines = new ArrayList<>();
        for (Item item : items) {
            lines.add(item.toFileLine());       // polymorphism: each child writes itself
        }
        FileHandler.writeLines(fileName, lines);
    }

    /** ITM001, ITM002 ... (highest existing number + 1) - same rule as every other module. */
    private String nextId(List<Item> items) {
        return IdGenerator.nextId("ITM", items.stream().map(Item::getId).toList());
    }
}
