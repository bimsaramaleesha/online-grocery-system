package com.grocery.user.model;

public final class UserFactory {

    private UserFactory() { }

    public static User create(String type, String id, String name, String email, String passwordHash,
                              String phone, String address, String department) {
        if (Customer.TYPE.equals(type)) {
            return new Customer(id, name, email, passwordHash, phone, address);
        }
        if (Admin.TYPE.equals(type)) {
            return new Admin(id, name, email, passwordHash, department);
        }
        throw new IllegalArgumentException("Please choose an account type.");
    }


    public static User fromFileLine(String line) {
        String[] p = line.split("\\|", -1);
        String type = p[0];

        if (Customer.TYPE.equals(type) && p.length == 7) {
            return new Customer(p[1], p[2], p[3], p[4], p[5], p[6]);
        }
        if (Admin.TYPE.equals(type) && p.length == 6) {
            return new Admin(p[1], p[2], p[3], p[4], p[5]);
        }
        throw new IllegalArgumentException("Bad user line: " + line);
    }
}
