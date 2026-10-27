package com.grocery.shop;

import com.grocery.inventory.model.Item;
import com.grocery.inventory.service.InventoryService;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Shop rules. It READS the inventory (through InventoryService) and never changes it.
 *  - expired products are hidden from customers
 *  - sold-out products are shown but cannot be bought
 */
@Service
public class ShopService {

    /** A category button on the shop page. */
    public record CategoryView(String name, String emoji, int count) { }

    private static final Map<String, String> ICONS = Map.ofEntries(
            Map.entry("dairy", "🥛"), Map.entry("vegetables", "🥕"), Map.entry("fruits", "🍓"),
            Map.entry("bakery", "🍞"), Map.entry("meat", "🍗"), Map.entry("grains", "🌾"),
            Map.entry("canned food", "🥫"), Map.entry("household", "🧴"), Map.entry("beverages", "☕"),
            Map.entry("snacks", "🍪"), Map.entry("toys", "🧸"), Map.entry("books", "📚"),
            Map.entry("stationery", "✏️"), Map.entry("electronics", "🎧"));

    private final InventoryService inventory;

    public ShopService(InventoryService inventory) {
        this.inventory = inventory;
    }

    /** Products for the shop page. sort = price-asc | price-desc | popular | name (anything else = newest id first). */
    public List<ProductView> products(String q, String category, String sort) {
        List<ProductView> list = inventory.search(q).stream()
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

    /** Products on sale first (close-to-expiry discount), used for the "SuperDeals" strip. */
    public List<ProductView> deals() {
        List<ProductView> onSale = products(null, null, null).stream().filter(ProductView::onSale).limit(6).toList();
        if (!onSale.isEmpty()) {
            return onSale;
        }
        return products(null, null, "price-asc").stream().limit(6).toList();
    }

    public Optional<ProductView> find(String id) {
        try {
            Item item = inventory.getById(id);
            return item.isExpired() ? Optional.empty() : Optional.of(toView(item));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /** Other products from the same category (for the product page). */
    public List<ProductView> related(ProductView product) {
        return products(null, product.category(), null).stream()
                .filter(p -> !p.id().equals(product.id()))
                .limit(4)
                .toList();
    }

    public List<CategoryView> categories() {
        Map<String, Integer> counts = new TreeMap<>();
        for (Item i : inventory.search(null)) {
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
