package com.grocery.auth;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Business rules for accounts: register, log in, and the two default accounts. */
@Service
public class AccountService {

    private final AccountRepository repository;

    public AccountService(AccountRepository repository) {
        this.repository = repository;
    }

    /** Public sign-up ALWAYS creates a CUSTOMER. Nobody can make themselves an admin from the website. */
    public Account registerCustomer(RegisterForm form) {
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            throw new IllegalArgumentException("The two passwords do not match.");
        }
        if (repository.findByEmail(form.getEmail()).isPresent()) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }
        Account account = new CustomerAccount(null, form.getFullName(), form.getEmail(),
                PasswordHasher.hash(form.getPassword()), form.getPhone(),
                LocalDate.now().toString(), form.getAddress());
        return repository.save(account);
    }

    /** Empty result = wrong email or password (we never say which one, on purpose). */
    public Optional<Account> login(String email, String password) {
        return repository.findByEmail(email)
                .filter(a -> PasswordHasher.matches(password, a.getPasswordHash()));
    }

    public List<Account> findAll() {
        return repository.findAll();
    }

    public long countByRole(Role role) {
        return repository.findAll().stream().filter(a -> a.getRole() == role).count();
    }

    /** Used by DefaultAccountSeeder to create the first accounts. */
    Account createAdmin(String name, String email, String password, String jobTitle) {
        return repository.save(new AdminAccount(null, name, email, PasswordHasher.hash(password),
                "", LocalDate.now().toString(), jobTitle));
    }

    Account createDemoCustomer(String name, String email, String password) {
        return repository.save(new CustomerAccount(null, name, email, PasswordHasher.hash(password),
                "0771234567", LocalDate.now().toString(), "No. 1, Galle Road, Colombo"));
    }
}
