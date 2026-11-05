package com.grocery.supplier;

import com.grocery.supplier.model.LocalFarmerSupplier;
import com.grocery.supplier.model.Supplier;
import com.grocery.supplier.model.WholesaleDistributor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying OOP concepts (Inheritance, Polymorphism, Encapsulation)
 * for Member 3: Supplier & Vendor Management Module.
 */
class SupplierServiceTest {

    @Test
    @DisplayName("OOP Encapsulation: Valid supplier creation and attribute access")
    void testEncapsulationAndValidation() {
        LocalFarmerSupplier farmer = new LocalFarmerSupplier(
                "SUP001", "Green Valley Farms", "0771234567",
                "info@greenvalley.lk", "Fresh Vegetables",
                "Nuwara Eliya", true, 250.0
        );

        assertEquals("SUP001", farmer.getSupplierId());
        assertEquals("Green Valley Farms", farmer.getCompanyName());
        assertEquals("0771234567", farmer.getContactNumber());
        assertEquals("info@greenvalley.lk", farmer.getEmail());
        assertTrue(farmer.isOrganicCertified());
        assertEquals(250.0, farmer.getFreshHandlingFee());

        // Test exception on empty company name
        assertThrows(IllegalArgumentException.class, () -> farmer.setCompanyName(""));
    }

    @Test
    @DisplayName("OOP Polymorphism: Lead time calculation for Farmer vs Wholesale")
    void testPolymorphicLeadTime() {
        Supplier organicFarmer = new LocalFarmerSupplier(
                "SUP001", "Eco Farms", "0771122334", "eco@farms.lk",
                "Vegetables", "Badulla", true, 200.0
        );
        Supplier nonOrganicFarmer = new LocalFarmerSupplier(
                "SUP002", "City Greens", "0772233445", "city@greens.lk",
                "Vegetables", "Colombo", false, 150.0
        );
        Supplier wholesale = new WholesaleDistributor(
                "SUP003", "Lanka Bulk Ltd", "0112345678", "sales@bulk.lk",
                "Grains", "WH-COLOMBO-01", 100, 0.10
        );

        // Organic farmer dispatches in 1 day, standard farmer in 2 days
        assertEquals(1, organicFarmer.calculateLeadTimeDays());
        assertEquals(2, nonOrganicFarmer.calculateLeadTimeDays());

        // Wholesale distributor takes standard logistics transit of 5 days
        assertEquals(5, wholesale.calculateLeadTimeDays());
    }

    @Test
    @DisplayName("OOP Polymorphism: Dynamic restock procurement cost calculation")
    void testPolymorphicRestockCost() {
        Supplier farmer = new LocalFarmerSupplier(
                "SUP001", "Eco Farms", "0771122334", "eco@farms.lk",
                "Vegetables", "Badulla", true, 250.0
        );
        Supplier wholesale = new WholesaleDistributor(
                "SUP002", "Lanka Bulk Ltd", "0112345678", "sales@bulk.lk",
                "Grains", "WH-COLOMBO-01", 100, 0.10 // 10% discount for MOQ >= 100
        );

        // Farmer formula: (units * unitPrice) + handlingFee = (100 * 50) + 250 = 5250.0
        double farmerCost = farmer.calculateRestockCost(100, 50.0);
        assertEquals(5250.0, farmerCost, 0.001);

        // Wholesale below MOQ (80 < 100): standard rate (80 * 50) = 4000.0
        double wholesaleRegular = wholesale.calculateRestockCost(80, 50.0);
        assertEquals(4000.0, wholesaleRegular, 0.001);

        // Wholesale meets MOQ (120 >= 100): 10% discount applied: (120 * 50) * 0.90 = 5400.0
        double wholesaleDiscounted = wholesale.calculateRestockCost(120, 50.0);
        assertEquals(5400.0, wholesaleDiscounted, 0.001);
    }

    @Test
    @DisplayName("Polymorphic Collection: Iterating over heterogeneous supplier list")
    void testPolymorphicList() {
        List<Supplier> supplierList = new ArrayList<>();
        supplierList.add(new LocalFarmerSupplier(
                "SUP001", "Hill Country Milk", "0773344556", "milk@hill.lk",
                "Dairy & Eggs", "Kandy", true, 180.0
        ));
        supplierList.add(new WholesaleDistributor(
                "SUP002", "National Beverages", "0113344556", "order@natbev.lk",
                "Beverages", "WH-GAMPAHA-01", 50, 0.05
        ));

        // Runtime dispatch: each object invokes its own overridden methods
        for (Supplier s : supplierList) {
            assertNotNull(s.getSupplierType());
            assertTrue(s.calculateLeadTimeDays() > 0);
            assertTrue(s.calculateRestockCost(60, 100.0) > 0);
            assertTrue(s.toFileString().contains(s.getSupplierId()));
        }
    }
}
