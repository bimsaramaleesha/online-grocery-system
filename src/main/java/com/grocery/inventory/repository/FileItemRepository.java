package com.grocery.inventory.repository;

import com.grocery.common.FileHandler;
import com.grocery.inventory.model.Item;
import com.grocery.inventory.model.ItemFactory;
import org.springframework.stereotype.Repository;

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

    private static final String FILE_NAME = "data/items.txt";

    private final FileHandler fileHandler;

    // Spring passes the FileHandler in automatically (constructor injection)
    public FileItemRepository(FileHandler fileHandler) {
        this.fileHandler = fileHandler;
    }

    @Override
    public synchronized List<Item> findAll() {
        List<Item> items = new ArrayList<>();
        for (String line : fileHandler.readLines(FILE_NAME)) {
            if (line.isBlank() || line.startsWith("#")) {
                continue;                       // ignore empty lines and comments
            }
            try {
                items.add(ItemFactory.fromFileLine(line));
            } catch (RuntimeException e) {      // one bad line must not break the whole page
                System.err.println("Skipping bad line in " + FILE_NAME + ": " + line);
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
        fileHandler.writeLines(FILE_NAME, lines);
    }

    /** ITM-001, ITM-002 ... (highest existing number + 1) */
    private String nextId(List<Item> items) {
        int max = 0;
        for (Item item : items) {
            try {
                max = Math.max(max, Integer.parseInt(item.getId().substring(4)));
            } catch (RuntimeException ignored) {
                // an id in another format: ignore it
            }
        }
        return String.format("ITM-%03d", max + 1);
    }
}
