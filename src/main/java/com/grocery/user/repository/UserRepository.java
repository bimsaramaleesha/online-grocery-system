package com.grocery.user.repository;

import com.grocery.user.model.User;

import java.util.List;
import java.util.Optional;

/**
 * ABSTRACTION: the service only knows THIS interface ("what can be done"),
 * not "how". Today it saves to a text file; tomorrow it could be a database
 * and the service would not change.
 */
public interface UserRepository {
    List<User> findAll();
    Optional<User> findById(String id);
    Optional<User> findByEmail(String email);
    User save(User user);            // adds if new (id == null), updates if it exists
    boolean deleteById(String id);
}
