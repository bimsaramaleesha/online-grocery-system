package com.grocery.inventory.controller;

import com.grocery.inventory.dto.ItemForm;
import com.grocery.inventory.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Receives browser requests and decides which page to show.
 * Every method = one URL.   GET = "show me a page",  POST = "I'm sending data".
 *
 *   GET  /inventory              list (+ ?q=search, ?filter=low|soon|expired)
 *   GET  /inventory/new          empty add form
 *   POST /inventory/new          save the new item
 *   GET  /inventory/{id}/edit    edit form filled with the item
 *   POST /inventory/{id}/edit    save the changes
 *   POST /inventory/{id}/delete  delete
 */
@Controller
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryService service;

    public InventoryController(InventoryService service) {
        this.service = service;
    }

    // ---------- READ ----------
    @GetMapping
    public String list(@RequestParam(required = false) String q,
                       @RequestParam(required = false) String filter, Model model) {
        model.addAttribute("items", service.search(q, filter));   // the page reads it as ${items}
        model.addAttribute("summary", service.summary());
        model.addAttribute("q", q);
        model.addAttribute("filter", filter);
        return "inventory/list";                          // templates/inventory/list.html
    }

    // ---------- CREATE ----------
    @GetMapping("/new")
    public String showAddForm(Model model) {
        model.addAttribute("form", new ItemForm());
        model.addAttribute("editMode", false);
        return "inventory/form";
    }

    @PostMapping("/new")
    public String add(@Valid @ModelAttribute("form") ItemForm form, BindingResult binding,
                      Model model, RedirectAttributes redirect) {
        model.addAttribute("editMode", false);
        if (binding.hasErrors()) {      // e.g. letters typed in the price box
            model.addAttribute("error", "Please check the number and date fields.");
            return "inventory/form";
        }
        try {
            service.addItem(form);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "inventory/form";    // show the form again with the message
        }
        redirect.addFlashAttribute("message", "Item added successfully.");
        return "redirect:/inventory";   // redirect so a browser refresh doesn't re-submit
    }

    // ---------- UPDATE ----------
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable String id, Model model, RedirectAttributes redirect) {
        try {
            model.addAttribute("form", ItemForm.from(service.getById(id)));
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/inventory";
        }
        model.addAttribute("editMode", true);
        return "inventory/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable String id,
                         @Valid @ModelAttribute("form") ItemForm form, BindingResult binding,
                         Model model, RedirectAttributes redirect) {
        form.setId(id);
        model.addAttribute("editMode", true);
        if (binding.hasErrors()) {
            model.addAttribute("error", "Please check the number and date fields.");
            return "inventory/form";
        }
        try {
            service.updateItem(id, form);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "inventory/form";
        }
        redirect.addFlashAttribute("message", "Item updated successfully.");
        return "redirect:/inventory";
    }

    // ---------- DELETE ----------
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable String id, RedirectAttributes redirect) {
        try {
            service.deleteItem(id);
            redirect.addFlashAttribute("message", "Item deleted.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/inventory";
    }
}
