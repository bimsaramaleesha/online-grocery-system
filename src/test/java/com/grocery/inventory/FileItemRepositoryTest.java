package com.grocery.inventory;

import com.grocery.common.FileHandler;
import com.grocery.inventory.dto.ItemForm;
import com.grocery.inventory.model.Item;
import com.grocery.inventory.repository.FileItemRepository;
import com.grocery.inventory.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Uses a temporary file, so the real data/items.txt is never touched. */
class FileItemRepositoryTest {

    private InventoryService service;
    private Path file;

    @BeforeEach
    void setUp() throws IOException {
        file = Files.createTempFile("items-test", ".txt");
        Files.delete(file);   // start with "no file yet", like a fresh install
        service = new InventoryService(new FileItemRepository(new FileHandler(), file.toString()));
    }

    private ItemForm rice(String name) {
        ItemForm f = new ItemForm();
        f.setType("NON_PERISHABLE");
        f.setName(name);
        f.setCategory("Grains");
        f.setPrice(500.0);
        f.setQuantity(40);
        f.setBrand("Araliya");
        f.setShelfLifeMonths(24);
        return f;
    }

    private ItemForm milk(String name, LocalDate expiry, int qty) {
        ItemForm f = new ItemForm();
        f.setType("PERISHABLE");
        f.setName(name);
        f.setCategory("Dairy");
        f.setPrice(100.0);
        f.setQuantity(qty);
        f.setExpiryDate(expiry);
        return f;
    }

    @Test
    void missingFileMeansEmptyInventory() {
        assertTrue(service.search(null).isEmpty());
    }

    @Test
    void idsFollowTheSharedPrefixRule() {
        assertEquals("ITM001", service.addItem(rice("Rice A")).getId());
        assertEquals("ITM002", service.addItem(rice("Rice B")).getId());
    }

    @Test
    void addUpdateDeleteWorkAndArePersisted() {
        Item added = service.addItem(rice("Rice A"));

        ItemForm edit = rice("Rice A Premium");
        edit.setQuantity(5);
        service.updateItem(added.getId(), edit);

        // a brand-new service reading the same file proves the data was really saved
        InventoryService reopened = new InventoryService(new FileItemRepository(new FileHandler(), file.toString()));
        Item saved = reopened.getById(added.getId());
        assertEquals("Rice A Premium", saved.getName());
        assertEquals(5, saved.getQuantity());

        service.deleteItem(added.getId());
        assertTrue(service.search(null).isEmpty());
    }

    @Test
    void searchMatchesNameCategoryAndId() {
        service.addItem(rice("Basmati Rice"));
        service.addItem(milk("Fresh Milk", LocalDate.now().plusDays(10), 20));
        assertEquals(1, service.search("basmati").size());
        assertEquals(1, service.search("dairy").size());
        assertEquals(1, service.search("itm002").size());
        assertEquals(2, service.search("  ").size());
    }

    @Test
    void filtersAndSummaryCountTheRightItems() {
        service.addItem(rice("Rice"));                                              // normal
        service.addItem(milk("Soon Milk", LocalDate.now().plusDays(1), 50));        // expiring soon
        service.addItem(milk("Old Milk", LocalDate.now().minusDays(2), 5));         // expired + low stock
        assertEquals(1, service.search(null, "soon").size());
        assertEquals(1, service.search(null, "expired").size());
        assertEquals(1, service.search(null, "low").size());
        assertEquals(3, service.search(null, "nonsense").size());
        assertEquals(3, service.summary().total());
        assertEquals(1, service.summary().expired());
    }

    @Test
    void invalidFormDataIsRejectedAndNothingIsSaved() {
        ItemForm bad = rice("Rice");
        bad.setPrice(null);
        assertThrows(IllegalArgumentException.class, () -> service.addItem(bad));
        assertTrue(service.search(null).isEmpty());
    }

    @Test
    void oneCorruptLineDoesNotBreakTheList() throws IOException {
        Files.write(file, List.of(
                "PERISHABLE|ITM001|Milk|Dairy|100.0|20|2030-01-01",
                "this line is garbage",
                "NON_PERISHABLE|ITM002|Rice|Grains|500.0|40|Araliya|24"));
        assertEquals(2, service.search(null).size());
    }

    @Test
    void deletingUnknownIdGivesAClearError() {
        assertThrows(IllegalArgumentException.class, () -> service.deleteItem("ITM999"));
    }
}
