package com.grocery.user.model;

import com.grocery.common.FileHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Customer extends User {

    public static final String TYPE = "CUSTOMER";

    private static final Pattern PHONE = Pattern.compile("^\\+?[0-9][0-9 \\-]{5,18}[0-9]$");

    private String phone;
    private String address;

    public Customer(String id, String name, String email, String passwordHash,
                    String phone, String address) {
        super(id, name, email, passwordHash);
        setPhone(phone);
        setAddress(address);
    }

    public String getPhone() { return phone; }
    public final void setPhone(String phone) {
        String clean = FileHandler.clean(phone);
        if (!clean.isEmpty() && !PHONE.matcher(clean).matches()) {
            throw new IllegalArgumentException("Phone number is not valid (digits, spaces, - and a leading + only).");
        }
        this.phone = clean;
    }

    public String getAddress() { return address; }
    public final void setAddress(String address) {
        String clean = FileHandler.clean(address);
        if (clean.length() > 200) {
            throw new IllegalArgumentException("Address must be 200 characters or fewer.");
        }
        this.address = clean;
    }

    @Override
    public String getRole() { return TYPE; }

    @Override
    public String getDetails() {
        List<String> parts = new ArrayList<>();
        if (!phone.isEmpty()) {
            parts.add(phone);
        }
        if (!address.isEmpty()) {
            parts.add(address);
        }
        return parts.isEmpty() ? "-" : String.join(", ", parts);
    }

    @Override
    public boolean canManageUsers() { return false; }

    @Override
    public String getHomePath() { return "/users/profile"; }

    @Override
    public String toFileLine() {
        return baseLine() + "|" + phone + "|" + address;
    }
}
