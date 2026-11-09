package com.grocery.feedback.repository;

import com.grocery.common.FileHandler;
import com.grocery.common.IdGenerator;
import com.grocery.feedback.model.Complaint;
import com.grocery.feedback.model.FeedbackFactory;
import com.grocery.feedback.model.Review;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Member 6 — Stores both Product Reviews and Customer Complaints in data/feedbacks.txt via FileHandler.
 */
@Repository
public class FileFeedbackRepository implements FeedbackRepository {

    private static final Logger log = LoggerFactory.getLogger(FileFeedbackRepository.class);

    private final String feedbacksFile;

    @Autowired
    public FileFeedbackRepository(
            @Value("${grocery.data.feedbacks-file:feedbacks.txt}") String feedbacksFile) {
        this.feedbacksFile = feedbacksFile;
    }

    public FileFeedbackRepository(String reviewsFile, String complaintsFile) {
        this.feedbacksFile = reviewsFile;
    }

    // ---------- Reviews ----------

    @Override
    public synchronized List<Review> findAllReviews() {
        List<Review> list = new ArrayList<>();
        for (String line : FileHandler.readLines(feedbacksFile)) {
            if (line.isBlank() || line.startsWith("#")) {
                continue;
            }
            if (line.startsWith(Review.TYPE + "|") || line.startsWith(Review.PREFIX)) {
                try {
                    list.add(FeedbackFactory.reviewFromFileLine(line));
                } catch (RuntimeException e) {
                    log.warn("Skipping bad review line in {}: {}", feedbacksFile, line);
                }
            }
        }
        return list;
    }

    @Override
    public synchronized Optional<Review> findReviewById(String id) {
        return findAllReviews().stream()
                .filter(r -> r.getId().equals(id))
                .findFirst();
    }

    @Override
    public synchronized Review saveReview(Review review) {
        List<Review> reviews = findAllReviews();
        List<Complaint> complaints = findAllComplaints();
        if (review.getId() == null || review.getId().isBlank()) {
            review.setId(IdGenerator.nextId(Review.PREFIX, reviews.stream().map(Review::getId).toList()));
            reviews.add(review);
        } else {
            boolean replaced = false;
            for (int i = 0; i < reviews.size(); i++) {
                if (reviews.get(i).getId().equals(review.getId())) {
                    reviews.set(i, review);
                    replaced = true;
                    break;
                }
            }
            if (!replaced) {
                reviews.add(review);
            }
        }
        writeAll(reviews, complaints);
        return review;
    }

    @Override
    public synchronized boolean deleteReviewById(String id) {
        List<Review> reviews = findAllReviews();
        List<Complaint> complaints = findAllComplaints();
        boolean removed = reviews.removeIf(r -> r.getId().equals(id));
        if (removed) {
            writeAll(reviews, complaints);
        }
        return removed;
    }

    // ---------- Complaints ----------

    @Override
    public synchronized List<Complaint> findAllComplaints() {
        List<Complaint> list = new ArrayList<>();
        for (String line : FileHandler.readLines(feedbacksFile)) {
            if (line.isBlank() || line.startsWith("#")) {
                continue;
            }
            if (line.startsWith(Complaint.TYPE + "|") || line.startsWith(Complaint.PREFIX)) {
                try {
                    list.add(FeedbackFactory.complaintFromFileLine(line));
                } catch (RuntimeException e) {
                    log.warn("Skipping bad complaint line in {}: {}", feedbacksFile, line);
                }
            }
        }
        return list;
    }

    @Override
    public synchronized Optional<Complaint> findComplaintById(String id) {
        return findAllComplaints().stream()
                .filter(c -> c.getId().equals(id))
                .findFirst();
    }

    @Override
    public synchronized Complaint saveComplaint(Complaint complaint) {
        List<Review> reviews = findAllReviews();
        List<Complaint> complaints = findAllComplaints();
        if (complaint.getId() == null || complaint.getId().isBlank()) {
            complaint.setId(IdGenerator.nextId(Complaint.PREFIX, complaints.stream().map(Complaint::getId).toList()));
            complaints.add(complaint);
        } else {
            boolean replaced = false;
            for (int i = 0; i < complaints.size(); i++) {
                if (complaints.get(i).getId().equals(complaint.getId())) {
                    complaints.set(i, complaint);
                    replaced = true;
                    break;
                }
            }
            if (!replaced) {
                complaints.add(complaint);
            }
        }
        writeAll(reviews, complaints);
        return complaint;
    }

    @Override
    public synchronized boolean deleteComplaintById(String id) {
        List<Review> reviews = findAllReviews();
        List<Complaint> complaints = findAllComplaints();
        boolean removed = complaints.removeIf(c -> c.getId().equals(id));
        if (removed) {
            writeAll(reviews, complaints);
        }
        return removed;
    }

    private void writeAll(List<Review> reviews, List<Complaint> complaints) {
        List<String> lines = new ArrayList<>();
        for (Review r : reviews) {
            lines.add(r.toFileLine());
        }
        for (Complaint c : complaints) {
            lines.add(c.toFileLine());
        }
        FileHandler.writeLines(feedbacksFile, lines);
    }
}
