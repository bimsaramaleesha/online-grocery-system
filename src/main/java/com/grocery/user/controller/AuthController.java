package com.grocery.user.controller;

import com.grocery.user.dto.LoginForm;
import com.grocery.user.dto.UserForm;
import com.grocery.user.model.User;
import com.grocery.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller("userAuthController")
@RequestMapping("/users")
public class AuthController {

    private final UserService service;
    private final SessionAuth auth;

    public AuthController(UserService service, SessionAuth auth) {
        this.service = service;
        this.auth = auth;
    }

    @GetMapping("/register")
    public String showRegister(HttpSession session, Model model) {
        User current = auth.currentUser(session);
        if (current != null) {
            return "redirect:" + current.getHomePath();
        }
        model.addAttribute("form", new UserForm());
        return "user/register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute("form") UserForm form, Model model,
                           RedirectAttributes redirect) {
        try {
            service.register(form);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "user/register";
        }
        redirect.addFlashAttribute("message", "Registration successful. Please log in.");
        return "redirect:/users/login";
    }

    @GetMapping("/login")
    public String showLogin(HttpSession session, Model model) {
        User current = auth.currentUser(session);
        if (current != null) {
            return "redirect:" + current.getHomePath();
        }
        model.addAttribute("form", new LoginForm());
        return "user/login";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute("form") LoginForm form, HttpServletRequest request,
                        Model model) {
        User user;
        try {
            user = service.authenticate(form.getEmail(), form.getPassword());
        } catch (IllegalArgumentException e) {
            form.setPassword(null);
            model.addAttribute("error", e.getMessage());
            return "user/login";
        }
        auth.login(request, user);
        return "redirect:" + user.getHomePath();
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        auth.logout(session);
        return "redirect:/users/login?logout";
    }
}
