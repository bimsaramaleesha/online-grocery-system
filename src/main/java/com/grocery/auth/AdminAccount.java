package com.grocery.auth;

/** Store staff. Sees the admin dashboard and every module of the system. */
public class AdminAccount extends Account {

    private String jobTitle;

    public AdminAccount(String id, String fullName, String email, String passwordHash,
                        String phone, String createdDate, String jobTitle) {
        super(id, fullName, email, passwordHash, phone, createdDate);
        this.jobTitle = jobTitle == null ? "" : jobTitle.trim();
    }

    public String getJobTitle() { return jobTitle; }

    @Override
    public Role getRole() { return Role.ADMIN; }

    @Override
    public String getHomePath() { return "/admin"; }

    @Override
    protected String getExtraField() { return jobTitle; }
}
