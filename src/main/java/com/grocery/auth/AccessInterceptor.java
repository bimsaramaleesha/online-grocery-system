package com.grocery.auth;

import com.grocery.order.model.Order;
import com.grocery.order.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * THE DOOR GUARD. Runs before EVERY request and decides: let in, send to login, or deny.
 * Because it works on the URL, it protects every module without touching their code,
 * including modules your teammates have not pushed yet.
 *
 *   Guest      -> shop pages, login, register
 *   Customer   -> + cart, own orders
 *   Admin      -> everything
 *
 * To protect a new page, just add its URL start to one of the two lists below.
 */
@Component
public class AccessInterceptor implements HandlerInterceptor {

    /** Only admins may open these (and everything below them). */
    private static final List<String> ADMIN_AREAS =
            List.of("/admin", "/inventory", "/suppliers", "/promos", "/users");

    /** Any logged-in user (customer or admin) may open these. */
    private static final List<String> LOGIN_AREAS =
            List.of("/cart", "/orders", "/reviews", "/complaints");

    /** Changing an order's status (Pending -> Shipped ...) is an admin job. */
    private static final Pattern ORDER_STATUS_URL = Pattern.compile("^/orders/[^/]+/status$");

    /** Order pages of ONE order, e.g. /orders/ORD001 or /orders/ORD001/cancel. */
    private static final Pattern ONE_ORDER_URL = Pattern.compile("^/orders/(ORD[^/]+)(/.*)?$", Pattern.CASE_INSENSITIVE);

    private final OrderService orderService;

    public AccessInterceptor(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        HttpSession session = request.getSession(false);
        Role role = AuthSession.roleOf(session);

        boolean adminOnly = startsWithAny(path, ADMIN_AREAS) || ORDER_STATUS_URL.matcher(path).matches();
        boolean loginNeeded = adminOnly || startsWithAny(path, LOGIN_AREAS);

        if (!loginNeeded) {
            return true;                                   // public page
        }
        if (role == null) {
            sendToLogin(request, response, path);          // guest
            return false;
        }
        if (adminOnly && role != Role.ADMIN) {
            response.sendRedirect(request.getContextPath() + "/access-denied");
            return false;
        }
        if (role == Role.CUSTOMER && !ownsOrderInUrl(path, AuthSession.userIdOf(session))) {
            response.sendRedirect(request.getContextPath() + "/access-denied");   // someone else's order
            return false;
        }
        return true;
    }

    private boolean startsWithAny(String path, List<String> areas) {
        for (String area : areas) {
            if (path.equals(area) || path.startsWith(area + "/")) {
                return true;
            }
        }
        return false;
    }

    /** A customer may only open orders that belong to them. */
    private boolean ownsOrderInUrl(String path, String userId) {
        Matcher m = ONE_ORDER_URL.matcher(path);
        if (!m.matches()) {
            return true;                                   // not an "one order" page
        }
        Optional<Order> order = orderService.getOrderById(m.group(1));
        return order.isEmpty() || userId != null && userId.equalsIgnoreCase(order.get().getUserId());
    }

    private void sendToLogin(HttpServletRequest request, HttpServletResponse response, String path)
            throws java.io.IOException {
        // Only GET pages can be reopened after login; for a POST (e.g. add to cart) go to the shop.
        String next = "GET".equals(request.getMethod()) ? path : "/shop";
        response.sendRedirect(request.getContextPath() + "/login?next="
                + URLEncoder.encode(next, StandardCharsets.UTF_8));
    }
}
