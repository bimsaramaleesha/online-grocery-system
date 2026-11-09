package com.grocery.inventory;

import com.grocery.inventory.model.Item;
import com.grocery.inventory.model.ItemFactory;
import com.grocery.inventory.model.NonPerishableItem;
import com.grocery.inventory.model.PerishableItem;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Plain Java tests (no Spring needed): they check the OOP model and the file format. */
class ItemModelTest {

    @Test
    void perishableItemExpiringSoonIsSoldTwentyPercentCheaper() {
        Item milk = new PerishableItem("ITM001", "Milk", "Dairy", 100.0, 20, LocalDate.now().plusDays(2));
        assertTrue(milk.isExpiringSoon());
        assertFalse(milk.isExpired());
        assertEquals(80.0, milk.getSellingPrice(), 0.001);
    }

    @Test
    void freshPerishableItemKeepsNormalPrice() {
        Item milk = new PerishableItem("ITM001", "Milk", "Dairy", 100.0, 20, LocalDate.now().plusDays(30));
        assertEquals(100.0, milk.getSellingPrice(), 0.001);
    }

    @Test
    void yesterdayIsExpired() {
        Item milk = new PerishableItem("ITM001", "Milk", "Dairy", 100.0, 20, LocalDate.now().minusDays(1));
        assertTrue(milk.isExpired());
        assertFalse(milk.isExpiringSoon());
    }

    @Test
    void nonPerishableItemNeverExpires() {
        Item rice = new NonPerishableItem("ITM002", "Rice", "Grains", 500.0, 40, "Araliya", 24);
        assertFalse(rice.isExpired());
        assertFalse(rice.isExpiringSoon());
        assertEquals(500.0, rice.getSellingPrice(), 0.001);
    }

    @Test
    void lowStockStartsAtTenOrFewer() {
        Item a = new NonPerishableItem("ITM003", "Tuna", "Canned", 600.0, 10, "Seafresh", 36);
        Item b = new NonPerishableItem("ITM004", "Tuna", "Canned", 600.0, 11, "Seafresh", 36);
        assertTrue(a.isLowStock());
        assertFalse(b.isLowStock());
    }

    @Test
    void settersRejectBadData() {
        assertThrows(IllegalArgumentException.class,
                () -> new NonPerishableItem("ITM005", " ", "Canned", 1.0, 1, "B", 1));
        assertThrows(IllegalArgumentException.class,
                () -> new NonPerishableItem("ITM005", "Tuna", "Canned", -1.0, 1, "B", 1));
        assertThrows(IllegalArgumentException.class,
                () -> new NonPerishableItem("ITM005", "Tuna", "Canned", 1.0, -1, "B", 1));
        assertThrows(IllegalArgumentException.class,
                () -> new PerishableItem("ITM005", "Milk", "Dairy", 1.0, 1, null));
    }

    @Test
    void itemSurvivesAWriteAndReadThroughTheFileFormat() {
        Item original = new PerishableItem("ITM001", "Fresh Milk 1L", "Dairy", 420.0, 25, LocalDate.of(2030, 1, 5));
        Item copy = ItemFactory.fromFileLine(original.toFileLine());
        assertTrue(copy instanceof PerishableItem);
        assertEquals(original.toFileLine(), copy.toFileLine());

        Item rice = new NonPerishableItem("ITM002", "Rice 5kg", "Grains", 1850.0, 40, "Araliya", 24);
        Item riceCopy = ItemFactory.fromFileLine(rice.toFileLine());
        assertTrue(riceCopy instanceof NonPerishableItem);
        assertEquals(rice.toFileLine(), riceCopy.toFileLine());
    }

    @Test
    void pipeCharacterInATextFieldCannotBreakTheFile() {
        Item item = new NonPerishableItem("ITM006", "Soap | Lavender", "Household", 100.0, 5, "Brand", 12);
        Item copy = ItemFactory.fromFileLine(item.toFileLine());   // would fail if "|" broke the columns
        assertFalse(copy.getName().contains("|"));
        assertEquals("Household", copy.getCategory());
        assertEquals(5, copy.getQuantity());
    }

    @Test
    void unknownTypeInFileIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> ItemFactory.fromFileLine("GADGET|ITM001|X|Y|1|1|z"));
    }
}
