package com.grocery.supplier.repository;

import com.grocery.common.FileHandler;
import com.grocery.supplier.model.LocalFarmerSupplier;
import com.grocery.supplier.model.Supplier;
import com.grocery.supplier.model.WholesaleDistributor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * SE1020 - Object Oriented Programming Project
 * Module: Supplier & Vendor Management (Member 3 - @Thilanjana01)
 *
 * Class: SupplierFileRepository
 * Implements data access using the shared com.grocery.common.FileHandler helper.
 * Manages data/suppliers.txt.
 */
@Repository
public class SupplierFileRepository implements SupplierRepository {

    private static final String FILE_NAME = "suppliers.txt";

    @Override
    public List<Supplier> findAll() {
        List<String> lines = FileHandler.readLines(FILE_NAME);
        List<Supplier> suppliers = new ArrayList<>();
        for (String line : lines) {
            Supplier s = parseLine(line);
            if (s != null) {
                suppliers.add(s);
            }
        }
        return suppliers;
    }

    @Override
    public Optional<Supplier> findById(String supplierId) {
        if (supplierId == null || supplierId.isBlank()) {
            return Optional.empty();
        }
        return findAll().stream()
                .filter(s -> s.getSupplierId().equalsIgnoreCase(supplierId.trim()))
                .findFirst();
    }

    @Override
    public void save(Supplier supplier) {
        if (supplier == null) return;
        FileHandler.appendLine(FILE_NAME, supplier.toFileString());
    }

    @Override
    public void update(Supplier updatedSupplier) {
        if (updatedSupplier == null) return;
        List<Supplier> list = findAll();
        List<String> lines = new ArrayList<>();
        for (Supplier s : list) {
            if (s.getSupplierId().equalsIgnoreCase(updatedSupplier.getSupplierId())) {
                lines.add(updatedSupplier.toFileString());
            } else {
                lines.add(s.toFileString());
            }
        }
        FileHandler.writeLines(FILE_NAME, lines);
    }

    @Override
    public boolean deleteById(String supplierId) {
        if (supplierId == null || supplierId.isBlank()) return false;
        List<Supplier> list = findAll();
        boolean removed = list.removeIf(s -> s.getSupplierId().equalsIgnoreCase(supplierId.trim()));
        if (removed) {
            List<String> lines = new ArrayList<>();
            for (Supplier s : list) {
                lines.add(s.toFileString());
            }
            FileHandler.writeLines(FILE_NAME, lines);
        }
        return removed;
    }

    @Override
    public List<Supplier> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findAll();
        }
        String key = keyword.trim().toLowerCase();
        return findAll().stream()
                .filter(s -> s.getCompanyName().toLowerCase().contains(key)
                        || s.getSupplyCategory().toLowerCase().contains(key)
                        || s.getSupplierId().toLowerCase().contains(key)
                        || s.getSupplierType().toLowerCase().contains(key))
                .toList();
    }

    @Override
    public List<Supplier> search(String category, String locationOrWarehouse) {
        return findAll().stream()
                .filter(s -> {
                    boolean matchCat = (category == null || category.isBlank() || "ALL".equalsIgnoreCase(category))
                            || s.getSupplyCategory().equalsIgnoreCase(category.trim());
                    boolean matchLoc = true;
                    if (locationOrWarehouse != null && !locationOrWarehouse.isBlank()) {
                        String term = locationOrWarehouse.trim().toLowerCase();
                        if (s instanceof LocalFarmerSupplier farmer) {
                            matchLoc = farmer.getFarmLocation().toLowerCase().contains(term);
                        } else if (s instanceof WholesaleDistributor wholesale) {
                            matchLoc = wholesale.getWarehouseCode().toLowerCase().contains(term);
                        }
                    }
                    return matchCat && matchLoc;
                })
                .toList();
    }

    private Supplier parseLine(String line) {
        String[] parts = line.split("\\|");
        if (parts.length < 8) return null;
        try {
            String type = parts[0].trim();
            String id = parts[1].trim();
            String name = parts[2].trim();
            String contact = parts[3].trim();
            String email = parts[4].trim();
            String category = parts[5].trim();

            if ("LOCAL".equalsIgnoreCase(type)) {
                String location = parts[6].trim();
                boolean organic = Boolean.parseBoolean(parts[7].trim());
                double handlingFee = Double.parseDouble(parts[8].trim());
                return new LocalFarmerSupplier(id, name, contact, email, category, location, organic, handlingFee);
            } else if ("WHOLESALE".equalsIgnoreCase(type)) {
                String warehouse = parts[6].trim();
                int minQty = Integer.parseInt(parts[7].trim());
                double discountRate = Double.parseDouble(parts[8].trim());
                return new WholesaleDistributor(id, name, contact, email, category, warehouse, minQty, discountRate);
            }
        } catch (Exception e) {
            System.err.println("Error parsing supplier line: " + line);
        }
        return null;
    }
}
