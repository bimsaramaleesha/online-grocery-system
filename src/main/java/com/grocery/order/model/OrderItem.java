package com.grocery.order.model;

import com.grocery.common.FileHandler;

/**
 * Represents a snapshotted line item inside an Order.
 * Encodes historical unit price to preserve billing accuracy.
 */
public class OrderItem {

    private String itemId;
    private String itemName;
    private int quantity;
    private double unitPrice;

    public OrderItem() {
    }

    public OrderItem(String itemId, String itemName, int quantity, double unitPrice) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public double getLineTotal() {
        return Math.round(quantity * unitPrice * 100.0) / 100.0;
    }

    /**
     * Serializes this item into a sub-token: itemId:quantity:unitPrice:itemName
     */
    public String toToken() {
        String cleanName = FileHandler.clean(itemName).replace(":", " ").replace(",", " ");
        return String.format("%s:%d:%.2f:%s",
                FileHandler.clean(itemId),
                quantity,
                unitPrice,
                cleanName);
    }

    /**
     * Parses a sub-token: itemId:quantity:unitPrice:itemName
     */
    public static OrderItem fromToken(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        String[] parts = token.split(":", -1);
        if (parts.length < 3) {
            return null;
        }
        String iId = parts[0].trim();
        int qty = 1;
        double price = 0.0;
        try {
            qty = Integer.parseInt(parts[1].trim());
            price = Double.parseDouble(parts[2].trim());
        } catch (NumberFormatException ignored) {}

        String name = parts.length > 3 ? parts[3].trim() : iId;
        return new OrderItem(iId, name, qty, price);
    }

    // Getters and Setters
    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName != null ? itemName : itemId;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }
}
