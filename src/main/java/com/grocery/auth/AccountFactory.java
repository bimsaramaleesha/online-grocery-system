package com.grocery.auth;

/** Builds the right Account child from one line of users.txt (same idea as ItemFactory). */
public final class AccountFactory {

    private AccountFactory() { }

    /** Expected: TYPE|id|fullName|email|passwordHash|phone|createdDate|extra */
    public static Account fromFileLine(String line) {
        String[] p = line.split("\\|", -1);   // -1 keeps empty values at the end
        String type = p[0];
        String id = p[1];
        String name = p[2];
        String email = p[3];
        String hash = p[4];
        String phone = p[5];
        String created = p[6];
        String extra = p[7];

        if (Role.CUSTOMER.name().equals(type)) {
            return new CustomerAccount(id, name, email, hash, phone, created, extra);
        }
        if (Role.ADMIN.name().equals(type)) {
            return new AdminAccount(id, name, email, hash, phone, created, extra);
        }
        throw new IllegalArgumentException("Unknown account type in file: " + type);
    }
}
