package com.grocery.admin;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.List;

/**
 * The list of team modules (the same owners as in .github/CODEOWNERS).
 * For each one it asks Spring "is there a controller for this URL yet?".
 * So when a teammate pushes their work, their card flips from "Not built yet" to "Open"
 * with NO change needed here.
 */
@Service
public class ModuleDirectory {

    private final RequestMappingHandlerMapping handlerMapping;

    public ModuleDirectory(@Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping handlerMapping) {
        this.handlerMapping = handlerMapping;
    }

    public List<ModuleInfo> modules() {
        return List.of(
                module("Inventory", "📦", "/inventory", "/inventory", "@bimsaramaleesha",
                        "Products, stock levels, expiry dates"),
                module("Suppliers", "🚚", "/suppliers", "/suppliers", "@Thilanjana01",
                        "Farmers and wholesale distributors"),
                module("Orders", "📋", "/orders", "/admin/orders", "@it26100058",
                        "All customer orders and their status"),
                module("Promotions", "🏷️", "/promos", "/promos", "@manujiyashindi-glit",
                        "Promo codes and discounts"),
                module("Reviews", "⭐", "/reviews", "/reviews", "@bimsaramaleesha",
                        "Customer reviews and complaints"),
                module("User management", "👥", "/users", "/users", "@MrHasit",
                        "Manage customer and admin accounts"));
    }

    private ModuleInfo module(String name, String icon, String path, String link, String owner, String description) {
        return new ModuleInfo(name, icon, path, link, owner, description, isBuilt(path));
    }

    /** True when some controller method answers this URL (or any URL below it). */
    private boolean isBuilt(String path) {
        for (RequestMappingInfo info : handlerMapping.getHandlerMethods().keySet()) {
            if (info.getPathPatternsCondition() == null) {
                continue;
            }
            for (String pattern : info.getPathPatternsCondition().getPatternValues()) {
                if (pattern.equals(path) || pattern.startsWith(path + "/")) {
                    return true;
                }
            }
        }
        return false;
    }
}
