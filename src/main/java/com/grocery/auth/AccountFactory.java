package com.grocery.auth;

/** Builds the right Account child from one line of users.txt (same idea as ItemFactory). */
public final class AccountFactory {

    private AccountFactory() { }

    /** Expected: TYPE|id|fullName|email|passwordHash|phone|createdDate|extra */
    public static Account fromFileLine(String line) {
        String[] p = line.split("\\|", -1);   // -1 keeps empty values at the end
        if (p.length < 6) {
            throw new IllegalArgumentException("Bad account line: " + line);
        }
        String type = p[0];
        String id = p[1];
        String name = p[2];
        String email = p[3];
        String hash = p[4];

        if (Role.CUSTOMER.name().equals(type)) {
            if (p.length >= 8) {
                return new CustomerAccount(id, name, email, hash, p[5], p[6], p[7]);
            }
            if (p.length == 7) {
                return new CustomerAccount(id, name, email, hash, p[5], "", p[6]);
            }
        }
        if (Role.ADMIN.name().equals(type)) {
            if (p.length >= 8) {
                return new AdminAccount(id, name, email, hash, p[5], p[6], p[7]);
            }
            if (p.length == 6) {
                return new AdminAccount(id, name, email, hash, "", "", p[5]);
            }
        }
        throw new IllegalArgumentException("Unknown account type in file: " + type);
    }
}
