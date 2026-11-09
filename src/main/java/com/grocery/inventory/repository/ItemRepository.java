package com.grocery.inventory.repository;

import com.grocery.inventory.model.Item;

import java.util.List;
import java.util.Optional;

/**
 * ABSTRACTION: the service only knows THIS interface ("what can be done"),
 * not "how". Today it saves to a text file; tomorrow it could be a database
 * and the service would not change.
 */
public interface ItemRepository {
    List<Item> findAll();
    Optional<Item> findById(String id);
    Item save(Item item);            // adds if new (id == null), updates if it exists
    boolean deleteById(String id);
}
