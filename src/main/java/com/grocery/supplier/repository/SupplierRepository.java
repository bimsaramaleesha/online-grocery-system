package com.grocery.supplier.repository;

import com.grocery.supplier.model.Supplier;

import java.util.List;
import java.util.Optional;

/**
 * SE1020 - Object Oriented Programming Project
 * Module: Supplier & Vendor Management (Member 3 - @Thilanjana01)
 *
 * Interface: SupplierRepository
 * Contract for supplier data access operations.
 * Demonstrates:
 * - Abstraction
 * - Compile-time Polymorphism (Overloaded search methods)
 */
public interface SupplierRepository {

    List<Supplier> findAll();

    Optional<Supplier> findById(String supplierId);

    void save(Supplier supplier);

    void update(Supplier supplier);

    boolean deleteById(String supplierId);

    // Overloaded search method 1: keyword
    List<Supplier> search(String keyword);

    // Overloaded search method 2: category + location/warehouse
    List<Supplier> search(String category, String locationOrWarehouse);
}
