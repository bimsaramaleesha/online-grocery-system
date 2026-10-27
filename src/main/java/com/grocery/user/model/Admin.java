package com.grocery.user.model;

import com.grocery.common.FileHandler;

public class Admin extends User {

    public static final String TYPE = "ADMIN";

    private String phone = "";
    private String department;

    public Admin(String id, String name, String email, String passwordHash, String department) {
        super(id, name, email, passwordHash);
        setDepartment(department);
    }

    public Admin(String id, String name, String email, String passwordHash,
                 String phone, String createdDate, String department) {
        super(id, name, email, passwordHash, createdDate);
        this.phone = phone == null ? "" : FileHandler.clean(phone);
        setDepartment(department);
    }

    public String getPhone() { return phone; }

    public String getDepartment() { return department; }
    public final void setDepartment(String department) {
        String clean = FileHandler.clean(department);
        if (clean.length() > 60) {
            throw new IllegalArgumentException("Department must be 60 characters or fewer.");
        }
        this.department = clean.isEmpty() ? "General" : clean;
    }

    @Override
    public String getRole() { return TYPE; }

    @Override
    public String getDetails() { return "Department: " + department; }

    @Override
    public boolean canManageUsers() { return true; }

    @Override
    public String getHomePath() { return "/admin"; }

    @Override
    public String toFileLine() {
        return baseLine() + "|" + phone + "|" + getCreatedDate() + "|" + department;
    }
}
