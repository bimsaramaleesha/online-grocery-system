package com.grocery.user.controller;

import com.grocery.user.dto.PasswordForm;
import com.grocery.user.dto.UserForm;
import com.grocery.user.model.User;
import com.grocery.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/users/profile")
public class ProfileController {

    private final UserService service;
    private final SessionAuth auth;

    public ProfileController(UserService service, SessionAuth auth) {
        this.service = service;
        this.auth = auth;
    }

    @GetMapping
    public String show(HttpSession session, Model model, RedirectAttributes redirect) {
        User user = auth.currentUser(session);
        if (user == null) {
            return loginRequired(redirect);
        }
        fillPage(model, user, UserForm.from(user));
        return "user/profile";
    }

    @PostMapping
    public String update(@ModelAttribute("form") UserForm form, HttpSession session,
                         Model model, RedirectAttributes redirect) {
        User user = auth.currentUser(session);
        if (user == null) {
            return loginRequired(redirect);
        }
        try {
            User updated = service.updateProfile(user.getId(), form);
            auth.refresh(session, updated);
        } catch (IllegalArgumentException e) {
            fillPage(model, user, form);
            model.addAttribute("error", e.getMessage());
            return "user/profile";
        }
        redirect.addFlashAttribute("message", "Profile updated.");
        return "redirect:/users/profile";
    }

    @PostMapping("/password")
    public String changePassword(@ModelAttribute("pwForm") PasswordForm pwForm, HttpSession session,
                                 Model model, RedirectAttributes redirect) {
        User user = auth.currentUser(session);
        if (user == null) {
            return loginRequired(redirect);
        }
        try {
            service.changePassword(user.getId(), pwForm);
        } catch (IllegalArgumentException e) {
            fillPage(model, user, UserForm.from(user));
            model.addAttribute("error", e.getMessage());
            return "user/profile";
        }
        redirect.addFlashAttribute("message", "Password changed.");
        return "redirect:/users/profile";
    }

    @PostMapping("/delete")
    public String deleteAccount(@RequestParam(defaultValue = "") String password, HttpSession session,
                                Model model, RedirectAttributes redirect) {
        User user = auth.currentUser(session);
        if (user == null) {
            return loginRequired(redirect);
        }
        try {
            service.deleteOwnAccount(user.getId(), password);
        } catch (IllegalArgumentException e) {
            fillPage(model, user, UserForm.from(user));
            model.addAttribute("error", e.getMessage());
            return "user/profile";
        }
        auth.logout(session);
        return "redirect:/users/login?deleted";
    }

    private void fillPage(Model model, User user, UserForm form) {
        model.addAttribute("user", user);
        model.addAttribute("form", form);
        model.addAttribute("pwForm", new PasswordForm());
    }

    private String loginRequired(RedirectAttributes redirect) {
        redirect.addFlashAttribute("error", "Please log in to continue.");
        return "redirect:/users/login";
    }
}
