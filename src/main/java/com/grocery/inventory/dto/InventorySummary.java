package com.grocery.inventory.dto;

/** The four numbers shown in the cards at the top of the inventory page. */
public record InventorySummary(int total, int lowStock, int expiringSoon, int expired, double stockValue) {
}
