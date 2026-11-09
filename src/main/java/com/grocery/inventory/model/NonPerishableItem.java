package com.grocery.inventory.model;

/**
 * INHERITANCE: second child of Item.
 * Covers both shelf-stable groceries (rice, canned food, coffee) and
 * items that have no expiry date at all (electronics, household appliances, books, etc.).
 */
public class NonPerishableItem extends Item {

    public static final String TYPE = "NON_PERISHABLE";

    private String brand;
    private int shelfLifeMonths;

    public NonPerishableItem(String id, String name, String category,
                             double price, int quantity, String brand) {
        this(id, name, category, price, quantity, brand, 0);
    }

    public NonPerishableItem(String id, String name, String category,
                             double price, int quantity,
                             String brand, int shelfLifeMonths) {
        super(id, name, category, price, quantity);
        setBrand(brand);
        setShelfLifeMonths(shelfLifeMonths);
    }

    public String getBrand() { return brand; }
    public final void setBrand(String brand) {
        if (brand == null || brand.isBlank()) {
            throw new IllegalArgumentException("Brand is required for non-perishable items.");
        }
        this.brand = brand.trim();
    }

    public int getShelfLifeMonths() { return shelfLifeMonths; }
    public final void setShelfLifeMonths(int shelfLifeMonths) {
        if (shelfLifeMonths < 0) {
            throw new IllegalArgumentException("Shelf life cannot be negative.");
        }
        this.shelfLifeMonths = shelfLifeMonths;
    }

    @Override
    public String getItemType() { return TYPE; }

    @Override
    public String getTypeLabel() { return "Non-Perishable"; }

    /** Same method name as PerishableItem, different rule: always the normal price. */
    @Override
    public double getSellingPrice() { return getPrice(); }

    @Override
    public String getDetails() {
        if (shelfLifeMonths <= 0) {
            return "Brand: " + brand + " (No expiry)";
        }
        return "Brand: " + brand + ", shelf life: " + shelfLifeMonths + " months";
    }

    @Override
    protected String getExtraFields() {
        return clean(brand) + "|" + shelfLifeMonths;
    }
}
