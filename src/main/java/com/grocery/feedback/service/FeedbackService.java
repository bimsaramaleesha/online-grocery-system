package com.grocery.feedback.service;

import com.grocery.feedback.dto.FeedbackForm;
import com.grocery.feedback.model.Complaint;
import com.grocery.feedback.model.FeedbackFactory;
import com.grocery.feedback.model.Review;
import com.grocery.feedback.repository.FeedbackRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * Member 6 — Business logic for Product Reviews and Customer Complaints.
 * Depends on the FeedbackRepository interface (Abstraction).
 */
@Service
public class FeedbackService {

    public record FeedbackSummary(
            int totalReviews,
            double averageRating,
            int positiveReviews,
            int totalComplaints,
            int openComplaints,
            int resolvedComplaints
    ) { }

    private final FeedbackRepository repository;

    public FeedbackService(FeedbackRepository repository) {
        this.repository = repository;
    }

    /** Seeds demo reviews and complaints when feedbacks.txt is empty. */
    @PostConstruct
    public synchronized void seedDefaults() {
        if (repository.findAllReviews().isEmpty()) {
            repository.saveReview(new Review(null, "ITM001", "U002", "Demo Customer",
                    5, "Super fresh milk!", "Delivered cold and fresh right on time.", LocalDate.now().minusDays(2)));
            repository.saveReview(new Review(null, "ITM006", "U002", "Demo Customer",
                    4, "Great quality basmati", "Aromatic grains, very good value for a 5kg pack.", LocalDate.now().minusDays(1)));
        }
        if (repository.findAllComplaints().isEmpty()) {
            repository.saveComplaint(new Complaint(null, "ORD001", "U002", "Demo Customer",
                    "Late Delivery", "MEDIUM", Complaint.STATUS_OPEN,
                    "Courier arrived 40 minutes after the scheduled delivery slot.", "", LocalDate.now().minusDays(1)));
        }
    }

    // ---------- Reviews CRUD & Search ----------

    public List<Review> searchReviews(String keyword, Integer ratingFilter) {
        List<Review> all = repository.findAllReviews();
        if (keyword != null && !keyword.isBlank()) {
            String k = keyword.trim().toLowerCase();
            all = all.stream()
                    .filter(r -> r.getId().toLowerCase().contains(k)
                            || r.getItemId().toLowerCase().contains(k)
                            || r.getUserName().toLowerCase().contains(k)
                            || r.getTitle().toLowerCase().contains(k)
                            || r.getComment().toLowerCase().contains(k))
                    .toList();
        }
        if (ratingFilter != null && ratingFilter >= 1 && ratingFilter <= 5) {
            all = all.stream().filter(r -> r.getRating() == ratingFilter).toList();
        }
        return all.stream().sorted(Comparator.comparing(Review::getId).reversed()).toList();
    }

    public List<Review> getReviewsForItem(String itemId) {
        if (itemId == null || itemId.isBlank()) {
            return List.of();
        }
        return repository.findAllReviews().stream()
                .filter(r -> r.getItemId().equalsIgnoreCase(itemId.trim()))
                .sorted(Comparator.comparing(Review::getId).reversed())
                .toList();
    }

    public Review getReviewById(String id) {
        return repository.findReviewById(id)
                .orElseThrow(() -> new IllegalArgumentException("Review " + id + " was not found."));
    }

    public Review addReview(FeedbackForm form, String userId, String userName) {
        Review review = FeedbackFactory.createReview(
                null, form.getItemId(), userId, userName,
                form.getRating(), form.getTitle(), form.getComment(), LocalDate.now());
        return repository.saveReview(review);
    }

    public Review updateReview(String id, FeedbackForm form, String currentUserId, boolean isAdmin) {
        Review existing = getReviewById(id);
        if (!isAdmin && !existing.getUserId().equalsIgnoreCase(currentUserId)) {
            throw new IllegalArgumentException("You can only edit your own reviews.");
        }
        Review updated = FeedbackFactory.createReview(
                id,
                form.getItemId() != null && !form.getItemId().isBlank() ? form.getItemId() : existing.getItemId(),
                existing.getUserId(),
                existing.getUserName(),
                form.getRating(),
                form.getTitle(),
                form.getComment(),
                existing.getCreatedDate());
        return repository.saveReview(updated);
    }

    public void deleteReview(String id, String currentUserId, boolean isAdmin) {
        Review existing = getReviewById(id);
        if (!isAdmin && !existing.getUserId().equalsIgnoreCase(currentUserId)) {
            throw new IllegalArgumentException("You can only delete your own reviews.");
        }
        if (!repository.deleteReviewById(id)) {
            throw new IllegalArgumentException("Review " + id + " was not found.");
        }
    }

    // ---------- Complaints CRUD & Search ----------

    public List<Complaint> searchComplaints(String keyword, String statusFilter, String currentUserId, boolean isAdmin) {
        List<Complaint> all = repository.findAllComplaints();
        if (!isAdmin) {
            all = all.stream()
                    .filter(c -> c.getUserId().equalsIgnoreCase(currentUserId))
                    .toList();
        }
        if (keyword != null && !keyword.isBlank()) {
            String k = keyword.trim().toLowerCase();
            all = all.stream()
                    .filter(c -> c.getId().toLowerCase().contains(k)
                            || c.getOrderId().toLowerCase().contains(k)
                            || c.getUserName().toLowerCase().contains(k)
                            || c.getCategory().toLowerCase().contains(k)
                            || c.getComment().toLowerCase().contains(k))
                    .toList();
        }
        if (statusFilter != null && !statusFilter.isBlank()) {
            all = all.stream().filter(c -> c.getStatus().equalsIgnoreCase(statusFilter)).toList();
        }
        return all.stream().sorted(Comparator.comparing(Complaint::getId).reversed()).toList();
    }

    public Complaint getComplaintById(String id) {
        return repository.findComplaintById(id)
                .orElseThrow(() -> new IllegalArgumentException("Complaint " + id + " was not found."));
    }

    public Complaint addComplaint(FeedbackForm form, String userId, String userName) {
        Complaint complaint = FeedbackFactory.createComplaint(
                null, form.getOrderId(), userId, userName,
                form.getCategory(), form.getSeverity(), Complaint.STATUS_OPEN,
                form.getComment(), "", LocalDate.now());
        return repository.saveComplaint(complaint);
    }

    public Complaint updateComplaint(String id, FeedbackForm form, String currentUserId, boolean isAdmin) {
        Complaint existing = getComplaintById(id);
        if (!isAdmin && !existing.getUserId().equalsIgnoreCase(currentUserId)) {
            throw new IllegalArgumentException("You can only edit your own complaints.");
        }
        String newStatus = isAdmin && form.getStatus() != null && !form.getStatus().isBlank()
                ? form.getStatus()
                : existing.getStatus();
        String newResolution = isAdmin && form.getResolutionNote() != null
                ? form.getResolutionNote()
                : existing.getResolutionNote();

        Complaint updated = FeedbackFactory.createComplaint(
                id,
                form.getOrderId() != null && !form.getOrderId().isBlank() ? form.getOrderId() : existing.getOrderId(),
                existing.getUserId(),
                existing.getUserName(),
                form.getCategory(),
                form.getSeverity(),
                newStatus,
                form.getComment(),
                newResolution,
                existing.getCreatedDate());
        return repository.saveComplaint(updated);
    }

    public void deleteComplaint(String id, String currentUserId, boolean isAdmin) {
        Complaint existing = getComplaintById(id);
        if (!isAdmin && !existing.getUserId().equalsIgnoreCase(currentUserId)) {
            throw new IllegalArgumentException("You can only delete your own complaints.");
        }
        if (!repository.deleteComplaintById(id)) {
            throw new IllegalArgumentException("Complaint " + id + " was not found.");
        }
    }

    // ---------- Summary ----------

    public FeedbackSummary summary(String currentUserId, boolean isAdmin) {
        List<Review> reviews = repository.findAllReviews();
        double avg = reviews.isEmpty() ? 0.0 : reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
        int positive = (int) reviews.stream().filter(Review::isPositive).count();

        List<Complaint> complaints = repository.findAllComplaints();
        if (!isAdmin && currentUserId != null) {
            complaints = complaints.stream()
                    .filter(c -> c.getUserId().equalsIgnoreCase(currentUserId))
                    .toList();
        }
        int open = (int) complaints.stream().filter(Complaint::requiresAttention).count();
        int resolved = (int) complaints.stream().filter(Complaint::isResolved).count();

        return new FeedbackSummary(
                reviews.size(),
                Math.round(avg * 10.0) / 10.0,
                positive,
                complaints.size(),
                open,
                resolved);
    }
}
