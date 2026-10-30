package com.grocery.inventory.repository;

import com.grocery.common.FileHandler;
import com.grocery.common.IdGenerator;
import com.grocery.inventory.model.Item;
import com.grocery.inventory.model.ItemFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Stores items in data/items.txt, one item per line.
 * Seeds default grocery items automatically on first run if data/items.txt is empty.
 */
@Repository
public class FileItemRepository implements ItemRepository {

    private static final Logger log = LoggerFactory.getLogger(FileItemRepository.class);

    private final String fileName;

    public FileItemRepository(@Value("${grocery.data.items-file:items.txt}") String fileName) {
        this.fileName = fileName;
        if ("items.txt".equals(fileName) && FileHandler.readLines(fileName).isEmpty()) {
            seedDefaultItems();
        }
    }

    private void seedDefaultItems() {
        LocalDate today = LocalDate.now();
        List<String> defaults = List.of(
                "PERISHABLE|ITM001|Fresh Milk 1L|Dairy|420.0|25|" + today.plusDays(14),
                "PERISHABLE|ITM002|Yoghurt Cup|Dairy|90.0|8|" + today.plusDays(2),
                "PERISHABLE|ITM003|Carrots 1kg|Vegetables|350.0|40|" + today.plusDays(10),
                "PERISHABLE|ITM004|Sandwich Bread|Bakery|180.0|30|" + today.plusDays(3),
                "PERISHABLE|ITM005|Chicken Breast 500g|Meat|950.0|12|" + today.plusDays(7),
                "NON_PERISHABLE|ITM006|Basmati Rice 5kg|Grains|1850.0|40|Araliya|24",
                "NON_PERISHABLE|ITM007|Canned Tuna 185g|Canned Food|620.0|6|Seafresh|36",
                "NON_PERISHABLE|ITM008|Dishwash Liquid 500ml|Household|480.0|18|Sunlight|30",
                "PERISHABLE|ITM009|Fresh Strawberries 250g|Fruits|550.0|20|" + today.plusDays(2),
                "PERISHABLE|ITM010|Bananas 1kg|Fruits|320.0|45|" + today.plusDays(5),
                "PERISHABLE|ITM011|Free-Range Eggs (10 pack)|Dairy|480.0|35|" + today.plusDays(12),
                "NON_PERISHABLE|ITM012|Ceylon Black Tea 200g|Beverages|350.0|60|Ceylon Pure|24"
        );
        FileHandler.writeLines(fileName, defaults);
    }

    @Override
    public synchronized List<Item> findAll() {
        List<Item> items = new ArrayList<>();
        for (String line : FileHandler.readLines(fileName)) {
            if (line.isBlank() || line.startsWith("#")) {
                continue;
            }
            try {
                items.add(ItemFactory.fromFileLine(line));
            } catch (RuntimeException e) {
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
            item.setId(nextId(items));
            items.add(item);
        } else {
            boolean replaced = false;
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).getId().equals(item.getId())) {
                    items.set(i, item);
                    replaced = true;
                    break;
                }
            }
            if (!replaced) {
                items.add(item);
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

    private void writeAll(List<Item> items) {
        List<String> lines = new ArrayList<>();
        for (Item item : items) {
            lines.add(item.toFileLine());
        }
        FileHandler.writeLines(fileName, lines);
    }

    private String nextId(List<Item> items) {
        return IdGenerator.nextId("ITM", items.stream().map(Item::getId).toList());
    }
}
