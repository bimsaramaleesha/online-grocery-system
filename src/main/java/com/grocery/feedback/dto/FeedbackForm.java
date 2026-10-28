package com.grocery.feedback.dto;

import com.grocery.feedback.model.Complaint;
import com.grocery.feedback.model.Feedback;
import com.grocery.feedback.model.Review;

/**
 * Plain carrier object for the HTML form (shared by Reviews and Complaints).
 */
public class FeedbackForm {

    private String type;
    private String id;
    private String comment;

    // Review fields
    private String itemId;
    private Integer rating = 5;
    private String title;

    // Complaint fields
    private String orderId;
    private String category;
    private String severity = "MEDIUM";
    private String status = Complaint.STATUS_OPEN;
    private String resolutionNote;

    public static FeedbackForm from(Feedback feedback) {
        FeedbackForm f = new FeedbackForm();
        f.setType(feedback.getFeedbackType());
        f.setId(feedback.getId());
        f.setComment(feedback.getComment());
        if (feedback instanceof Review r) {
            f.setItemId(r.getItemId());
            f.setRating(r.getRating());
            f.setTitle(r.getTitle());
        } else if (feedback instanceof Complaint c) {
            f.setOrderId(c.getOrderId());
            f.setCategory(c.getCategory());
            f.setSeverity(c.getSeverity());
            f.setStatus(c.getStatus());
            f.setResolutionNote(c.getResolutionNote());
        }
        return f;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getResolutionNote() { return resolutionNote; }
    public void setResolutionNote(String resolutionNote) { this.resolutionNote = resolutionNote; }
}
