package com.grocery.supplier.service;

import com.grocery.common.FileHandler;
import com.grocery.common.IdGenerator;
import com.grocery.supplier.model.LocalFarmerSupplier;
import com.grocery.supplier.model.Supplier;
import com.grocery.supplier.model.WholesaleDistributor;
import com.grocery.supplier.repository.SupplierRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * SE1020 - Object Oriented Programming Project
 * Module: Supplier & Vendor Management (Member 3 - @Thilanjana01)
 *
 * Service: SupplierService
 * Encapsulates business logic, ID generation, and validation.
 */
@Service
public class SupplierService {

    private final SupplierRepository repository;

    public SupplierService(SupplierRepository repository) {
        this.repository = repository;
    }

    public List<Supplier> getAllSuppliers() {
        return repository.findAll();
    }

    public Optional<Supplier> getSupplierById(String supplierId) {
        return repository.findById(supplierId);
    }

    public List<Supplier> searchSuppliers(String keyword) {
        return repository.search(keyword);
    }

    public List<Supplier> filterSuppliers(String category, String location) {
        return repository.search(category, location);
    }

    public String generateNextId() {
        List<String> existingIds = repository.findAll().stream()
                .map(Supplier::getSupplierId)
                .toList();
        return IdGenerator.nextId("SUP", existingIds);
    }

    public void registerFarmer(String companyName, String contactNumber, String email,
                               String category, String farmLocation, boolean organic, double handlingFee) {
        String nextId = generateNextId();
        Supplier farmer = new LocalFarmerSupplier(
                nextId,
                FileHandler.clean(companyName),
                FileHandler.clean(contactNumber),
                FileHandler.clean(email),
                FileHandler.clean(category),
                FileHandler.clean(farmLocation),
                organic,
                handlingFee
        );
        repository.save(farmer);
    }

    public void registerDistributor(String companyName, String contactNumber, String email,
                                   String category, String warehouseCode, int minOrderQty, double discountRate) {
        String nextId = generateNextId();
        Supplier wholesale = new WholesaleDistributor(
                nextId,
                FileHandler.clean(companyName),
                FileHandler.clean(contactNumber),
                FileHandler.clean(email),
                FileHandler.clean(category),
                FileHandler.clean(warehouseCode),
                minOrderQty,
                discountRate
        );
        repository.save(wholesale);
    }

    public boolean updateSupplier(String id, String companyName, String contactNumber, String email) {
        Optional<Supplier> opt = repository.findById(id);
        if (opt.isEmpty()) return false;

        Supplier s = opt.get();
        s.setCompanyName(FileHandler.clean(companyName));
        s.setContactNumber(FileHandler.clean(contactNumber));
        s.setEmail(FileHandler.clean(email));
        repository.update(s);
        return true;
    }

    public boolean deleteSupplier(String id) {
        return repository.deleteById(id);
    }

    public double calculateProcurement(String supplierId, int units, double unitPrice) {
        return repository.findById(supplierId)
                .map(s -> s.calculateRestockCost(units, unitPrice))
                .orElse(0.0);
    }
}
