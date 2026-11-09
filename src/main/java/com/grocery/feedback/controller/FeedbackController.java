package com.grocery.feedback.controller;

import com.grocery.common.FileHandler;
import com.grocery.feedback.dto.FeedbackForm;
import com.grocery.feedback.model.Complaint;
import com.grocery.feedback.model.Review;
import com.grocery.feedback.service.FeedbackService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Member 6 — Controller for the Reviews & Customer Complaints module.
 * Reads product names (items.txt) and order IDs (orders.txt) fault-tolerantly via FileHandler
 * so this module has zero Java dependencies on other modules.
 */
@Controller
public class FeedbackController {

    public record ItemOption(String id, String name, String category, double sellingPrice) { }
    public record OrderOption(String orderId, String orderDate, double netAmount) { }

    private final FeedbackService service;

    public FeedbackController(FeedbackService service) {
        this.service = service;
    }

    // ==================== 1. PRODUCT REVIEWS (/reviews) ====================

    @GetMapping("/reviews")
    public String listReviews(@RequestParam(required = false) String q,
                              @RequestParam(required = false) Integer rating,
                              HttpSession session, Model model) {
        String userId = currentUserId(session);
        boolean isAdmin = isAdmin(session);

        List<Review> reviews = service.searchReviews(q, rating);
        model.addAttribute("reviews", reviews);
        model.addAttribute("itemNames", itemNamesMap());
        model.addAttribute("summary", service.summary(userId, isAdmin));
        model.addAttribute("q", q);
        model.addAttribute("rating", rating);
        model.addAttribute("currentUserId", userId);
        model.addAttribute("isAdmin", isAdmin);
        return "feedback/reviews";
    }

    @GetMapping("/reviews/new")
    public String showAddReviewForm(@RequestParam(required = false) String itemId,
                                    HttpSession session, Model model) {
        FeedbackForm form = new FeedbackForm();
        form.setType(Review.TYPE);
        if (itemId != null && !itemId.isBlank()) {
            form.setItemId(itemId);
        }
        prepareFormModel(model, form, false, session);
        return "feedback/form";
    }

    @PostMapping("/reviews/new")
    public String addReview(@ModelAttribute("form") FeedbackForm form,
                            HttpSession session, Model model, RedirectAttributes redirect) {
        form.setType(Review.TYPE);
        try {
            service.addReview(form, currentUserId(session), currentUserName(session));
        } catch (IllegalArgumentException e) {
            prepareFormModel(model, form, false, session);
            model.addAttribute("error", e.getMessage());
            return "feedback/form";
        }
        redirect.addFlashAttribute("message", "Thank you! Your product review has been posted.");
        return "redirect:/reviews";
    }

    @GetMapping("/reviews/{id}/edit")
    public String showEditReviewForm(@PathVariable String id,
                                     HttpSession session, Model model, RedirectAttributes redirect) {
        try {
            Review review = service.getReviewById(id);
            if (!isAdmin(session) && !review.getUserId().equalsIgnoreCase(currentUserId(session))) {
                redirect.addFlashAttribute("error", "You can only edit your own reviews.");
                return "redirect:/reviews";
            }
            prepareFormModel(model, FeedbackForm.from(review), true, session);
            return "feedback/form";
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/reviews";
        }
    }

    @PostMapping("/reviews/{id}/edit")
    public String updateReview(@PathVariable String id,
                               @ModelAttribute("form") FeedbackForm form,
                               HttpSession session, Model model, RedirectAttributes redirect) {
        form.setId(id);
        form.setType(Review.TYPE);
        try {
            service.updateReview(id, form, currentUserId(session), isAdmin(session));
        } catch (IllegalArgumentException e) {
            prepareFormModel(model, form, true, session);
            model.addAttribute("error", e.getMessage());
            return "feedback/form";
        }
        redirect.addFlashAttribute("message", "Review updated successfully.");
        return "redirect:/reviews";
    }

    @PostMapping("/reviews/{id}/delete")
    public String deleteReview(@PathVariable String id, HttpSession session, RedirectAttributes redirect) {
        try {
            service.deleteReview(id, currentUserId(session), isAdmin(session));
            redirect.addFlashAttribute("message", "Review deleted.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/reviews";
    }

    // ==================== 2. ORDER COMPLAINTS (/complaints) ====================

    @GetMapping("/complaints")
    public String listComplaints(@RequestParam(required = false) String q,
                                 @RequestParam(required = false) String status,
                                 HttpSession session, Model model) {
        String userId = currentUserId(session);
        boolean isAdmin = isAdmin(session);

        List<Complaint> complaints = service.searchComplaints(q, status, userId, isAdmin);
        model.addAttribute("complaints", complaints);
        model.addAttribute("summary", service.summary(userId, isAdmin));
        model.addAttribute("q", q);
        model.addAttribute("status", status);
        model.addAttribute("currentUserId", userId);
        model.addAttribute("isAdmin", isAdmin);
        return "feedback/complaints";
    }

    @GetMapping("/complaints/new")
    public String showAddComplaintForm(@RequestParam(required = false) String orderId,
                                       HttpSession session, Model model) {
        FeedbackForm form = new FeedbackForm();
        form.setType(Complaint.TYPE);
        if (orderId != null && !orderId.isBlank()) {
            form.setOrderId(orderId);
        }
        prepareFormModel(model, form, false, session);
        return "feedback/form";
    }

    @PostMapping("/complaints/new")
    public String addComplaint(@ModelAttribute("form") FeedbackForm form,
                               HttpSession session, Model model, RedirectAttributes redirect) {
        form.setType(Complaint.TYPE);
        try {
            service.addComplaint(form, currentUserId(session), currentUserName(session));
        } catch (IllegalArgumentException e) {
            prepareFormModel(model, form, false, session);
            model.addAttribute("error", e.getMessage());
            return "feedback/form";
        }
        redirect.addFlashAttribute("message", "Complaint submitted. Our support team will review it shortly.");
        return "redirect:/complaints";
    }

    @GetMapping("/complaints/{id}/edit")
    public String showEditComplaintForm(@PathVariable String id,
                                        HttpSession session, Model model, RedirectAttributes redirect) {
        try {
            Complaint complaint = service.getComplaintById(id);
            if (!isAdmin(session) && !complaint.getUserId().equalsIgnoreCase(currentUserId(session))) {
                redirect.addFlashAttribute("error", "You can only edit your own complaints.");
                return "redirect:/complaints";
            }
            prepareFormModel(model, FeedbackForm.from(complaint), true, session);
            return "feedback/form";
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/complaints";
        }
    }

    @PostMapping("/complaints/{id}/edit")
    public String updateComplaint(@PathVariable String id,
                                  @ModelAttribute("form") FeedbackForm form,
                                  HttpSession session, Model model, RedirectAttributes redirect) {
        form.setId(id);
        form.setType(Complaint.TYPE);
        try {
            service.updateComplaint(id, form, currentUserId(session), isAdmin(session));
        } catch (IllegalArgumentException e) {
            prepareFormModel(model, form, true, session);
            model.addAttribute("error", e.getMessage());
            return "feedback/form";
        }
        redirect.addFlashAttribute("message", "Complaint updated successfully.");
        return "redirect:/complaints";
    }

    @PostMapping("/complaints/{id}/delete")
    public String deleteComplaint(@PathVariable String id, HttpSession session, RedirectAttributes redirect) {
        try {
            service.deleteComplaint(id, currentUserId(session), isAdmin(session));
            redirect.addFlashAttribute("message", "Complaint deleted.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/complaints";
    }

    // ==================== Fault-Tolerant Helpers ====================

    private void prepareFormModel(Model model, FeedbackForm form, boolean editMode, HttpSession session) {
        boolean isAdmin = isAdmin(session);
        String userId = currentUserId(session);

        model.addAttribute("form", form);
        model.addAttribute("editMode", editMode);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("items", loadItemOptions());
        model.addAttribute("orders", loadOrderOptions(userId, isAdmin));
    }

    private List<ItemOption> loadItemOptions() {
        List<ItemOption> list = new ArrayList<>();
        try {
            for (String line : FileHandler.readLines("items.txt")) {
                String[] p = line.split("\\|", -1);
                if (p.length >= 6) {
                    double price = Double.parseDouble(p[3].trim());
                    list.add(new ItemOption(p[1].trim(), p[2].trim(), p[5].trim(), price));
                }
            }
        } catch (Exception ignored) {
        }
        return list;
    }

    private List<OrderOption> loadOrderOptions(String userId, boolean isAdmin) {
        List<OrderOption> list = new ArrayList<>();
        try {
            for (String line : FileHandler.readLines("orders.txt")) {
                String[] p = line.split("\\|", -1);
                if (p.length >= 8) {
                    String ordId = p[0].trim();
                    String ordUser = p[1].trim();
                    double net = Double.parseDouble(p[5].trim());
                    String date = p[7].trim();
                    if (isAdmin || ordUser.equalsIgnoreCase(userId)) {
                        list.add(new OrderOption(ordId, date, net));
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return list;
    }

    private Map<String, String> itemNamesMap() {
        Map<String, String> map = new HashMap<>();
        for (ItemOption item : loadItemOptions()) {
            map.put(item.id(), item.name());
        }
        return map;
    }

    private String currentUserId(HttpSession session) {
        if (session == null) {
            return "U002";
        }
        Object id = session.getAttribute("userId");
        return (id == null || id.toString().isBlank()) ? "U002" : id.toString();
    }

    private String currentUserName(HttpSession session) {
        if (session == null) {
            return "Customer";
        }
        Object name = session.getAttribute("userName");
        return (name == null || name.toString().isBlank()) ? "Customer" : name.toString();
    }

    private boolean isAdmin(HttpSession session) {
        return session != null && "ADMIN".equals(session.getAttribute("role"));
    }
}
