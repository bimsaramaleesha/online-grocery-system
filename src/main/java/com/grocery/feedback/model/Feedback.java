package com.grocery.feedback.model;

import com.grocery.common.FileHandler;

import java.time.LocalDate;

/**
 * ABSTRACTION: Feedback is an abstract parent class.
 * It holds common attributes shared by both Product Reviews and Order Complaints,
 * and forces each child class to implement its own type, reference, summary details,
 * and file serialization.
 *
 * ENCAPSULATION: all fields are private and validated via setters.
 */
public abstract class Feedback {

    private String id;
    private String userId;
    private String userName;
    private String comment;
    private LocalDate createdDate;

    protected Feedback(String id, String userId, String userName, String comment, LocalDate createdDate) {
        this.id = id;
        setUserId(userId);
        setUserName(userName);
        setComment(comment);
        setCreatedDate(createdDate);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public final void setUserId(String userId) {
        String clean = FileHandler.clean(userId);
        if (clean.isBlank()) {
            throw new IllegalArgumentException("User ID is required.");
        }
        this.userId = clean;
    }

    public String getUserName() { return userName; }
    public final void setUserName(String userName) {
        String clean = FileHandler.clean(userName);
        this.userName = clean.isBlank() ? "Customer" : clean;
    }

    public String getComment() { return comment; }
    public final void setComment(String comment) {
        String clean = FileHandler.clean(comment);
        if (clean.isBlank()) {
            throw new IllegalArgumentException("Comment / description is required.");
        }
        if (clean.length() > 500) {
            throw new IllegalArgumentException("Comment must be 500 characters or fewer.");
        }
        this.comment = clean;
    }

    public LocalDate getCreatedDate() { return createdDate; }
    public final void setCreatedDate(LocalDate createdDate) {
        this.createdDate = (createdDate == null) ? LocalDate.now() : createdDate;
    }

    protected static String clean(String text) {
        return FileHandler.clean(text);
    }

    // ---------- Abstract methods implemented polymorphically by children ----------

    /** Short type code ("REVIEW" or "COMPLAINT"). */
    public abstract String getFeedbackType();

    /** Friendly label shown on the UI ("Product Review" or "Order Complaint"). */
    public abstract String getTypeLabel();

    /** The linked entity ID (itemId for Review, orderId for Complaint). */
    public abstract String getReferenceId();

    /** Polymorphic description shown in tables and cards. */
    public abstract String getSummaryDetails();

    /** Polymorphic status or rating badge text. */
    public abstract String getBadgeText();

    /** True when this feedback requires staff attention (e.g. open complaint or low rating). */
    public abstract boolean requiresAttention();

    /** Turns the object into one pipe-delimited line for its data file. */
    public abstract String toFileLine();
}
