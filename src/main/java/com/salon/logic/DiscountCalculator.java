package com.salon.logic;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Знижка 3% з п'ятого запису клієнта. */
public final class DiscountCalculator {

    public static final int DISCOUNT_PERCENT = 3;
    public static final int FROM_BOOKING_NUMBER = 5;

    private DiscountCalculator() {
    }

    /** previousBookings - скільки записів у клієнта вже є до нового. */
    public static int percentFor(long previousBookings) {
        return previousBookings + 1 >= FROM_BOOKING_NUMBER ? DISCOUNT_PERCENT : 0;
    }

    public static BigDecimal finalPrice(BigDecimal price, int percent) {
        return price.multiply(BigDecimal.valueOf(100 - percent))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
}
