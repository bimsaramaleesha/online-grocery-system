package com.grocery.auth;

import jakarta.servlet.http.HttpSession;

/**
 * What we remember about the logged-in user, kept in the HTTP session.
 * "userId" is the key the Order/Cart module already reads, so carts and orders
 * automatically belong to whoever is logged in.
 */
public final class AuthSession {

    public static final String USER_ID = "userId";
    public static final String ROLE = "role";          // "CUSTOMER" or "ADMIN" (the navbar reads this)
    public static final String USER_NAME = "userName";

    private AuthSession() { }

    public static void signIn(HttpSession session, Account account) {
        session.setAttribute(USER_ID, account.getId());
        session.setAttribute(ROLE, account.getRole().name());
        session.setAttribute(USER_NAME, account.getFullName());
    }

    /** null = not logged in (a guest). */
    public static Role roleOf(HttpSession session) {
        if (session == null || session.getAttribute(ROLE) == null) {
            return null;
        }
        return Role.valueOf((String) session.getAttribute(ROLE));
    }

    public static String userIdOf(HttpSession session) {
        return session == null ? null : (String) session.getAttribute(USER_ID);
    }
}
