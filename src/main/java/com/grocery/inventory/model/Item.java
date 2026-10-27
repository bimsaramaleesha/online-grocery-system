package com.grocery.inventory.model;

import com.grocery.common.FileHandler;

/**
 * ABSTRACTION: Item is an abstract class. You can never write "new Item(...)".
 * It holds what EVERY grocery item has, and forces each child class to say
 * how it is different (the abstract methods at the bottom).
 *
 * ENCAPSULATION: every field is private. The outside world can only use the
 * getters/setters, and the setters check the data before accepting it.
 */
public abstract class Item {

    private static final int LOW_STOCK_LIMIT = 10;

    private String id;
    private String name;
    private String category;
    private double price;
    private int quantity;

    protected Item(String id, String name, String category, double price, int quantity) {
        this.id = id;
        setName(name);
        setCategory(category);
        setPrice(price);
        setQuantity(quantity);
    }

    // ---------- getters / setters (with validation) ----------

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public final void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Item name is required.");
        }
        this.name = name.trim();
    }

    public String getCategory() { return category; }
    public final void setCategory(String category) {
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("Category is required.");
        }
        this.category = category.trim();
    }

    public double getPrice() { return price; }
    public final void setPrice(double price) {
        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative.");
        }
        this.price = price;
    }

    public int getQuantity() { return quantity; }
    public final void setQuantity(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative.");
        }
        this.quantity = quantity;
    }

    // ---------- behaviour shared by all items ----------

    /** INFORMATION HIDING: the limit is private; others only ask the question. */
    public boolean isLowStock() {
        return quantity <= LOW_STOCK_LIMIT;
    }

    /** Only perishable items can expire; the others keep these defaults. */
    public boolean isExpired() { return false; }

    public boolean isExpiringSoon() { return false; }

    /**
     * Turns the item into ONE line of text for items.txt.
     * Format: TYPE|id|name|category|price|quantity|<extra fields of the child>
     * (This is the "template method" idea: the base class builds the line,
     * the child class only supplies its own extra fields.)
     */
    public String toFileLine() {
        return String.join("|",
                getItemType(),
                id,
                clean(name),
                clean(category),
                String.valueOf(price),
                String.valueOf(quantity),
                getExtraFields());
    }

    /** The "|" character is our separator, so it must never appear inside data (shared team rule). */
    protected static String clean(String text) {
        return FileHandler.clean(text);
    }

    // ---------- every child MUST implement these (abstraction) ----------

    /** Short code written to the file, e.g. PERISHABLE. */
    public abstract String getItemType();

    /** Friendly text shown on the web page, e.g. "Perishable". */
    public abstract String getTypeLabel();

    /** POLYMORPHISM: each child calculates the price customers pay in its own way. */
    public abstract double getSellingPrice();

    /** POLYMORPHISM: each child describes itself differently on the list page. */
    public abstract String getDetails();

    /** The child's own fields, already joined with "|" (used by toFileLine). */
    protected abstract String getExtraFields();
}
