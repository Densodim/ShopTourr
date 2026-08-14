package com.shoptourr.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public final class MoneyMath {

    private MoneyMath() {}

    public static BigDecimal parse(String raw) {
        String cleaned = raw.trim().replace(" ", "").replace(',', '.');
        return new BigDecimal(cleaned).setScale(2, RoundingMode.HALF_UP);
    }

    public static String format(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    public static BigDecimal scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    public static List<BigDecimal> splitEqually(BigDecimal total, int parts) {
        if (parts <= 0) {
            throw new IllegalArgumentException("parts must be > 0");
        }
        long minor = scale(total).movePointRight(2).longValueExact();
        long base = minor / parts;
        long remainder = minor % parts;
        List<BigDecimal> shares = new ArrayList<>(parts);
        for (int i = 0; i < parts; i++) {
            long amount = base + (i < remainder ? 1 : 0);
            shares.add(BigDecimal.valueOf(amount, 2));
        }
        return shares;
    }
}
