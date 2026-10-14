package com.grocery.supplier.model;

/**
 * SE1020 - Object Oriented Programming Project
 * Module: Supplier & Vendor Management (Member 3)
 *
 * Subclass: WholesaleDistributor (Inherits from Supplier)
 * Represents a commercial distributor supplying packaged goods, bulk grains, or canned foods.
 * Demonstrates:
 * - Inheritance: Extends Supplier via 'super' constructor and fields.
 * - Polymorphism: Overrides calculateLeadTimeDays, calculateRestockCost, toFileString.
 * - Encapsulation: Private warehouse fields with validation.
 */
public class WholesaleDistributor extends Supplier {

    private String warehouseCode;
    private int minOrderQuantity;
    private double bulkDiscountRate; // e.g. 0.10 for 10% discount

    // Default Constructor
    public WholesaleDistributor() {
        super();
    }

    // Parameterized Constructor
    public WholesaleDistributor(String supplierId, String companyName, String contactNumber,
                                String email, String supplyCategory, String warehouseCode,
                                int minOrderQuantity, double bulkDiscountRate) {
        super(supplierId, companyName, contactNumber, email, supplyCategory);
        setWarehouseCode(warehouseCode);
        setMinOrderQuantity(minOrderQuantity);
        setBulkDiscountRate(bulkDiscountRate);
    }

    // --- Getters and Setters (Encapsulation) ---

    public String getWarehouseCode() {
        return warehouseCode;
    }

    public void setWarehouseCode(String warehouseCode) {
        if (warehouseCode == null || warehouseCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Warehouse code cannot be empty.");
        }
        this.warehouseCode = warehouseCode.trim();
    }

    public int getMinOrderQuantity() {
        return minOrderQuantity;
    }

    public void setMinOrderQuantity(int minOrderQuantity) {
        if (minOrderQuantity <= 0) {
            throw new IllegalArgumentException("Minimum order quantity must be greater than zero.");
        }
        this.minOrderQuantity = minOrderQuantity;
    }

    public double getBulkDiscountRate() {
        return bulkDiscountRate;
    }

    public void setBulkDiscountRate(double bulkDiscountRate) {
        if (bulkDiscountRate < 0.0 || bulkDiscountRate > 0.50) {
            throw new IllegalArgumentException("Bulk discount rate must be between 0% and 50% (0.0 to 0.50).");
        }
        this.bulkDiscountRate = bulkDiscountRate;
    }

    // --- Polymorphic Overridden Methods ---

    @Override
    public String getSupplierType() {
        return "Wholesale Distributor";
    }

    /**
     * Commercial wholesale freight logistics requires standard dispatch and delivery windows.
     * Fixed average lead time of 5 business days.
     */
    @Override
    public int calculateLeadTimeDays() {
        return 5;
    }

    /**
     * Cost calculation: Applies bulk discount if the units ordered meet or exceed
     * the distributor's Minimum Order Quantity (MOQ).
     * Formula: (units * unitPrice) * (1 - bulkDiscountRate) if units >= MOQ
     */
    @Override
    public double calculateRestockCost(int units, double unitPrice) {
        if (units <= 0 || unitPrice <= 0) {
            throw new IllegalArgumentException("Units and unit price must be greater than zero.");
        }
        double baseCost = units * unitPrice;
        if (units >= this.minOrderQuantity) {
            baseCost = baseCost * (1.0 - this.bulkDiscountRate);
        }
        return baseCost;
    }

    /**
     * Serializes wholesale distributor data into a pipe-delimited format for suppliers.txt storage.
     * Format: WHOLESALE|supplierId|companyName|contactNumber|email|supplyCategory|warehouseCode|minOrderQuantity|bulkDiscountRate
     */
    @Override
    public String toFileString() {
        return String.format("WHOLESALE|%s|%s|%s|%s|%s|%s|%d|%.4f",
                getSupplierId(),
                getCompanyName(),
                getContactNumber(),
                getEmail(),
                getSupplyCategory(),
                getWarehouseCode(),
                getMinOrderQuantity(),
                getBulkDiscountRate());
    }
}
