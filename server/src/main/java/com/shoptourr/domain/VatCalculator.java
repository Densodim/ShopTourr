package com.shoptourr.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class VatCalculator {

    private VatCalculator() {}

    public record Breakdown(
            BigDecimal net,
            BigDecimal vat,
            BigDecimal gross,
            BigDecimal vatRatePercent,
            boolean vatIncluded
    ) {}

    public static Breakdown breakdown(BigDecimal amount, BigDecimal vatRatePercent, boolean vatIncluded) {
        if (vatRatePercent.signum() < 0) {
            throw new IllegalArgumentException("vat rate must be >= 0");
        }
        BigDecimal scaledAmount = MoneyMath.scale(amount);
        BigDecimal scaledRate = vatRatePercent.stripTrailingZeros().scale() < 0
                ? vatRatePercent.setScale(0)
                : vatRatePercent;
        if (vatIncluded) {
            BigDecimal gross = scaledAmount;
            BigDecimal net;
            if (vatRatePercent.signum() == 0) {
                net = gross;
            } else {
                BigDecimal divisor = BigDecimal.ONE.add(vatRatePercent.movePointLeft(2));
                net = gross.divide(divisor, 2, RoundingMode.HALF_UP);
            }
            return new Breakdown(net, gross.subtract(net), gross, MoneyMath.scale(scaledRate), true);
        }
        BigDecimal net = scaledAmount;
        BigDecimal vat = vatRatePercent.signum() == 0
                ? BigDecimal.ZERO.setScale(2)
                : net.multiply(vatRatePercent).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return new Breakdown(net, vat, net.add(vat), MoneyMath.scale(scaledRate), false);
    }
}
