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
 * A plain "carrier" object for the HTML form.
 * Why not bind the form straight to Item? Because Item is abstract and an HTML
 * form can't choose a child class by itself. The form fills ItemForm, then the
 * service turns it into the right Item.
 * Numbers are Double/Integer (not double/int) so an empty box is null, not a crash.
 */
public class ItemForm {

    @NotBlank(message = "Please choose an item type.")
    private String type;

    private String id;   // set by the controller on edit; not from the form

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

    // perishable only — expiry date presence is checked in ItemFactory
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate expiryDate;

    // non-perishable only
    private String brand;

    @Positive(message = "Shelf life must be at least 1 month.")
    private Integer shelfLifeMonths;

    /** Fills the edit form with the values of an existing item. */
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
