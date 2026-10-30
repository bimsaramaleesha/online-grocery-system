package com.grocery.inventory.service;

import com.grocery.inventory.dto.InventorySummary;
import com.grocery.inventory.dto.ItemForm;
import com.grocery.inventory.dto.ItemForm.CategoryView;
import com.grocery.inventory.dto.ItemForm.ProductView;
import com.grocery.inventory.model.Item;
import com.grocery.inventory.model.ItemFactory;
import com.grocery.inventory.repository.ItemRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Business logic for Grocery Inventory & Customer Storefront.
 * Depends on the ItemRepository interface (Abstraction).
 */
@Service
public class InventoryService {

    private static final Map<String, String> ICONS = Map.ofEntries(
            Map.entry("dairy", "🥛"), Map.entry("vegetables", "🥕"), Map.entry("fruits", "🍓"),
            Map.entry("bakery", "🍞"), Map.entry("meat", "🍗"), Map.entry("grains", "🌾"),
            Map.entry("canned food", "🥫"), Map.entry("household", "🧴"), Map.entry("beverages", "☕"),
            Map.entry("snacks", "🍪"), Map.entry("toys", "🧸"), Map.entry("books", "📚"),
            Map.entry("stationery", "✏️"), Map.entry("electronics", "🎧"));

    private final ItemRepository repository;

    public InventoryService(ItemRepository repository) {
        this.repository = repository;
    }

    // ---------- 1. Admin Inventory CRUD & Search ----------

    public List<Item> search(String keyword) {
        return search(keyword, null);
    }

    public List<Item> search(String keyword, String filter) {
        List<Item> all = repository.findAll();
        if (keyword != null && !keyword.isBlank()) {
            String k = keyword.trim().toLowerCase();
            all = all.stream()
                    .filter(i -> i.getName().toLowerCase().contains(k)
                            || i.getCategory().toLowerCase().contains(k)
                            || i.getId().toLowerCase().contains(k))
                    .toList();
        }
        if ("low".equals(filter)) {
            all = all.stream().filter(Item::isLowStock).toList();
        } else if ("soon".equals(filter)) {
            all = all.stream().filter(Item::isExpiringSoon).toList();
        } else if ("expired".equals(filter)) {
            all = all.stream().filter(Item::isExpired).toList();
        }
        return all.stream().sorted(Comparator.comparing(Item::getId)).toList();
    }

    public InventorySummary summary() {
        List<Item> all = repository.findAll();
        double value = 0;
        for (Item i : all) {
            value += i.getPrice() * i.getQuantity();
        }
        return new InventorySummary(
                all.size(),
                (int) all.stream().filter(Item::isLowStock).count(),
                (int) all.stream().filter(Item::isExpiringSoon).count(),
                (int) all.stream().filter(Item::isExpired).count(),
                Math.round(value * 100.0) / 100.0);
    }

    public Item getById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Item " + id + " was not found."));
    }

    public Item addItem(ItemForm form) {
        Item item = buildItem(null, form);
        return repository.save(item);
    }

    public Item updateItem(String id, ItemForm form) {
        Item existing = getById(id);
        form.setType(existing.getItemType());
        Item updated = buildItem(id, form);
        return repository.save(updated);
    }

    public void deleteItem(String id) {
        if (!repository.deleteById(id)) {
            throw new IllegalArgumentException("Item " + id + " was not found.");
        }
    }

    private Item buildItem(String id, ItemForm f) {
        return ItemFactory.create(f.getType(), id, f.getName(), f.getCategory(),
                f.getPrice(), f.getQuantity(),
                f.getExpiryDate(), f.getBrand(), f.getShelfLifeMonths());
    }

    // ---------- 2. Customer Storefront Views (/shop) ----------

    public List<ProductView> shopProducts(String q, String category, String sort) {
        List<ProductView> list = search(q).stream()
                .filter(i -> !i.isExpired())
                .filter(i -> category == null || category.isBlank() || i.getCategory().equalsIgnoreCase(category))
                .map(this::toView)
                .toList();

        Comparator<ProductView> order = switch (sort == null ? "" : sort) {
            case "price-asc" -> Comparator.comparingDouble(ProductView::sellingPrice);
            case "price-desc" -> Comparator.comparingDouble(ProductView::sellingPrice).reversed();
            case "popular" -> Comparator.comparingInt(ProductView::soldCount).reversed();
            case "name" -> Comparator.comparing(ProductView::name, String.CASE_INSENSITIVE_ORDER);
            default -> Comparator.comparing(ProductView::id).reversed();
        };
        return list.stream().sorted(order).toList();
    }

    public List<ProductView> shopDeals() {
        List<ProductView> onSale = shopProducts(null, null, null).stream().filter(ProductView::onSale).limit(6).toList();
        if (!onSale.isEmpty()) {
            return onSale;
        }
        return shopProducts(null, null, "price-asc").stream().limit(6).toList();
    }

    public Optional<ProductView> findShopProduct(String id) {
        try {
            Item item = getById(id);
            return item.isExpired() ? Optional.empty() : Optional.of(toView(item));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public List<ProductView> relatedProducts(ProductView product) {
        return shopProducts(null, product.category(), null).stream()
                .filter(p -> !p.id().equals(product.id()))
                .limit(4)
                .toList();
    }

    public List<CategoryView> shopCategories() {
        Map<String, Integer> counts = new TreeMap<>();
        for (Item i : search(null)) {
            if (!i.isExpired()) {
                counts.merge(i.getCategory(), 1, Integer::sum);
            }
        }
        return counts.entrySet().stream()
                .map(e -> new CategoryView(e.getKey(), iconFor(e.getKey()), e.getValue()))
                .toList();
    }

    private ProductView toView(Item i) {
        double selling = i.getSellingPrice();
        int hash = Math.abs(i.getId().hashCode());
        double rating = 4.5 + ((hash % 5) * 0.1);
        int soldCount = 45 + (hash % 420);
        return new ProductView(i.getId(), i.getName(), i.getCategory(), i.getTypeLabel(),
                i.getPrice(), selling, selling < i.getPrice(),
                i.getQuantity(), i.getQuantity() > 0, i.isLowStock() && i.getQuantity() > 0,
                i.getDetails(), iconFor(i.getCategory()),
                Math.abs(i.getCategory().toLowerCase().hashCode()) % 6,
                Math.round(rating * 10.0) / 10.0, soldCount);
    }

    private static String iconFor(String category) {
        return ICONS.getOrDefault(category.toLowerCase(), "🛍️");
    }
}
