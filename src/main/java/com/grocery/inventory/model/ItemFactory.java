package com.grocery.inventory.model;

import java.time.LocalDate;

/**
 * Builds the right kind of Item. Two jobs:
 *  1. create(...)        - from the web form values
 *  2. fromFileLine(...)  - from one line of items.txt
 * Everything else in the program just receives an "Item" and never needs to
 * know which child class it really is (that is polymorphism at work).
 */
public final class ItemFactory {

    private ItemFactory() { }   // utility class: nobody creates an ItemFactory object

    public static Item create(String type, String id, String name, String category,
                              Double price, Integer quantity,
                              LocalDate expiryDate, String brand, Integer shelfLifeMonths) {
        if (price == null) {
            throw new IllegalArgumentException("Price is required.");
        }
        if (quantity == null) {
            throw new IllegalArgumentException("Quantity is required.");
        }
        if (PerishableItem.TYPE.equals(type)) {
            return new PerishableItem(id, name, category, price, quantity, expiryDate);
        }
        if (NonPerishableItem.TYPE.equals(type)) {
            if (shelfLifeMonths == null) {
                throw new IllegalArgumentException("Shelf life is required for non-perishable items.");
            }
            return new NonPerishableItem(id, name, category, price, quantity, brand, shelfLifeMonths);
        }
        throw new IllegalArgumentException("Please choose an item type.");
    }

    /** Expected: TYPE|id|name|category|price|quantity|extra... */
    public static Item fromFileLine(String line) {
        String[] p = line.split("\\|", -1);   // -1 keeps empty values at the end
        String type = p[0];
        String id = p[1];
        String name = p[2];
        String category = p[3];
        double price = Double.parseDouble(p[4]);
        int quantity = Integer.parseInt(p[5]);

        if (PerishableItem.TYPE.equals(type)) {
            return new PerishableItem(id, name, category, price, quantity, LocalDate.parse(p[6]));
        }
        if (NonPerishableItem.TYPE.equals(type)) {
            return new NonPerishableItem(id, name, category, price, quantity, p[6], Integer.parseInt(p[7]));
        }
        throw new IllegalArgumentException("Unknown item type in file: " + type);
    }
}
