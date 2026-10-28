package com.grocery.order.service;

import com.grocery.common.FileHandler;
import com.grocery.inventory.model.Item;
import com.grocery.inventory.model.ItemFactory;
import com.grocery.inventory.model.NonPerishableItem;
import com.grocery.inventory.model.PerishableItem;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service to look up grocery items by ID (ITM...).
 * Reads from data/items.txt if created by the Inventory module,
 * and seamlessly provides default grocery catalog entries for instant testing.
 */
@Service
public class ItemCatalogService {

    /** Simple catalog item projection for order & cart display */
    public static class CatalogItem {
        private final String id;
        private final String name;
        private final String category;
        private final double price;
        private final String unit;

        public CatalogItem(String id, String name, String category, double price, String unit) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.price = price;
            this.unit = unit;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getCategory() { return category; }
        public double getPrice() { return price; }
        public String getUnit() { return unit; }
    }

    private static final Map<String, CatalogItem> DEFAULT_CATALOG = new LinkedHashMap<>();

    static {
        DEFAULT_CATALOG.put("ITM001", new CatalogItem("ITM001", "Fresh Farm Milk 1L", "Dairy", 280.00, "bottle"));
        DEFAULT_CATALOG.put("ITM002", new CatalogItem("ITM002", "Whole Grain Bread 450g", "Bakery", 160.00, "loaf"));
        DEFAULT_CATALOG.put("ITM003", new CatalogItem("ITM003", "Brown Farm Eggs (Pack of 10)", "Dairy & Eggs", 420.00, "pack"));
        DEFAULT_CATALOG.put("ITM004", new CatalogItem("ITM004", "Premium Basmati Rice 5kg", "Grains", 1550.00, "bag"));
        DEFAULT_CATALOG.put("ITM005", new CatalogItem("ITM005", "Crisp Gala Apples 1kg", "Fruits", 650.00, "kg"));
        DEFAULT_CATALOG.put("ITM006", new CatalogItem("ITM006", "Ceylon Black Tea 200g", "Beverages", 350.00, "box"));
    }

    /** Returns all available items from items.txt or default catalog */
    public List<CatalogItem> getAvailableCatalog() {
        Map<String, CatalogItem> catalog = new LinkedHashMap<>();

        // 1. Try reading items.txt
        try {
            List<String> lines = FileHandler.readLines("items.txt");
            for (String line : lines) {
                if (line.isBlank() || line.startsWith("#")) {
                    continue;
                }
                String[] parts = line.split("\\|", -1);
                String first = parts[0].trim();
                if (PerishableItem.TYPE.equals(first) || NonPerishableItem.TYPE.equals(first)) {
                    try {
                        Item item = ItemFactory.fromFileLine(line);
                        if (!item.isExpired()) {
                            catalog.put(item.getId(), new CatalogItem(
                                    item.getId(),
                                    item.getName(),
                                    item.getCategory(),
                                    item.getSellingPrice(),
                                    "item"
                            ));
                        }
                    } catch (RuntimeException ignored) {
                    }
                } else if (parts.length >= 3) {
                    String id = first;
                    String name = parts[1].trim();
                    double price = 0.0;
                    try {
                        price = Double.parseDouble(parts[2].trim());
                    } catch (NumberFormatException ignored) {}

                    String cat = parts.length > 3 ? parts[3].trim() : "Groceries";
                    String unit = parts.length > 4 ? parts[4].trim() : "item";
                    catalog.put(id, new CatalogItem(id, name, cat, price, unit));
                }
            }
        } catch (Exception ignored) {
            // If items.txt is not yet configured, gracefully fall back
        }

        // If items.txt is empty, supply the default catalog
        if (catalog.isEmpty()) {
            return new ArrayList<>(DEFAULT_CATALOG.values());
        }

        return new ArrayList<>(catalog.values());
    }

    /** Finds a catalog item by item ID */
    public Optional<CatalogItem> findItemById(String itemId) {
        if (itemId == null || itemId.isBlank()) {
            return Optional.empty();
        }
        for (CatalogItem item : getAvailableCatalog()) {
            if (item.getId().equalsIgnoreCase(itemId.trim())) {
                return Optional.of(item);
            }
        }
        return Optional.empty();
    }
}
