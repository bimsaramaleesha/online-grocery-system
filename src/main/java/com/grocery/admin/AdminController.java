package com.grocery.admin;

import com.grocery.inventory.service.InventoryService;
import com.grocery.order.model.Order;
import com.grocery.order.model.OrderStatus;
import com.grocery.order.service.OrderService;
import com.grocery.supplier.service.SupplierService;
import com.grocery.user.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Comparator;
import java.util.List;

/**
 * Admin-only pages (SessionAuth blocks everyone else from /admin/**).
 *   GET /admin           dashboard: numbers from every module + module cards
 *   GET /admin/orders    ALL customers' orders (the /orders page only shows your own)
 *   GET /admin/accounts  redirects to /users (unified user management)
 */
@Controller
public class AdminController {

    private final InventoryService inventory;
    private final OrderService orders;
    private final SupplierService suppliers;
    private final UserService users;
    private final ModuleDirectory modules;

    public AdminController(InventoryService inventory, OrderService orders, SupplierService suppliers,
                           UserService users, ModuleDirectory modules) {
        this.inventory = inventory;
        this.orders = orders;
        this.suppliers = suppliers;
        this.users = users;
        this.modules = modules;
    }

    @GetMapping("/admin")
    public String dashboard(Model model) {
        List<Order> all = newestFirst(orders.getAllOrders());
        double revenue = all.stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                .mapToDouble(Order::getNetAmount)
                .sum();

        model.addAttribute("inventory", inventory.summary());
        model.addAttribute("orderCount", all.size());
        model.addAttribute("pendingCount", all.stream().filter(o -> o.getStatus() == OrderStatus.PENDING).count());
        model.addAttribute("revenue", revenue);
        model.addAttribute("recentOrders", all.stream().limit(5).toList());
        model.addAttribute("supplierCount", suppliers.getAllSuppliers().size());
        model.addAttribute("customerCount", users.summary().customers());
        model.addAttribute("modules", modules.modules());
        return "admin/dashboard";
    }

    @GetMapping("/admin/orders")
    public String allOrders(Model model) {
        model.addAttribute("orders", newestFirst(orders.getAllOrders()));
        return "admin/orders";
    }

    @GetMapping("/admin/accounts")
    public String allAccounts() {
        return "redirect:/users";
    }

    private List<Order> newestFirst(List<Order> list) {
        return list.stream().sorted(Comparator.comparing(Order::getOrderId).reversed()).toList();
    }
}
