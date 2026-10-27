package com.grocery.user.service;

import com.grocery.auth.PasswordHasher;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public final class PasswordUtil {

    private static final int ITERATIONS = 210000;
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
        if (savedHash.contains("$")) {
            return PasswordHasher.matches(password, savedHash);
        }
        String[] parts = savedHash.split(":", -1);
        if (parts.length != 3) {
            return false;
        }

        int iterations;
        try {
            iterations = Integer.parseInt(parts[0]);
        } catch (NumberFormatException e) {
            return false;
        }

        if (iterations < 100000 || iterations > 1000000) {
            return false;
        }

        try {
            byte[] salt = Base64.getDecoder().decode(parts[1]);
            byte[] expected = Base64.getDecoder().decode(parts[2]);
            if (salt.length != SALT_LENGTH || expected.length != KEY_LENGTH / 8) {
                return false;
            }
            byte[] actual = derive(password, salt, iterations);
            return MessageDigest.isEqual(expected, actual);
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
