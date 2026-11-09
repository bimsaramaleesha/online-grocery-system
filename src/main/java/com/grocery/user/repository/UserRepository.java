package com.grocery.user.repository;

import com.grocery.user.model.User;

import java.util.List;
import java.util.Optional;


public interface UserRepository {
    List<User> findAll();
    Optional<User> findById(String id);
    Optional<User> findByEmail(String email);
    User save(User user);
    boolean deleteById(String id);
}
