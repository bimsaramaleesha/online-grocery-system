package com.grocery.inventory.service;

import com.grocery.inventory.dto.ItemForm;
import com.grocery.inventory.model.Item;
import com.grocery.inventory.model.ItemFactory;
import com.grocery.inventory.repository.ItemRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * The "business rules" layer. The controller talks to this class; this class
 * talks to the repository. Note it depends on the ItemRepository INTERFACE.
 */
@Service
public class InventoryService {

    private final ItemRepository repository;

    public InventoryService(ItemRepository repository) {
        this.repository = repository;
    }

    /** READ (list + search by name, category or id). */
    public List<Item> search(String keyword) {
        List<Item> all = repository.findAll();
        if (keyword != null && !keyword.isBlank()) {
            String k = keyword.trim().toLowerCase();
            all = all.stream()
                    .filter(i -> i.getName().toLowerCase().contains(k)
                            || i.getCategory().toLowerCase().contains(k)
                            || i.getId().toLowerCase().contains(k))
                    .toList();
        }
        return all.stream().sorted(Comparator.comparing(Item::getId)).toList();
    }

    /** READ one. */
    public Item getById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Item " + id + " was not found."));
    }

    /** CREATE. Invalid data makes the model throw IllegalArgumentException. */
    public Item addItem(ItemForm form) {
        Item item = buildItem(null, form);
        return repository.save(item);
    }

    /** UPDATE. The type of an existing item can't change, only its details. */
    public Item updateItem(String id, ItemForm form) {
        Item existing = getById(id);
        form.setType(existing.getItemType());
        Item updated = buildItem(id, form);
        return repository.save(updated);
    }

    /** DELETE. */
    public void deleteItem(String id) {
        if (!repository.deleteById(id)) {
            throw new IllegalArgumentException("Item " + id + " was not found.");
        }
    }

    private Item buildItem(String id, ItemForm f) {
        return ItemFactory.create(f.getType(), id, f.getName(), f.getCategory(),
                f.getPrice(), f.getQuantity(),
                f.getExpiryDate(), f.getBrand(), f.getShelfLifeMonths());
    }
}
