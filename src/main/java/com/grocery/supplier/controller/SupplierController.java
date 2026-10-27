package com.grocery.supplier.controller;

import com.grocery.supplier.model.LocalFarmerSupplier;
import com.grocery.supplier.model.Supplier;
import com.grocery.supplier.model.WholesaleDistributor;
import com.grocery.supplier.service.SupplierService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * SE1020 - Object Oriented Programming Project
 * Module: Supplier & Vendor Management (Member 3 - @Thilanjana01)
 *
 * Controller: SupplierController
 * Mapped to /suppliers (as agreed in team README).
 */
@Controller
@RequestMapping("/suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @GetMapping
    public String listSuppliers(@RequestParam(value = "search", required = false) String search,
                                @RequestParam(value = "category", required = false) String category,
                                Model model) {
        List<Supplier> suppliers;
        if (search != null && !search.isBlank()) {
            suppliers = supplierService.searchSuppliers(search);
        } else if (category != null && !category.isBlank() && !"ALL".equalsIgnoreCase(category)) {
            suppliers = supplierService.filterSuppliers(category, null);
        } else {
            suppliers = supplierService.getAllSuppliers();
        }

        long farmerCount = suppliers.stream().filter(s -> s instanceof LocalFarmerSupplier).count();
        long wholesaleCount = suppliers.stream().filter(s -> s instanceof WholesaleDistributor).count();

        model.addAttribute("suppliers", suppliers);
        model.addAttribute("farmerCount", farmerCount);
        model.addAttribute("wholesaleCount", wholesaleCount);
        model.addAttribute("totalCount", suppliers.size());
        model.addAttribute("nextId", supplierService.generateNextId());
        model.addAttribute("searchKeyword", search != null ? search : "");
        model.addAttribute("categoryFilter", category != null ? category : "ALL");

        return "supplier/supplier-list";
    }

    @PostMapping("/add")
    public String addSupplier(@RequestParam("supplierType") String type,
                              @RequestParam("companyName") String name,
                              @RequestParam("contactNumber") String phone,
                              @RequestParam("email") String email,
                              @RequestParam("supplyCategory") String category,
                              @RequestParam(value = "farmLocation", required = false, defaultValue = "") String farmLocation,
                              @RequestParam(value = "organicCertified", required = false, defaultValue = "false") boolean organic,
                              @RequestParam(value = "freshHandlingFee", required = false, defaultValue = "0.0") double handlingFee,
                              @RequestParam(value = "warehouseCode", required = false, defaultValue = "") String warehouseCode,
                              @RequestParam(value = "minOrderQuantity", required = false, defaultValue = "50") int minQty,
                              @RequestParam(value = "bulkDiscountRate", required = false, defaultValue = "0.05") double discountRate) {

        if ("LOCAL".equalsIgnoreCase(type)) {
            supplierService.registerFarmer(name, phone, email, category, farmLocation, organic, handlingFee);
        } else {
            supplierService.registerDistributor(name, phone, email, category, warehouseCode, minQty, discountRate);
        }

        return "redirect:/suppliers";
    }

    @PostMapping("/update")
    public String updateSupplier(@RequestParam("supplierId") String id,
                                 @RequestParam("companyName") String name,
                                 @RequestParam("contactNumber") String phone,
                                 @RequestParam("email") String email) {
        supplierService.updateSupplier(id, name, phone, email);
        return "redirect:/suppliers";
    }

    @PostMapping("/delete/{id}")
    public String deleteSupplier(@PathVariable("id") String id) {
        supplierService.deleteSupplier(id);
        return "redirect:/suppliers";
    }
}
