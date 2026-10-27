package com.grocery.auth;

import java.util.List;
import java.util.Optional;

/** WHAT can be done with accounts (HOW is up to the implementation: today a text file). */
public interface AccountRepository {
    List<Account> findAll();
    Optional<Account> findById(String id);
    Optional<Account> findByEmail(String email);
    Account save(Account account);     // adds if id == null, updates if it exists
}
