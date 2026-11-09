package com.grocery.feedback.repository;

import com.grocery.feedback.model.Complaint;
import com.grocery.feedback.model.Review;

import java.util.List;
import java.util.Optional;

/**
 * ABSTRACTION: interface for reading and writing Product Reviews and Order Complaints.
 */
public interface FeedbackRepository {
    List<Review> findAllReviews();
    Optional<Review> findReviewById(String id);
    Review saveReview(Review review);
    boolean deleteReviewById(String id);

    List<Complaint> findAllComplaints();
    Optional<Complaint> findComplaintById(String id);
    Complaint saveComplaint(Complaint complaint);
    boolean deleteComplaintById(String id);
}
