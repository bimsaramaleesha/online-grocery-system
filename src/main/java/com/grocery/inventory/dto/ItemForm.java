package com.grocery.inventory.dto;

import com.grocery.inventory.model.Item;
import com.grocery.inventory.model.NonPerishableItem;
import com.grocery.inventory.model.PerishableItem;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Carries both the HTML form fields (ItemForm) and the storefront product view (ProductView).
 */
public class ItemForm {

    /** Read-only view of an Item for the customer storefront (/shop). */
    public record ProductView(
            String id,
            String name,
            String category,
            String typeLabel,
            double price,
            double sellingPrice,
            boolean onSale,
            int quantity,
            boolean inStock,
            boolean lowStock,
            String details,
            String emoji,
            int colorIndex,
            double rating,
            int soldCount) {
    }

    /** Category chip on the storefront. */
    public record CategoryView(String name, String emoji, int count) { }

    @NotBlank(message = "Please choose an item type.")
    private String type;

    private String id;

    @NotBlank(message = "Name is required.")
    private String name;

    @NotBlank(message = "Category is required.")
    private String category;

    @NotNull(message = "Price is required.")
    @PositiveOrZero(message = "Price cannot be negative.")
    private Double price;

    @NotNull(message = "Quantity is required.")
    @PositiveOrZero(message = "Quantity cannot be negative.")
    private Integer quantity;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate expiryDate;

    private String brand;

    @Positive(message = "Shelf life must be at least 1 month.")
    private Integer shelfLifeMonths;

    public static ItemForm from(Item item) {
        ItemForm f = new ItemForm();
        f.setType(item.getItemType());
        f.setId(item.getId());
        f.setName(item.getName());
        f.setCategory(item.getCategory());
        f.setPrice(item.getPrice());
        f.setQuantity(item.getQuantity());
        if (item instanceof PerishableItem p) {
            f.setExpiryDate(p.getExpiryDate());
        } else if (item instanceof NonPerishableItem n) {
            f.setBrand(n.getBrand());
            f.setShelfLifeMonths(n.getShelfLifeMonths());
        }
        return f;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public Integer getShelfLifeMonths() { return shelfLifeMonths; }
    public void setShelfLifeMonths(Integer shelfLifeMonths) { this.shelfLifeMonths = shelfLifeMonths; }
}
