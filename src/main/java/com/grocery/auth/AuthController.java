package com.grocery.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

/**
 *   GET  /login          login page (?next=/page  = where to go afterwards)
 *   POST /login          check email + password
 *   GET  /register       customer sign-up page
 *   POST /register       create a CUSTOMER account and log in
 *   POST /logout         end the session
 *   GET  /access-denied  "you are not allowed here" page
 */
@Controller
public class AuthController {

    private final AccountService service;

    public AuthController(AccountService service) {
        this.service = service;
    }

    // ---------- LOGIN ----------
    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String next, HttpSession session, Model model) {
        Role role = AuthSession.roleOf(session);
        if (role != null) {
            return "redirect:" + (role == Role.ADMIN ? "/admin" : "/shop");   // already logged in
        }
        model.addAttribute("next", next);
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password,
                        @RequestParam(required = false) String next,
                        HttpServletRequest request, Model model) {
        Optional<Account> account = service.login(email, password);
        if (account.isEmpty()) {
            model.addAttribute("error", "Wrong email or password.");
            model.addAttribute("email", email);
            model.addAttribute("next", next);
            return "auth/login";
        }
        return startSession(request, account.get(), next);
    }

    // ---------- REGISTER ----------
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("form", new RegisterForm());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("form") RegisterForm form, BindingResult binding,
                           HttpServletRequest request, Model model) {
        if (binding.hasErrors()) {
            model.addAttribute("error", binding.getAllErrors().get(0).getDefaultMessage());
            return "auth/register";
        }
        try {
            Account account = service.registerCustomer(form);
            return startSession(request, account, null);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        }
    }

    // ---------- LOGOUT / DENIED ----------
    @PostMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirect) {
        session.invalidate();
        redirect.addFlashAttribute("message", "You have been logged out.");
        return "redirect:/shop";
    }

    @GetMapping("/access-denied")
    public String denied(HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        return "auth/denied";
    }

    // ---------- helpers ----------

    /** Logs the account in. A fresh session id on login stops "session fixation" attacks. */
    private String startSession(HttpServletRequest request, Account account, String next) {
        request.getSession(true);
        request.changeSessionId();
        AuthSession.signIn(request.getSession(), account);
        if (account.isAdmin()) {
            return "redirect:/admin";                       // admins always land on the dashboard
        }
        return "redirect:" + (isSafe(next) ? next : account.getHomePath());
    }

    /** Only allow our own pages as the "next" target (blocks redirects to other websites). */
    private boolean isSafe(String next) {
        return next != null && next.startsWith("/") && !next.startsWith("//") && !next.contains("\\")
                && !next.startsWith("/login") && !next.startsWith("/register");
    }
}
