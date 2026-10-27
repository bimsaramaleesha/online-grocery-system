package com.grocery.user.controller;

import com.grocery.user.dto.UserForm;
import com.grocery.user.model.User;
import com.grocery.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/users")
public class AdminUserController {

    private final UserService service;
    private final SessionAuth auth;

    public AdminUserController(UserService service, SessionAuth auth) {
        this.service = service;
        this.auth = auth;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String q,
                       @RequestParam(required = false) String role,
                       HttpSession session, Model model, RedirectAttributes redirect) {
        // /users is also the link in the navbar, so a customer is simply sent to their own profile
        User me = auth.currentUser(session);
        if (me != null && !me.canManageUsers()) {
            return "redirect:" + me.getHomePath();
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

    @GetMapping("/new")
    public String showAddForm(HttpSession session, Model model, RedirectAttributes redirect) {
        String denied = guard(auth.currentUser(session), redirect);
        if (denied != null) {
            return denied;
        }
        model.addAttribute("form", new UserForm());
        model.addAttribute("editMode", false);
        return "user/user-form";
    }

    @PostMapping("/new")
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

    @GetMapping("/{id}/edit")
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

    @PostMapping("/{id}/edit")
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

    @PostMapping("/{id}/delete")
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

    private String guard(User me, RedirectAttributes redirect) {
        if (me == null) {
            redirect.addFlashAttribute("error", "Please log in to continue.");
            return "redirect:/users/login";
        }
        if (!me.canManageUsers()) {
            redirect.addFlashAttribute("error", "Only administrators can open that page.");
            return "redirect:" + me.getHomePath();
        }
        return null;
    }
}
