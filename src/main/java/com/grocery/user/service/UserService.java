package com.grocery.user.service;

import com.grocery.user.dto.PasswordForm;
import com.grocery.user.dto.UserForm;
import com.grocery.user.dto.UserSummary;
import com.grocery.user.model.Customer;
import com.grocery.user.model.User;
import com.grocery.user.model.UserFactory;
import com.grocery.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;


@Service
public class UserService {

    private static final int MIN_PASSWORD = 8;
    private static final int MAX_PASSWORD = 128;

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }


    public synchronized User register(UserForm form) {
        form.setType(Customer.TYPE);
        return create(form);
    }

    public synchronized User createUser(UserForm form) {
        return create(form);
    }

    private User create(UserForm form) {
        validateNewPassword(form.getPassword(), form.getConfirmPassword());
        User user = build(null, form, PasswordUtil.hash(form.getPassword()));
        ensureEmailFree(user.getEmail(), null);
        return repository.save(user);
    }

    public Optional<User> findById(String id) {
        return id == null ? Optional.empty() : repository.findById(id);
    }

    public User getById(String id) {
        return findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User " + id + " was not found."));
    }

    public List<User> search(String keyword, String role) {
        List<User> all = repository.findAll();
        if (keyword != null && !keyword.isBlank()) {
            String k = keyword.trim().toLowerCase();
            all = all.stream()
                    .filter(u -> u.getName().toLowerCase().contains(k)
                            || u.getEmail().contains(k)
                            || u.getId().toLowerCase().contains(k))
                    .toList();
        }
        if (role != null && !role.isBlank()) {
            all = all.stream().filter(u -> u.getRole().equalsIgnoreCase(role)).toList();
        }
        return all.stream().sorted(Comparator.comparing(User::getId)).toList();
    }

    public UserSummary summary() {
        List<User> all = repository.findAll();
        int admins = (int) all.stream().filter(User::canManageUsers).count();
        return new UserSummary(all.size(), all.size() - admins, admins);
    }

    public User authenticate(String email, String password) {
        if (email == null || email.isBlank() || password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Email and password are required.");
        }
        User user = repository.findByEmail(email).orElse(null);
        if (user == null
                || password.length() > MAX_PASSWORD
                || !PasswordUtil.verify(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password.");
        }
        return user;
    }

    public synchronized User updateProfile(String id, UserForm form) {
        return update(id, form, false);
    }


    public synchronized User adminUpdate(String id, UserForm form) {
        return update(id, form, true);
    }

    private User update(String id, UserForm form, boolean allowPasswordReset) {
        User existing = getById(id);
        form.setType(existing.getRole());

        String hash = existing.getPasswordHash();
        boolean newPassword = form.getPassword() != null && !form.getPassword().isEmpty();
        if (allowPasswordReset && newPassword) {
            validateNewPassword(form.getPassword(), form.getConfirmPassword());
            hash = PasswordUtil.hash(form.getPassword());
        }

        User updated = build(id, form, hash);
        ensureEmailFree(updated.getEmail(), id);
        return repository.save(updated);
    }

    public synchronized void changePassword(String id, PasswordForm form) {
        User user = getById(id);
        if (form.getCurrentPassword() == null
                || form.getCurrentPassword().length() > MAX_PASSWORD
                || !PasswordUtil.verify(form.getCurrentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Your current password is not correct.");
        }
        validateNewPassword(form.getNewPassword(), form.getConfirmPassword());
        user.setPasswordHash(PasswordUtil.hash(form.getNewPassword()));
        repository.save(user);
    }

    public synchronized void deleteOwnAccount(String id, String password) {
        User user = getById(id);
        if (password == null || password.isEmpty() || password.length() > MAX_PASSWORD
                || !PasswordUtil.verify(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Password is not correct, so the account was not deleted.");
        }
        deleteUser(id);
    }

    public synchronized void deleteUser(String id) {
        User target = getById(id);
        if (target.canManageUsers() && summary().admins() <= 1) {
            throw new IllegalArgumentException("You cannot delete the last administrator account.");
        }
        repository.deleteById(id);
    }


    private User build(String id, UserForm f, String passwordHash) {
        return UserFactory.create(f.getType(), id, f.getName(), f.getEmail(), passwordHash,
                f.getPhone(), f.getAddress(), f.getDepartment());
    }

    private void ensureEmailFree(String email, String ignoreId) {
        repository.findByEmail(email)
                .filter(other -> !other.getId().equals(ignoreId))
                .ifPresent(other -> {
                    throw new IllegalArgumentException("An account with this email already exists.");
                });
    }

    private void validateNewPassword(String password, String confirm) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password is required.");
        }
        if (password.length() < MIN_PASSWORD || password.length() > MAX_PASSWORD) {
            throw new IllegalArgumentException("Password must be " + MIN_PASSWORD + "-" + MAX_PASSWORD + " characters.");
        }
        if (!password.equals(confirm)) {
            throw new IllegalArgumentException("The two passwords do not match.");
        }
    }
}
