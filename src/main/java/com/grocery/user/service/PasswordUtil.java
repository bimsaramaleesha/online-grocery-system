package com.grocery.user.service;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public final class PasswordUtil {

    private static final int ITERATIONS = 210_000;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;

    private PasswordUtil() { }

    public static String hash(String password) {
        byte[] salt = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(salt);
        byte[] hash = derive(password, salt, ITERATIONS);
        return ITERATIONS + ":"
                + Base64.getEncoder().encodeToString(salt) + ":"
                + Base64.getEncoder().encodeToString(hash);
    }

    public static boolean verify(String password, String savedHash) {
        if (password == null || savedHash == null) {
            return false;
        }
        if (savedHash.indexOf('$') > 0 && savedHash.indexOf(':') < 0) {
            return verifyLegacyHash(password, savedHash);
        }
        String[] parts = savedHash.split(":", -1);
        if (parts.length != 3) {
            return false;
        }
        try {
            int iterations = Integer.parseInt(parts[0]);
            if (iterations < 10_000 || iterations > 1_000_000) {
                return false;
            }
            byte[] salt = Base64.getDecoder().decode(parts[1]);
            byte[] expected = Base64.getDecoder().decode(parts[2]);
            if (salt.length != SALT_LENGTH || expected.length != KEY_LENGTH / 8) {
                return false;
            }
            return MessageDigest.isEqual(expected, derive(password, salt, iterations));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /** Verifies legacy salt$hash format (PBKDF2, 65,000 rounds, Base64Url). */
    private static boolean verifyLegacyHash(String password, String savedHash) {
        String[] parts = savedHash.split("\\$");
        if (parts.length != 2) {
            return false;
        }
        try {
            byte[] salt = Base64.getUrlDecoder().decode(parts[0]);
            byte[] expected = Base64.getUrlDecoder().decode(parts[1]);
            return MessageDigest.isEqual(expected, derive(password, salt, 65_000));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] derive(String password, byte[] salt, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, KEY_LENGTH);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec)
                    .getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Password hashing is not available.", e);
        } finally {
            spec.clearPassword();
        }
    }
}
