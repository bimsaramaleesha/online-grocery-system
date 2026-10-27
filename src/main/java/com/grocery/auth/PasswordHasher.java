package com.grocery.auth;

import com.grocery.user.service.PasswordUtil;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Passwords are never saved as plain text. We save  salt$hash  instead.
 *  - salt: random bytes, so two users with the same password get different hashes
 *  - hash: PBKDF2 (a standard, slow hash that ships with Java, no extra library needed)
 */
public final class PasswordHasher {

    private static final int ITERATIONS = 65_000;
    private static final int KEY_BITS = 256;

    private PasswordHasher() { }

    /** Turns a password into the text we store in users.txt. */
    public static String hash(String password) {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return encode(salt) + "$" + encode(derive(password, salt));
    }

    /** True when the typed password matches the stored salt$hash (or iterations:salt:hash from UserService). */
    public static boolean matches(String password, String stored) {
        if (password == null || stored == null) {
            return false;
        }
        if (stored.contains(":")) {
            return PasswordUtil.verify(password, stored);
        }
        if (!stored.contains("$")) {
            return false;
        }
        try {
            String[] parts = stored.split("\\$");
            if (parts.length != 2) {
                return false;
            }
            byte[] salt = Base64.getUrlDecoder().decode(parts[0]);
            byte[] expected = Base64.getUrlDecoder().decode(parts[1]);
            return MessageDigest.isEqual(expected, derive(password, salt));   // constant-time compare
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] derive(String password, byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_BITS);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Password hashing is not available", e);
        }
    }

    private static String encode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
