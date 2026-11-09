package com.grocery.feedback.model;

import java.time.LocalDate;

/**
 * Factory class that creates the right Feedback subclass (Review or Complaint)
 * from form values or from a line in data/feedbacks.txt.
 */
public final class FeedbackFactory {

    private FeedbackFactory() { }

    public static Review createReview(String id, String itemId, String userId, String userName,
                                      Integer rating, String title, String comment, LocalDate createdDate) {
        if (rating == null) {
            throw new IllegalArgumentException("Rating is required.");
        }
        return new Review(id, itemId, userId, userName, rating, title, comment, createdDate);
    }

    public static Complaint createComplaint(String id, String orderId, String userId, String userName,
                                            String category, String severity, String status,
                                            String comment, String resolutionNote, LocalDate createdDate) {
        return new Complaint(id, orderId, userId, userName, category, severity, status,
                comment, resolutionNote, createdDate);
    }

    /** Expected: REVIEW|id|itemId|userId|userName|rating|title|comment|createdDate */
    public static Review reviewFromFileLine(String line) {
        String[] p = line.split("\\|", -1);
        int offset = (p.length >= 9 && Review.TYPE.equalsIgnoreCase(p[0].trim())) ? 1 : 0;
        if (p.length < 8 + offset) {
            throw new IllegalArgumentException("Invalid review line: " + line);
        }
        return new Review(
                p[offset],
                p[offset + 1],
                p[offset + 2],
                p[offset + 3],
                Integer.parseInt(p[offset + 4].trim()),
                p[offset + 5],
                p[offset + 6],
                LocalDate.parse(p[offset + 7].trim()));
    }

    /** Expected: COMPLAINT|id|orderId|userId|userName|category|severity|status|comment|resolutionNote|createdDate */
    public static Complaint complaintFromFileLine(String line) {
        String[] p = line.split("\\|", -1);
        int offset = (p.length >= 11 && Complaint.TYPE.equalsIgnoreCase(p[0].trim())) ? 1 : 0;
        if (p.length < 10 + offset) {
            throw new IllegalArgumentException("Invalid complaint line: " + line);
        }
        return new Complaint(
                p[offset],
                p[offset + 1],
                p[offset + 2],
                p[offset + 3],
                p[offset + 4],
                p[offset + 5],
                p[offset + 6],
                p[offset + 7],
                p[offset + 8],
                LocalDate.parse(p[offset + 9].trim()));
    }
}
