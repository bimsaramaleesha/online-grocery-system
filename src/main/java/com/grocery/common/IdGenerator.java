package com.grocery.common;

import java.util.List;

/**
 * Creates the next ID for a module, e.g. U001, U002 ... or ITM001, ITM002 ...
 *
 * Agreed prefixes:
 *   U = user, ITM = item, SUP = supplier, ORD = order, CRT = cart,
 *   PRM = promo, REV = review, CMP = complaint
 *
 * Usage:  String id = IdGenerator.nextId("ITM", existingIds);
 */
public final class IdGenerator {

    private IdGenerator() {
    }

    public static String nextId(String prefix, List<String> existingIds) {
        int max = 0;
        for (String id : existingIds) {
            if (id != null && id.startsWith(prefix)) {
                try {
                    max = Math.max(max, Integer.parseInt(id.substring(prefix.length())));
                } catch (NumberFormatException ignored) {
                    // not a valid ID, skip it
                }
            }
        }
        return String.format("%s%03d", prefix, max + 1);
    }
}
