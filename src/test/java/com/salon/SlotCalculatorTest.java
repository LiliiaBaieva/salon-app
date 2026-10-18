package com.salon;

import com.salon.logic.SlotCalculator;
import com.salon.logic.SlotCalculator.Interval;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SlotCalculatorTest {

    @Test
    void emptyDayHasAllSlotsForThirtyMinuteService() {
        List<LocalTime> slots = SlotCalculator.freeStarts(30, List.of());
        assertEquals(43, slots.size());
        assertEquals(LocalTime.of(9, 0), slots.get(0));
        assertEquals(LocalTime.of(19, 30), slots.get(slots.size() - 1));
    }

    @Test
    void sixtyMinuteServiceMustEndByTwenty() {
        List<LocalTime> slots = SlotCalculator.freeStarts(60, List.of());
        assertEquals(LocalTime.of(19, 0), slots.get(slots.size() - 1));
    }

    @Test
    void busyIntervalRemovesOverlappingStarts() {
        List<Interval> busy = List.of(new Interval(LocalTime.of(10, 0), LocalTime.of(10, 30)));
        List<LocalTime> slots = SlotCalculator.freeStarts(30, busy);
        assertFalse(slots.contains(LocalTime.of(9, 45)));
        assertFalse(slots.contains(LocalTime.of(10, 0)));
        assertFalse(slots.contains(LocalTime.of(10, 15)));
        assertTrue(slots.contains(LocalTime.of(9, 30)));
        assertTrue(slots.contains(LocalTime.of(10, 30)));
    }

    @Test
    void touchingIntervalsDoNotOverlap() {
        assertFalse(SlotCalculator.overlaps(LocalTime.of(10, 0), LocalTime.of(10, 30),
                LocalTime.of(10, 30), LocalTime.of(11, 0)));
        assertTrue(SlotCalculator.overlaps(LocalTime.of(10, 0), LocalTime.of(10, 30),
                LocalTime.of(10, 15), LocalTime.of(10, 45)));
    }

    @Test
    void workDayRules() {
        assertTrue(SlotCalculator.fitsWorkDay(LocalTime.of(9, 0), 30));
        assertTrue(SlotCalculator.fitsWorkDay(LocalTime.of(19, 30), 30));
        assertFalse(SlotCalculator.fitsWorkDay(LocalTime.of(8, 45), 30));
        assertFalse(SlotCalculator.fitsWorkDay(LocalTime.of(19, 45), 30));
        assertFalse(SlotCalculator.fitsWorkDay(LocalTime.of(19, 30), 60));
        assertFalse(SlotCalculator.fitsWorkDay(LocalTime.of(10, 10), 30));
    }
}
