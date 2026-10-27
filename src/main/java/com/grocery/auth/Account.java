package com.grocery.auth;

import com.grocery.common.FileHandler;

/**
 * ABSTRACTION: a login account. You never write "new Account(...)": it is either a
 * CustomerAccount or an AdminAccount (INHERITANCE).
 * ENCAPSULATION: private fields + setters that check the data.
 *
 * File line (data/users.txt):  TYPE|id|fullName|email|passwordHash|phone|createdDate|extra
 * The password is never stored as plain text, only as a salted hash (see PasswordHasher).
 */
public abstract class Account {

    private String id;
    private String fullName;
    private String email;
    private String passwordHash;
    private String phone;
    private final String createdDate;

    protected Account(String id, String fullName, String email, String passwordHash,
                      String phone, String createdDate) {
        this.id = id;
        setFullName(fullName);
        setEmail(email);
        this.passwordHash = passwordHash;
        this.phone = phone == null ? "" : phone.trim();
        this.createdDate = createdDate;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFullName() { return fullName; }
    public final void setFullName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Full name is required.");
        }
        this.fullName = fullName.trim();
    }

    public String getEmail() { return email; }
    public final void setEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("A valid email is required.");
        }
        this.email = email.trim().toLowerCase();
    }

    public String getPasswordHash() { return passwordHash; }
    public String getPhone() { return phone; }
    public String getCreatedDate() { return createdDate; }

    public boolean isAdmin() {
        return getRole() == Role.ADMIN;
    }

    /** One line for users.txt. The child class only supplies its own extra field. */
    public String toFileLine() {
        return String.join("|",
                getRole().name(),
                id,
                FileHandler.clean(fullName),
                email,
                passwordHash,
                FileHandler.clean(phone),
                createdDate,
                FileHandler.clean(getExtraField()));
    }

    // ---------- every child MUST implement these (abstraction / polymorphism) ----------

    /** Which user level this account has. */
    public abstract Role getRole();

    /** Where the user lands after login: customers go to the shop, admins to the dashboard. */
    public abstract String getHomePath();

    /** The child's own extra detail (customer: address, admin: job title). */
    protected abstract String getExtraField();
}
