package com.grocery.feedback.model;

import java.time.LocalDate;

/**
 * INHERITANCE: Complaint extends Feedback for customer order complaints.
 * Stored in data/feedbacks.txt with prefix CMP (e.g. CMP001).
 * Line format: COMPLAINT|id|orderId|userId|userName|category|severity|status|comment|resolutionNote|createdDate
 */
public class Complaint extends Feedback {

    public static final String TYPE = "COMPLAINT";
    public static final String PREFIX = "CMP";

    public static final String STATUS_OPEN = "OPEN";
    public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    public static final String STATUS_RESOLVED = "RESOLVED";

    private String orderId;
    private String category;
    private String severity;
    private String status;
    private String resolutionNote;

    public Complaint(String id, String orderId, String userId, String userName,
                     String category, String severity, String status,
                     String comment, String resolutionNote, LocalDate createdDate) {
        super(id, userId, userName, comment, createdDate);
        setOrderId(orderId);
        setCategory(category);
        setSeverity(severity);
        setStatus(status);
        setResolutionNote(resolutionNote);
    }

    public String getOrderId() { return orderId; }
    public final void setOrderId(String orderId) {
        String clean = clean(orderId);
        if (clean.isBlank()) {
            throw new IllegalArgumentException("Order ID is required for a complaint.");
        }
        this.orderId = clean;
    }

    public String getCategory() { return category; }
    public final void setCategory(String category) {
        String clean = clean(category);
        if (clean.isBlank()) {
            throw new IllegalArgumentException("Issue category is required.");
        }
        this.category = clean;
    }

    public String getSeverity() { return severity; }
    public final void setSeverity(String severity) {
        String clean = clean(severity).toUpperCase();
        if (!"LOW".equals(clean) && !"MEDIUM".equals(clean) && !"HIGH".equals(clean)) {
            clean = "MEDIUM";
        }
        this.severity = clean;
    }

    public String getStatus() { return status; }
    public final void setStatus(String status) {
        String clean = clean(status).toUpperCase();
        if (!STATUS_OPEN.equals(clean) && !STATUS_IN_PROGRESS.equals(clean) && !STATUS_RESOLVED.equals(clean)) {
            clean = STATUS_OPEN;
        }
        this.status = clean;
    }

    public String getResolutionNote() { return resolutionNote; }
    public final void setResolutionNote(String resolutionNote) {
        this.resolutionNote = clean(resolutionNote);
    }

    public boolean isResolved() {
        return STATUS_RESOLVED.equals(status);
    }

    @Override
    public String getFeedbackType() { return TYPE; }

    @Override
    public String getTypeLabel() { return "Order Complaint"; }

    @Override
    public String getReferenceId() { return orderId; }

    @Override
    public String getSummaryDetails() {
        String base = "[" + severity + "] " + category + " (" + status.replace('_', ' ') + ")";
        return resolutionNote.isEmpty() ? base : base + " — Resolution: " + resolutionNote;
    }

    @Override
    public String getBadgeText() {
        return status.replace('_', ' ');
    }

    @Override
    public boolean requiresAttention() {
        return !isResolved();
    }

    @Override
    public String toFileLine() {
        return String.join("|",
                TYPE,
                getId(),
                clean(orderId),
                clean(getUserId()),
                clean(getUserName()),
                clean(category),
                clean(severity),
                clean(status),
                clean(getComment()),
                clean(resolutionNote),
                getCreatedDate().toString());
    }
}
