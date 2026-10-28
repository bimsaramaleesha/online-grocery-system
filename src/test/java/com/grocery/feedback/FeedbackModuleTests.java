package com.grocery.feedback;

import com.grocery.feedback.dto.FeedbackForm;
import com.grocery.feedback.model.Complaint;
import com.grocery.feedback.model.Feedback;
import com.grocery.feedback.model.FeedbackFactory;
import com.grocery.feedback.model.Review;
import com.grocery.feedback.repository.FileFeedbackRepository;
import com.grocery.feedback.service.FeedbackService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class FeedbackModuleTests {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;
    private FeedbackService isolatedService;
    private String reviewsFile;
    private String complaintsFile;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        long stamp = System.nanoTime();
        reviewsFile = "reviews-test-" + stamp + ".txt";
        complaintsFile = "complaints-test-" + stamp + ".txt";
        isolatedService = new FeedbackService(new FileFeedbackRepository(reviewsFile, complaintsFile));
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(Path.of("data", reviewsFile));
        Files.deleteIfExists(Path.of("data", complaintsFile));
    }

    @Test
    void inheritancePolymorphismAndFileSerializationWorkForReviewAndComplaint() {
        Feedback review = new Review("REV001", "ITM001", "U002", "Nimal",
                5, "Great Milk", "Fresh | creamy", LocalDate.of(2026, 10, 20));
        Feedback complaint = new Complaint("CMP001", "ORD001", "U002", "Nimal",
                "Late Delivery", "HIGH", Complaint.STATUS_OPEN,
                "Order was delayed", "", LocalDate.of(2026, 10, 21));

        assertEquals("REVIEW", review.getFeedbackType());
        assertEquals("ITM001", review.getReferenceId());
        assertFalse(review.requiresAttention());

        assertEquals("COMPLAINT", complaint.getFeedbackType());
        assertEquals("ORD001", complaint.getReferenceId());
        assertTrue(complaint.requiresAttention());

        Review parsedReview = FeedbackFactory.reviewFromFileLine(review.toFileLine());
        assertEquals(review.toFileLine(), parsedReview.toFileLine());
        assertFalse(parsedReview.getComment().contains("|"));

        Complaint parsedComplaint = FeedbackFactory.complaintFromFileLine(complaint.toFileLine());
        assertEquals(complaint.toFileLine(), parsedComplaint.toFileLine());
    }

    @Test
    void reviewAndComplaintCrudOperationsAndPermissionsWork() {
        // 1. Add Review
        FeedbackForm revForm = new FeedbackForm();
        revForm.setItemId("ITM001");
        revForm.setRating(5);
        revForm.setTitle("Awesome quality");
        revForm.setComment("Loved this item!");

        Review addedReview = isolatedService.addReview(revForm, "U002", "Demo Customer");
        assertEquals("REV001", addedReview.getId());
        assertEquals(1, isolatedService.searchReviews(null, null).size());

        // 2. Update Review by owner
        revForm.setRating(4);
        revForm.setTitle("Very good quality");
        Review updatedReview = isolatedService.updateReview("REV001", revForm, "U002", false);
        assertEquals(4, updatedReview.getRating());

        // Non-owner customer cannot update or delete another customer's review
        assertThrows(IllegalArgumentException.class,
                () -> isolatedService.updateReview("REV001", revForm, "U999", false));

        // 3. Add Complaint
        FeedbackForm cmpForm = new FeedbackForm();
        cmpForm.setOrderId("ORD001");
        cmpForm.setCategory("Damaged / Spoiled Item");
        cmpForm.setSeverity("HIGH");
        cmpForm.setComment("Milk carton was leaking.");

        Complaint addedComplaint = isolatedService.addComplaint(cmpForm, "U002", "Demo Customer");
        assertEquals("CMP001", addedComplaint.getId());
        assertEquals(Complaint.STATUS_OPEN, addedComplaint.getStatus());

        // 4. Admin resolves complaint
        cmpForm.setStatus(Complaint.STATUS_RESOLVED);
        cmpForm.setResolutionNote("Full refund processed.");
        Complaint resolved = isolatedService.updateComplaint("CMP001", cmpForm, "U001", true);
        assertTrue(resolved.isResolved());
        assertEquals("Full refund processed.", resolved.getResolutionNote());

        // 5. Delete Review & Complaint
        isolatedService.deleteReview("REV001", "U002", false);
        assertTrue(isolatedService.searchReviews(null, null).isEmpty());

        isolatedService.deleteComplaint("CMP001", "U001", true);
        assertTrue(isolatedService.searchComplaints(null, null, "U001", true).isEmpty());
    }

    @Test
    void feedbackWebRoutesAreAccessibleToLoggedInCustomerAndAdmin() throws Exception {
        MockHttpSession customerSession = new MockHttpSession();
        customerSession.setAttribute("userId", "U002");
        customerSession.setAttribute("userName", "Demo Customer");
        customerSession.setAttribute("role", "CUSTOMER");

        mockMvc.perform(get("/reviews").session(customerSession)).andExpect(status().isOk());
        mockMvc.perform(get("/reviews/new").session(customerSession)).andExpect(status().isOk());
        mockMvc.perform(get("/complaints").session(customerSession)).andExpect(status().isOk());
        mockMvc.perform(get("/complaints/new").session(customerSession)).andExpect(status().isOk());

        MockHttpSession adminSession = new MockHttpSession();
        adminSession.setAttribute("userId", "U001");
        adminSession.setAttribute("userName", "Store Admin");
        adminSession.setAttribute("role", "ADMIN");

        mockMvc.perform(get("/reviews").session(adminSession)).andExpect(status().isOk());
        mockMvc.perform(get("/complaints").session(adminSession)).andExpect(status().isOk());
    }
}
