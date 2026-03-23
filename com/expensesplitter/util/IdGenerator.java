package com.expensesplitter.util;

public class IdGenerator {

    private static int counter = 0;

    /**
     * Generates a unique ID with the given prefix.
     * Example: generateId("USR") → "USR-1", "USR-2", ...
     * 
     * Replaceable with DB auto-increment or UUID later.
     */
    public static String generateId(String prefix) {
        counter++;
        return prefix + "-" + counter;
    }
}
