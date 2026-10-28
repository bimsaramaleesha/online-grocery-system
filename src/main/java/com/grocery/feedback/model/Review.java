package com.grocery.feedback.model;

import java.time.LocalDate;

/**
 * INHERITANCE: Review extends Feedback for customer product reviews.
 * Stored in data/feedbacks.txt with prefix REV (e.g. REV001).
 * Line format: REVIEW|id|itemId|userId|userName|rating|title|comment|createdDate
 */
public class Review extends Feedback {

    public static final String TYPE = "REVIEW";
    public static final String PREFIX = "REV";
    private static final int LOW_RATING_LIMIT = 2;

    private String itemId;
    private int rating;
    private String title;

    public Review(String id, String itemId, String userId, String userName,
                  int rating, String title, String comment, LocalDate createdDate) {
        super(id, userId, userName, comment, createdDate);
        setItemId(itemId);
        setRating(rating);
        setTitle(title);
    }

    public String getItemId() { return itemId; }
    public final void setItemId(String itemId) {
        String clean = clean(itemId);
        if (clean.isBlank()) {
            throw new IllegalArgumentException("Please select a product to review.");
        }
        this.itemId = clean;
    }

    public int getRating() { return rating; }
    public final void setRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5 stars.");
        }
        this.rating = rating;
    }

    public String getTitle() { return title; }
    public final void setTitle(String title) {
        String clean = clean(title);
        if (clean.isBlank()) {
            throw new IllegalArgumentException("Review title is required.");
        }
        if (clean.length() > 100) {
            throw new IllegalArgumentException("Review title must be 100 characters or fewer.");
        }
        this.title = clean;
    }

    /** Returns a star string like "⭐⭐⭐⭐⭐" for display in templates. */
    public String getStars() {
        return "⭐".repeat(rating);
    }

    public boolean isPositive() {
        return rating >= 4;
    }

    @Override
    public String getFeedbackType() { return TYPE; }

    @Override
    public String getTypeLabel() { return "Product Review"; }

    @Override
    public String getReferenceId() { return itemId; }

    @Override
    public String getSummaryDetails() {
        return getStars() + " (" + rating + "/5) — " + title;
    }

    @Override
    public String getBadgeText() {
        return rating + " / 5 Stars";
    }

    @Override
    public boolean requiresAttention() {
        return rating <= LOW_RATING_LIMIT;
    }

    @Override
    public String toFileLine() {
        return String.join("|",
                TYPE,
                getId(),
                clean(itemId),
                clean(getUserId()),
                clean(getUserName()),
                String.valueOf(rating),
                clean(title),
                clean(getComment()),
                getCreatedDate().toString());
    }
}
