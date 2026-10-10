package com.grocery.supplier.model;

/**
 * SE1020 - Object Oriented Programming Project
 * Module: Supplier & Vendor Management (Member 3)
 *
 * Subclass: LocalFarmerSupplier (Inherits from Supplier)
 * Represents a local agricultural farmer supplying fresh produce, dairy, or greens.
 * Demonstrates:
 * - Inheritance: Extends Supplier via 'super' constructor and fields.
 * - Polymorphism: Overrides calculateLeadTimeDays, calculateRestockCost, toFileString.
 * - Encapsulation: Private farm fields with validation.
 */
public class LocalFarmerSupplier extends Supplier {

    private String farmLocation;
    private boolean organicCertified;
    private double freshHandlingFee;

    // Default Constructor
    public LocalFarmerSupplier() {
        super();
    }

    // Parameterized Constructor
    public LocalFarmerSupplier(String supplierId, String companyName, String contactNumber,
                               String email, String supplyCategory, String farmLocation,
                               boolean organicCertified, double freshHandlingFee) {
        super(supplierId, companyName, contactNumber, email, supplyCategory);
        setFarmLocation(farmLocation);
        setOrganicCertified(organicCertified);
        setFreshHandlingFee(freshHandlingFee);
    }

    // --- Getters and Setters (Encapsulation) ---

    public String getFarmLocation() {
        return farmLocation;
    }

    public void setFarmLocation(String farmLocation) {
        if (farmLocation == null || farmLocation.trim().isEmpty()) {
            throw new IllegalArgumentException("Farm location cannot be empty.");
        }
        this.farmLocation = farmLocation.trim();
    }

    public boolean isOrganicCertified() {
        return organicCertified;
    }

    public void setOrganicCertified(boolean organicCertified) {
        this.organicCertified = organicCertified;
    }

    public double getFreshHandlingFee() {
        return freshHandlingFee;
    }

    public void setFreshHandlingFee(double freshHandlingFee) {
        if (freshHandlingFee < 0) {
            throw new IllegalArgumentException("Fresh handling fee cannot be negative.");
        }
        this.freshHandlingFee = freshHandlingFee;
    }

    // --- Polymorphic Overridden Methods ---

    @Override
    public String getSupplierType() {
        return "Local Farmer";
    }

    /**
     * Local farmers deliver swiftly to preserve freshness.
     * Organic certified produce is picked and dispatched within 1 day; standard within 2 days.
     */
    @Override
    public int calculateLeadTimeDays() {
        return this.organicCertified ? 1 : 2;
    }

    /**
     * Cost calculation: Base procurement cost + specialized fresh produce packaging/cold handling fee.
     * Formula: (units * unitPrice) + freshHandlingFee
     */
    @Override
    public double calculateRestockCost(int units, double unitPrice) {
        if (units <= 0 || unitPrice <= 0) {
            throw new IllegalArgumentException("Units and unit price must be greater than zero.");
        }
        double baseCost = units * unitPrice;
        return baseCost + this.freshHandlingFee;
    }

    /**
     * Serializes local farmer data into a pipe-delimited format for suppliers.txt storage.
     * Format: LOCAL|supplierId|companyName|contactNumber|email|supplyCategory|farmLocation|organicCertified|freshHandlingFee
     */
    @Override
    public String toFileString() {
        return String.format("LOCAL|%s|%s|%s|%s|%s|%s|%b|%.2f",
                getSupplierId(),
                getCompanyName(),
                getContactNumber(),
                getEmail(),
                getSupplyCategory(),
                getFarmLocation(),
                isOrganicCertified(),
                getFreshHandlingFee());
    }
}
