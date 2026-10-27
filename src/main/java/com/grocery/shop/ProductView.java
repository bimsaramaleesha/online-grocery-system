package com.grocery.shop;

/**
 * One product as the customer sees it (a simple read-only carrier for the shop pages).
 * It is built from an inventory Item by ShopService.
 */
public record ProductView(
        String id,
        String name,
        String category,
        String typeLabel,
        double price,          // normal price
        double sellingPrice,   // price to pay today (lower when a perishable item is close to expiry)
        boolean onSale,
        int quantity,
        boolean inStock,
        boolean lowStock,
        String details,
        String emoji,
        int colorIndex) {      // 0-5, picks a card colour so every category looks different
}
