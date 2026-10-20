package com.grocery.user.dto;

import com.grocery.user.model.Admin;
import com.grocery.user.model.Customer;
import com.grocery.user.model.User;

public class UserForm {

    private String type;
    private String id;
    private String name;
    private String email;
    private String password;
    private String confirmPassword;

    // customer only
    private String phone;
    private String address;

    // admin only
    private String department;

    /** Fills the edit form with the values of an existing user (never the password). */
    public static UserForm from(User user) {
        UserForm f = new UserForm();
        f.setType(user.getRole());
        f.setId(user.getId());
        f.setName(user.getName());
        f.setEmail(user.getEmail());
        if (user instanceof Customer c) {
            f.setPhone(c.getPhone());
            f.setAddress(c.getAddress());
        } else if (user instanceof Admin a) {
            f.setDepartment(a.getDepartment());
        }
        return f;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
}
