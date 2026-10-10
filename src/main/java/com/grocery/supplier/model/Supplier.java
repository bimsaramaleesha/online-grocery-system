package com.grocery.supplier.model;

/**
 * SE1020 - Object Oriented Programming Project
 * Module: Supplier & Vendor Management (Member 3)
 *
 * Abstract Superclass: Supplier
 * Represents a generic supplier in the Online Grocery System.
 * Demonstrates:
 * - Abstraction: Cannot be directly instantiated; defines common contract.
 * - Encapsulation: Private fields with validated getters and setters.
 */
public abstract class Supplier {

    // Encapsulation: All attributes are strictly private
    private String supplierId;
    private String companyName;
    private String contactNumber;
    private String email;
    private String supplyCategory;

    // Default Constructor
    public Supplier() {
    }

    // Parameterized Constructor
    public Supplier(String supplierId, String companyName, String contactNumber, String email, String supplyCategory) {
        setSupplierId(supplierId);
        setCompanyName(companyName);
        setContactNumber(contactNumber);
        setEmail(email);
        setSupplyCategory(supplyCategory);
    }

    // --- Getters and Setters with Validation (Encapsulation) ---

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(String supplierId) {
        if (supplierId == null || supplierId.trim().isEmpty()) {
            throw new IllegalArgumentException("Supplier ID cannot be empty.");
        }
        this.supplierId = supplierId.trim();
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        if (companyName == null || companyName.trim().isEmpty()) {
            throw new IllegalArgumentException("Company name cannot be empty.");
        }
        this.companyName = companyName.trim();
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        if (contactNumber == null || !contactNumber.matches("^[0-9+ -]{9,15}$")) {
            throw new IllegalArgumentException("Invalid contact number format. Must be 9 to 15 digits.");
        }
        this.contactNumber = contactNumber.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("Invalid email format.");
        }
        this.email = email.trim();
    }

    public String getSupplyCategory() {
        return supplyCategory;
    }

    public void setSupplyCategory(String supplyCategory) {
        if (supplyCategory == null || supplyCategory.trim().isEmpty()) {
            throw new IllegalArgumentException("Supply category cannot be empty.");
        }
        this.supplyCategory = supplyCategory.trim();
    }

    // --- Abstract Methods (Polymorphism & Abstraction) ---

    /**
     * Identifies the category type of supplier ("Local Farmer" vs "Wholesale Distributor").
     */
    public abstract String getSupplierType();

    /**
     * Calculates the estimated lead time (in days) required to restock grocery supplies.
     */
    public abstract int calculateLeadTimeDays();

    /**
     * Calculates total restock procurement cost based on units, applying specific
     * business rules (e.g. handling fee for farmers, bulk discounts for wholesale).
     */
    public abstract double calculateRestockCost(int units, double unitPrice);

    /**
     * Serializes object attributes into a single delimited line for text file storage.
     */
    public abstract String toFileString();

    // --- Concrete Helper Methods ---

    public String getSupplierSummary() {
        return String.format("[%s] %s (%s) - Contact: %s | Email: %s | Est. Lead Time: %d day(s)",
                getSupplierId(), getCompanyName(), getSupplierType(), getContactNumber(), getEmail(), calculateLeadTimeDays());
    }

    @Override
    public String toString() {
        return getSupplierSummary();
    }
}
