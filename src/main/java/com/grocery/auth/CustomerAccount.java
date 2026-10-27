package com.grocery.auth;

/** A shopper. Can browse, use the cart, place orders and track them. Nothing else. */
public class CustomerAccount extends Account {

    private String address;

    public CustomerAccount(String id, String fullName, String email, String passwordHash,
                           String phone, String createdDate, String address) {
        super(id, fullName, email, passwordHash, phone, createdDate);
        this.address = address == null ? "" : address.trim();
    }

    public String getAddress() { return address; }

    @Override
    public Role getRole() { return Role.CUSTOMER; }

    @Override
    public String getHomePath() { return "/shop"; }

    @Override
    protected String getExtraField() { return address; }
}
