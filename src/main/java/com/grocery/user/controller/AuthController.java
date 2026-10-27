package com.grocery.user.controller;

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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Handles all authentication and self-service tasks for:
 *  1. New customers without an account (/register, /login)
 *  2. Existing logged-in customers & admins (/users/profile, /logout, /access-denied)
 */
@Controller
public class AuthController {

    private final UserService service;
    private final SessionAuth auth;

    public AuthController(UserService service, SessionAuth auth) {
        this.service = service;
        this.auth = auth;
    }

    // ---------- 1. New Customers / Guests: Register & Login ----------

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

    // ---------- 2. Existing Customers & Admins: Self-Service Profile ----------

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

    private void fillProfilePage(Model model, User user, UserForm form) {
        model.addAttribute("user", user);
        model.addAttribute("form", form);
        model.addAttribute("pwForm", new UserForm());
    }

    private String loginRequired(RedirectAttributes redirect) {
        redirect.addFlashAttribute("error", "Please log in to continue.");
        return "redirect:/login";
    }

    private boolean isSafe(String next) {
        return next != null && next.startsWith("/") && !next.startsWith("//") && !next.contains("\\")
                && !next.startsWith("/login") && !next.startsWith("/register");
    }
}
