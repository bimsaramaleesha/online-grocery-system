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
                "PERISHABLE|ITM001|Fresh Milk 1L|Dairy|420.0|25|" + today.plusDays(10) + "|https://cdn.dummyjson.com/products/images/groceries/Milk/thumbnail.png",
                "PERISHABLE|ITM002|Vanilla Ice Cream Tub 500ml|Dairy|650.0|8|" + today.plusDays(2) + "|https://cdn.dummyjson.com/products/images/groceries/Ice%20Cream/thumbnail.png",
                "PERISHABLE|ITM003|Fresh Potatoes 1kg|Vegetables|320.0|40|" + today.plusDays(12) + "|https://cdn.dummyjson.com/products/images/groceries/Potatoes/thumbnail.png",
                "PERISHABLE|ITM004|Red Onions 1kg|Vegetables|380.0|30|" + today.plusDays(2) + "|https://cdn.dummyjson.com/products/images/groceries/Red%20Onions/thumbnail.png",
                "PERISHABLE|ITM005|Fresh Chicken Meat 500g|Meat|950.0|12|" + today.plusDays(6) + "|https://cdn.dummyjson.com/products/images/groceries/Chicken%20Meat/thumbnail.png",
                "NON_PERISHABLE|ITM006|Basmati Rice 5kg|Grains|1850.0|40|Araliya|24|https://cdn.dummyjson.com/products/images/groceries/Rice/thumbnail.png",
                "PERISHABLE|ITM007|Fresh Fish Steak 500g|Seafood|1250.0|6|" + today.plusDays(5) + "|https://cdn.dummyjson.com/products/images/groceries/Fish%20Steak/thumbnail.png",
                "NON_PERISHABLE|ITM008|Facial Tissue Paper Box|Household|350.0|18|SoftCare|36|https://cdn.dummyjson.com/products/images/groceries/Tissue%20Paper%20Box/thumbnail.png",
                "PERISHABLE|ITM009|Fresh Strawberries 250g|Fruits|550.0|20|" + today.plusDays(2) + "|https://cdn.dummyjson.com/products/images/groceries/Strawberry/thumbnail.png",
                "PERISHABLE|ITM010|Red Apples 1kg|Fruits|680.0|45|" + today.plusDays(8) + "|https://cdn.dummyjson.com/products/images/groceries/Apple/thumbnail.png",
                "PERISHABLE|ITM011|Farm Fresh Eggs (12 Pack)|Dairy|540.0|35|" + today.plusDays(9) + "|https://cdn.dummyjson.com/products/images/groceries/Eggs/thumbnail.png",
                "PERISHABLE|ITM012|Premium Beef Steak 500g|Meat|1650.0|15|" + today.plusDays(7) + "|https://cdn.dummyjson.com/products/images/groceries/Beef%20Steak/thumbnail.png",
                "NON_PERISHABLE|ITM013|Nescafe Classic Coffee 200g|Beverages|1450.0|60|Nescafe|24|https://cdn.dummyjson.com/products/images/groceries/Nescafe%20Coffee/thumbnail.png",
                "NON_PERISHABLE|ITM014|Pure Bee Honey Jar 350g|Pantry|980.0|30|NatureGold|24|https://cdn.dummyjson.com/products/images/groceries/Honey%20Jar/thumbnail.png",
                "NON_PERISHABLE|ITM015|Sunflower Cooking Oil 1L|Pantry|890.0|50|GoldenDrop|18|https://cdn.dummyjson.com/products/images/groceries/Cooking%20Oil/thumbnail.png",
                "PERISHABLE|ITM016|Fresh Green Cucumber 500g|Vegetables|190.0|35|" + today.plusDays(6) + "|https://cdn.dummyjson.com/products/images/groceries/Cucumber/thumbnail.png",
                "PERISHABLE|ITM017|Green Bell Pepper 500g|Vegetables|420.0|25|" + today.plusDays(7) + "|https://cdn.dummyjson.com/products/images/groceries/Green%20Bell%20Pepper/thumbnail.png",
                "PERISHABLE|ITM018|Green Chili Pepper 250g|Vegetables|240.0|9|" + today.plusDays(6) + "|https://cdn.dummyjson.com/products/images/groceries/Green%20Chili%20Pepper/thumbnail.png",
                "PERISHABLE|ITM019|Fresh Kiwi Fruit (4 Pack)|Fruits|750.0|30|" + today.plusDays(9) + "|https://cdn.dummyjson.com/products/images/groceries/Kiwi/thumbnail.png",
                "PERISHABLE|ITM020|Fresh Yellow Lemons 500g|Fruits|390.0|28|" + today.plusDays(11) + "|https://cdn.dummyjson.com/products/images/groceries/Lemon/thumbnail.png",
                "PERISHABLE|ITM021|Organic Mulberries 250g|Fruits|620.0|18|" + today.plusDays(4) + "|https://cdn.dummyjson.com/products/images/groceries/Mulberry/thumbnail.png",
                "PERISHABLE|ITM022|Fresh Orange Juice 1L|Beverages|520.0|40|" + today.plusDays(8) + "|https://cdn.dummyjson.com/products/images/groceries/Juice/thumbnail.png",
                "NON_PERISHABLE|ITM023|Assorted Soft Drinks Pack|Beverages|780.0|50|FizzPop|12|https://cdn.dummyjson.com/products/images/groceries/Soft%20Drinks/thumbnail.png",
                "NON_PERISHABLE|ITM024|Mineral Drinking Water 1.5L|Beverages|150.0|100|AquaPure|24|https://cdn.dummyjson.com/products/images/groceries/Water/thumbnail.png",
                "NON_PERISHABLE|ITM025|Whey Protein Powder 1kg|Health & Nutrition|6800.0|7|ProFit|24|https://cdn.dummyjson.com/products/images/groceries/Protein%20Powder/thumbnail.png",
                "NON_PERISHABLE|ITM026|Adult Cat Food 1.2kg|Pet Supplies|1950.0|22|WhiskerCare|18|https://cdn.dummyjson.com/products/images/groceries/Cat%20Food/thumbnail.png",
                "NON_PERISHABLE|ITM027|Premium Dog Food 2kg|Pet Supplies|2650.0|16|PawNutri|18|https://cdn.dummyjson.com/products/images/groceries/Dog%20Food/thumbnail.png"
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
