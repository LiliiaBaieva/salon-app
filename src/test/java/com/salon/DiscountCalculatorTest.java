package com.salon;

import com.salon.logic.DiscountCalculator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DiscountCalculatorTest {

    @Test
    void noDiscountForFirstFourBookings() {
        for (long previous = 0; previous <= 3; previous++) {
            assertEquals(0, DiscountCalculator.percentFor(previous));
        }
    }

    @Test
    void discountFromFifthBooking() {
        assertEquals(3, DiscountCalculator.percentFor(4));
        assertEquals(3, DiscountCalculator.percentFor(10));
    }

    @Test
    void finalPriceIsRoundedToKopecks() {
        assertEquals(new BigDecimal("291.00"), DiscountCalculator.finalPrice(new BigDecimal("300.00"), 3));
        assertEquals(new BigDecimal("776.00"), DiscountCalculator.finalPrice(new BigDecimal("800.00"), 3));
        assertEquals(new BigDecimal("300.00"), DiscountCalculator.finalPrice(new BigDecimal("300.00"), 0));
        assertEquals(new BigDecimal("12.13"), DiscountCalculator.finalPrice(new BigDecimal("12.50"), 3));
    }
}
