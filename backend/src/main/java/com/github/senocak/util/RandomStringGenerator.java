package com.github.senocak.util;

import java.security.SecureRandom;
import java.util.Random;

public class RandomStringGenerator {
    private final int length;
    private final char[] symbols;
    private final char[] buf;

    public RandomStringGenerator(int length) {
        this.length = length;
        this.symbols = (UPPER + UPPER.toLowerCase() + DIGITS).toCharArray();
        this.buf = new char[length];
    }

    public String next() {
        for (int i = 0; i < buf.length; i++) {
            buf[i] = symbols[random.nextInt(symbols.length)];
        }
        return new String(buf);
    }
    private final Random random = new SecureRandom();
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
}
