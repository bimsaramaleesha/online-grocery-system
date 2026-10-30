package com.grocery.user.controller;

import com.grocery.common.FileHandler;
import com.grocery.user.dto.UserForm;
import com.grocery.user.model.User;
import com.grocery.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Member 1 — User & Authentication Controller.
 * Handles guest login/registration, customer profile management, and admin user/dashboard views.
 * Reads store statistics fault-tolerantly from data files so the user module stays 100% independent.
 */
@Controller
public class UserController {

    public record InventoryStats(int total, double stockValue, int lowStock, int expiringSoon, int expired) { }
    public record StatusBadge(String displayName, String badgeClass) { }
    public record OrderRow(String orderId, String userId, String orderDate, String paymentMethod,
                           double netAmount, String rawStatus, StatusBadge status) { }
    public record ModuleCard(String name, String owner, String icon, String description, String link, boolean built) { }

    private final UserService service;
    private final SessionAuth auth;

    public UserController(UserService service, SessionAuth auth) {
        this.service = service;
        this.auth = auth;
    }

    // ---------- 1. Guest & Customer Authentication (/register, /login, /logout) ----------

    @GetMapping({"/register", "/users/register"})
    public String showRegister(HttpSession session, Model model) {
        User current = auth.currentUser(session);
        if (current != null) {
            return "redirect:" + current.getHomePath();
        }
        model.addAttribute("form", new UserForm());
        return "user/register";
    }

    @PostMapping({"/register", "/users/register"})
    public String register(@ModelAttribute("form") UserForm form, Model model,
                           RedirectAttributes redirect) {
        try {
            service.register(form);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "user/register";
        }
        redirect.addFlashAttribute("message", "Account created! Please sign in below.");
        return "redirect:/login";
    }

    @GetMapping({"/login", "/users/login"})
    public String showLogin(@RequestParam(required = false) String next,
                            HttpSession session, Model model) {
        User current = auth.currentUser(session);
        if (current != null) {
            return "redirect:" + current.getHomePath();
        }
        model.addAttribute("form", new UserForm());
        model.addAttribute("next", next);
        return "user/login";
    }

    @PostMapping({"/login", "/users/login"})
    public String login(@ModelAttribute("form") UserForm form,
                        @RequestParam(required = false) String next,
                        HttpServletRequest request, Model model) {
        User user;
        try {
            user = service.authenticate(form.getEmail(), form.getPassword());
        } catch (IllegalArgumentException e) {
            form.setPassword(null);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("next", next);
            return "user/login";
        }
        auth.login(request, user);
        if (user.canManageUsers()) {
            return "redirect:" + user.getHomePath();
        }
        return "redirect:" + (isSafe(next) ? next : user.getHomePath());
    }

    @PostMapping({"/logout", "/users/logout"})
    public String logout(HttpSession session) {
        auth.logout(session);
        return "redirect:/login?logout";
    }

    @GetMapping("/access-denied")
    public String denied(HttpServletResponse response, Model model) {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        model.addAttribute("status", 403);
        model.addAttribute("message", "Your account does not have permission to open that page.");
        return "error";
    }

    // ---------- 2. Customer & Admin Self-Service Profile (/users/profile) ----------

    @GetMapping("/users/profile")
    public String showProfile(HttpSession session, Model model, RedirectAttributes redirect) {
        User user = auth.currentUser(session);
        if (user == null) {
            return loginRequired(redirect);
        }
        fillProfilePage(model, user, UserForm.from(user));
        return "user/profile";
    }

    @PostMapping("/users/profile")
    public String updateProfile(@ModelAttribute("form") UserForm form, HttpSession session,
                                Model model, RedirectAttributes redirect) {
        User user = auth.currentUser(session);
        if (user == null) {
            return loginRequired(redirect);
        }
        try {
            User updated = service.updateProfile(user.getId(), form);
            auth.refresh(session, updated);
        } catch (IllegalArgumentException e) {
            fillProfilePage(model, user, form);
            model.addAttribute("error", e.getMessage());
            return "user/profile";
        }
        redirect.addFlashAttribute("message", "Profile updated.");
        return "redirect:/users/profile";
    }

    @PostMapping("/users/profile/password")
    public String changePassword(@ModelAttribute("pwForm") UserForm pwForm, HttpSession session,
                                 Model model, RedirectAttributes redirect) {
        User user = auth.currentUser(session);
        if (user == null) {
            return loginRequired(redirect);
        }
        try {
            service.changePassword(user.getId(), pwForm);
        } catch (IllegalArgumentException e) {
            fillProfilePage(model, user, UserForm.from(user));
            model.addAttribute("error", e.getMessage());
            return "user/profile";
        }
        redirect.addFlashAttribute("message", "Password changed.");
        return "redirect:/users/profile";
    }

    @PostMapping("/users/profile/delete")
    public String deleteAccount(@RequestParam(defaultValue = "") String password, HttpSession session,
                                Model model, RedirectAttributes redirect) {
        User user = auth.currentUser(session);
        if (user == null) {
            return loginRequired(redirect);
        }
        try {
            service.deleteOwnAccount(user.getId(), password);
        } catch (IllegalArgumentException e) {
            fillProfilePage(model, user, UserForm.from(user));
            model.addAttribute("error", e.getMessage());
            return "user/profile";
        }
        auth.logout(session);
        return "redirect:/login?deleted";
    }

    // ---------- 3. Admin Dashboard & Store Overview (/admin) ----------

    @GetMapping("/admin")
    public String dashboard(Model model) {
        List<OrderRow> allOrders = readOrderRows();
        double revenue = allOrders.stream()
                .filter(o -> !"CANCELLED".equalsIgnoreCase(o.rawStatus()))
                .mapToDouble(OrderRow::netAmount)
                .sum();
        long pendingCount = allOrders.stream()
                .filter(o -> "PENDING".equalsIgnoreCase(o.rawStatus()))
                .count();

        model.addAttribute("inventory", readInventoryStats());
        model.addAttribute("orderCount", allOrders.size());
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("revenue", revenue);
        model.addAttribute("recentOrders", allOrders.stream().limit(5).toList());
        model.addAttribute("supplierCount", countSuppliers());
        model.addAttribute("customerCount", service.summary().customers());
        model.addAttribute("modules", moduleCards());
        return "user/dashboard";
    }

    @GetMapping("/admin/orders")
    public String allOrders(Model model) {
        model.addAttribute("orders", readOrderRows());
        return "user/orders";
    }

    @GetMapping("/admin/accounts")
    public String allAccounts() {
        return "redirect:/users";
    }

    // ---------- 4. Admin User Management (/users) ----------

    @GetMapping("/users")
    public String list(@RequestParam(required = false) String q,
                       @RequestParam(required = false) String role,
                       HttpSession session, Model model, RedirectAttributes redirect) {
        User me = auth.currentUser(session);
        if (me != null && !me.canManageUsers()) {
            return "redirect:/users/profile";
        }
        String denied = guard(me, redirect);
        if (denied != null) {
            return denied;
        }
        model.addAttribute("users", service.search(q, role));
        model.addAttribute("summary", service.summary());
        model.addAttribute("q", q);
        model.addAttribute("role", role);
        model.addAttribute("currentUserId", me.getId());
        return "user/admin";
    }

    @GetMapping("/users/new")
    public String showAddForm(HttpSession session, Model model, RedirectAttributes redirect) {
        String denied = guard(auth.currentUser(session), redirect);
        if (denied != null) {
            return denied;
        }
        model.addAttribute("form", new UserForm());
        model.addAttribute("editMode", false);
        return "user/user-form";
    }

    @PostMapping("/users/new")
    public String add(@ModelAttribute("form") UserForm form, HttpSession session,
                      Model model, RedirectAttributes redirect) {
        String denied = guard(auth.currentUser(session), redirect);
        if (denied != null) {
            return denied;
        }
        model.addAttribute("editMode", false);
        try {
            service.createUser(form);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "user/user-form";
        }
        redirect.addFlashAttribute("message", "Account created.");
        return "redirect:/users";
    }

    @GetMapping("/users/{id}/edit")
    public String showEditForm(@PathVariable String id, HttpSession session,
                               Model model, RedirectAttributes redirect) {
        String denied = guard(auth.currentUser(session), redirect);
        if (denied != null) {
            return denied;
        }
        try {
            model.addAttribute("form", UserForm.from(service.getById(id)));
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/users";
        }
        model.addAttribute("editMode", true);
        return "user/user-form";
    }

    @PostMapping("/users/{id}/edit")
    public String update(@PathVariable String id, @ModelAttribute("form") UserForm form,
                         HttpSession session, Model model, RedirectAttributes redirect) {
        User me = auth.currentUser(session);
        String denied = guard(me, redirect);
        if (denied != null) {
            return denied;
        }
        form.setId(id);
        model.addAttribute("editMode", true);
        try {
            User updated = service.adminUpdate(id, form);
            if (me.getId().equals(id)) {
                auth.refresh(session, updated);
            }
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "user/user-form";
        }
        redirect.addFlashAttribute("message", "Account updated.");
        return "redirect:/users";
    }

    @PostMapping("/users/{id}/delete")
    public String delete(@PathVariable String id, HttpSession session, RedirectAttributes redirect) {
        User me = auth.currentUser(session);
        String denied = guard(me, redirect);
        if (denied != null) {
            return denied;
        }
        if (me.getId().equals(id)) {
            redirect.addFlashAttribute("error",
                    "You cannot delete the account you are logged in with. Use your profile page for that.");
            return "redirect:/users";
        }
        try {
            service.deleteUser(id);
            redirect.addFlashAttribute("message", "Account deleted.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/users";
    }

    // ---------- Fault-Tolerant Helper Methods ----------

    private List<OrderRow> readOrderRows() {
        List<OrderRow> rows = new ArrayList<>();
        try {
            for (String line : FileHandler.readLines("orders.txt")) {
                String[] p = line.split("\\|", -1);
                if (p.length >= 8) {
                    String id = p[0].trim();
                    String userId = p[1].trim();
                    double net = parseDouble(p[5]);
                    String rawStatus = p[6].trim().isEmpty() ? "PENDING" : p[6].trim().toUpperCase();
                    String date = p[7].trim();
                    String payment = p.length > 10 && !p[10].trim().isEmpty() ? p[10].trim() : "Cash on Delivery";
                    rows.add(new OrderRow(id, userId, date, payment, net, rawStatus, toBadge(rawStatus)));
                }
            }
        } catch (Exception ignored) {
            // Fault isolation: if orders.txt is missing or malformed, return empty list
        }
        rows.sort(Comparator.comparing(OrderRow::orderId).reversed());
        return rows;
    }

    private StatusBadge toBadge(String rawStatus) {
        return switch (rawStatus) {
            case "CONFIRMED" -> new StatusBadge("Confirmed", "badge bg-info text-dark");
            case "PROCESSING" -> new StatusBadge("Processing", "badge bg-primary");
            case "OUT_FOR_DELIVERY" -> new StatusBadge("Out for Delivery", "badge bg-secondary");
            case "DELIVERED" -> new StatusBadge("Delivered", "badge bg-success");
            case "CANCELLED" -> new StatusBadge("Cancelled", "badge bg-danger");
            default -> new StatusBadge("Pending", "badge bg-warning text-dark");
        };
    }

    private InventoryStats readInventoryStats() {
        int total = 0;
        double stockValue = 0.0;
        int lowStock = 0;
        int expiringSoon = 0;
        int expired = 0;
        try {
            for (String line : FileHandler.readLines("items.txt")) {
                String[] p = line.split("\\|", -1);
                if (p.length >= 6) {
                    total++;
                    double price = parseDouble(p[3]);
                    int qty = (int) parseDouble(p[4]);
                    stockValue += price * qty;
                    if (qty <= 10) {
                        lowStock++;
                    }
                    if ("PERISHABLE".equalsIgnoreCase(p[0].trim()) && p.length >= 7) {
                        try {
                            LocalDate exp = LocalDate.parse(p[6].trim());
                            long days = ChronoUnit.DAYS.between(LocalDate.now(), exp);
                            if (days < 0) {
                                expired++;
                            } else if (days <= 3) {
                                expiringSoon++;
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return new InventoryStats(total, stockValue, lowStock, expiringSoon, expired);
    }

    private int countSuppliers() {
        try {
            return FileHandler.readLines("suppliers.txt").size();
        } catch (Exception ignored) {
            return 0;
        }
    }

    private List<ModuleCard> moduleCards() {
        return List.of(
                new ModuleCard("Users & Accounts", "@member-1", "👤", "Logins, sign-ups, profiles and admin access", "/users", true),
                new ModuleCard("Inventory & Storefront", "@member-2", "🥦", "Products, prices, stock levels and expiry dates", "/inventory", true),
                new ModuleCard("Suppliers", "@member-3", "🚚", "Local farmers and wholesale vendors", "/suppliers", true),
                new ModuleCard("Orders & Cart", "@member-4", "🛒", "Customer baskets, checkout and order tracking", "/admin/orders", true),
                new ModuleCard("Promotions", "@member-5", "🏷️", "Discount codes and special offers", "/promos", true),
                new ModuleCard("Reviews & Complaints", "@member-6", "💬", "Customer ratings and issue tickets", "/reviews", true)
        );
    }

    private double parseDouble(String s) {
        try {
            return Double.parseDouble(s.trim());
        } catch (Exception e) {
            return 0.0;
        }
    }

    private void fillProfilePage(Model model, User user, UserForm form) {
        model.addAttribute("user", user);
        model.addAttribute("form", form);
        model.addAttribute("pwForm", new UserForm());
    }

    private String loginRequired(RedirectAttributes redirect) {
        redirect.addFlashAttribute("error", "Please log in to continue.");
        return "redirect:/login";
    }

    private String guard(User me, RedirectAttributes redirect) {
        if (me == null) {
            return loginRequired(redirect);
        }
        if (!me.canManageUsers()) {
            redirect.addFlashAttribute("error", "Only administrators can open that page.");
            return "redirect:" + me.getHomePath();
        }
        return null;
    }

    private boolean isSafe(String next) {
        return next != null && next.startsWith("/") && !next.startsWith("//") && !next.contains("\\")
                && !next.startsWith("/login") && !next.startsWith("/register");
    }
}
