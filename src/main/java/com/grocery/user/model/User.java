package com.grocery.user.model;

import com.grocery.common.FileHandler;

import java.util.regex.Pattern;

public abstract class User {

    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private String id;
    private String name;
    private String email;
    private String passwordHash;

    protected User(String id, String name, String email, String passwordHash) {
        this.id = id;
        setName(name);
        setEmail(email);
        setPasswordHash(passwordHash);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public final void setName(String name) {
        String clean = FileHandler.clean(name);
        if (clean.isBlank()) {
            throw new IllegalArgumentException("Name is required.");
        }
        if (clean.length() > 100) {
            throw new IllegalArgumentException("Name must be 100 characters or fewer.");
        }
        this.name = clean;
    }

    public String getEmail() { return email; }
    public final void setEmail(String email) {
        String clean = FileHandler.clean(email).toLowerCase();
        if (clean.isBlank()) {
            throw new IllegalArgumentException("Email is required.");
        }
        if (clean.length() > 254 || !EMAIL.matcher(clean).matches()) {
            throw new IllegalArgumentException("Please enter a valid email address.");
        }
        this.email = clean;
    }

    public String getPasswordHash() { return passwordHash; }
    public final void setPasswordHash(String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank() || passwordHash.contains("|")) {
            throw new IllegalArgumentException("Password hash is invalid.");
        }
        this.passwordHash = passwordHash;
    }

    protected final String baseLine() {
        return String.join("|", getRole(), id, name, email, passwordHash);
    }

    public abstract String getRole();

    public abstract String getDetails();

    public abstract boolean canManageUsers();

    public abstract String getHomePath();

    public abstract String toFileLine();
}
