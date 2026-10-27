package com.grocery.user.controller;

import com.grocery.auth.AuthSession;
import com.grocery.user.model.User;
import com.grocery.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

@Component
public class SessionAuth {

    public static final String USER_ID = "userId";
    public static final String USER_NAME = "userName";
    public static final String USER_ROLE = "userRole";

    private static final int TIMEOUT_SECONDS = 30 * 60;

    private final UserService service;

    public SessionAuth(UserService service) {
        this.service = service;
    }

    public User currentUser(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object id = session.getAttribute(USER_ID);
        if (id == null) {
            return null;
        }
        return service.findById(id.toString()).orElse(null);
    }

    public void login(HttpServletRequest request, User user) {
        if (request.getSession(false) != null) {
            request.changeSessionId();
        }
        HttpSession session = request.getSession(true);
        session.setAttribute(USER_ID, user.getId());
        session.setAttribute(USER_NAME, user.getName());
        session.setAttribute(USER_ROLE, user.getRole());
        session.setAttribute(AuthSession.ROLE, user.getRole());
        session.setMaxInactiveInterval(TIMEOUT_SECONDS);
    }

    public void refresh(HttpSession session, User user) {
        if (session != null && user != null) {
            session.setAttribute(USER_NAME, user.getName());
            session.setAttribute(USER_ROLE, user.getRole());
            session.setAttribute(AuthSession.ROLE, user.getRole());
        }
    }

    public void logout(HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
    }
}
