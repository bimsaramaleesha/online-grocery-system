package com.grocery.user.controller;

import com.grocery.order.model.Order;
import com.grocery.order.service.OrderService;
import com.grocery.user.model.Admin;
import com.grocery.user.model.Customer;
import com.grocery.user.model.User;
import com.grocery.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles HTTP session state for logged-in users and acts as the request door-guard
 * (HandlerInterceptor + WebMvcConfigurer) for the whole application:
 *  - New customers / guests -> can browse /shop, /shop/{id}, /login, /register
 *  - Existing customers     -> + /cart, /orders (own orders), /users/profile, /reviews, /complaints
 *  - Administrators         -> + /admin/**, /inventory, /suppliers, /promos, /users
 */
@Configuration
public class SessionAuth implements HandlerInterceptor, WebMvcConfigurer {

    public static final String USER_ID = "userId";
    public static final String USER_NAME = "userName";
    public static final String ROLE = "role";
    public static final String USER_ROLE = "userRole";

    private static final int TIMEOUT_SECONDS = 30 * 60;

    private static final List<String> ADMIN_AREAS =
            List.of("/admin", "/inventory", "/suppliers", "/promos", "/users");

    private static final List<String> PUBLIC_AREAS =
            List.of("/users/login", "/users/register", "/users/logout");

    private static final List<String> SELF_SERVICE_AREAS =
            List.of("/users/profile");

    private static final List<String> LOGIN_AREAS =
            List.of("/cart", "/orders", "/reviews", "/complaints", "/users/profile");

    private static final Pattern ORDER_STATUS_URL = Pattern.compile("^/orders/[^/]+/status$");
    private static final Pattern ONE_ORDER_URL = Pattern.compile("^/orders/(ORD[^/]+)(/.*)?$", Pattern.CASE_INSENSITIVE);

    private final UserService userService;
    private final OrderService orderService;

    public SessionAuth(UserService userService, OrderService orderService) {
        this.userService = userService;
        this.orderService = orderService;
    }

    public User currentUser(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object id = session.getAttribute(USER_ID);
        return id == null ? null : userService.findById(id.toString()).orElse(null);
    }

    public void login(HttpServletRequest request, User user) {
        if (request.getSession(false) != null) {
            request.changeSessionId();
        }
        HttpSession session = request.getSession(true);
        session.setAttribute(USER_ID, user.getId());
        session.setAttribute(USER_NAME, user.getName());
        session.setAttribute(ROLE, user.getRole());
        session.setAttribute(USER_ROLE, user.getRole());
        session.setMaxInactiveInterval(TIMEOUT_SECONDS);
    }

    public void refresh(HttpSession session, User user) {
        if (session != null && user != null) {
            session.setAttribute(USER_NAME, user.getName());
            session.setAttribute(ROLE, user.getRole());
            session.setAttribute(USER_ROLE, user.getRole());
        }
    }

    public void logout(HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
    }

    public static String roleOf(HttpSession session) {
        return session == null ? null : (String) session.getAttribute(ROLE);
    }

    public static String userIdOf(HttpSession session) {
        return session == null ? null : (String) session.getAttribute(USER_ID);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this)
                .addPathPatterns("/**")
                .excludePathPatterns("/css/**", "/js/**", "/images/**", "/favicon.ico", "/error");
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        HttpSession session = request.getSession(false);
        String role = roleOf(session);

        if (startsWithAny(path, PUBLIC_AREAS)) {
            return true;
        }

        boolean adminOnly = (startsWithAny(path, ADMIN_AREAS) && !startsWithAny(path, SELF_SERVICE_AREAS))
                || ORDER_STATUS_URL.matcher(path).matches();
        boolean loginNeeded = adminOnly || startsWithAny(path, LOGIN_AREAS);

        if (!loginNeeded) {
            return true;
        }
        if (role == null) {
            String next = "GET".equals(request.getMethod()) ? path : "/shop";
            response.sendRedirect(request.getContextPath() + "/login?next="
                    + URLEncoder.encode(next, StandardCharsets.UTF_8));
            return false;
        }
        if (adminOnly && !Admin.TYPE.equals(role)) {
            response.sendRedirect(request.getContextPath() + "/access-denied");
            return false;
        }
        if (Customer.TYPE.equals(role) && !ownsOrderInUrl(path, userIdOf(session))) {
            response.sendRedirect(request.getContextPath() + "/access-denied");
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

    private boolean ownsOrderInUrl(String path, String userId) {
        Matcher m = ONE_ORDER_URL.matcher(path);
        if (!m.matches()) {
            return true;
        }
        Optional<Order> order = orderService.getOrderById(m.group(1));
        return order.isEmpty() || (userId != null && userId.equalsIgnoreCase(order.get().getUserId()));
    }
}
