package com.salon.logic;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/** Правила часу запису: робочий день 09:00-20:00, крок 15 хвилин, інтервали не перетинаються. */
public final class SlotCalculator {

    public static final int OPEN_MIN = 9 * 60;
    public static final int CLOSE_MIN = 20 * 60;
    public static final int STEP_MIN = 15;

    private SlotCalculator() {
    }

    /** Інтервал зайнятого часу: [start, end). */
    public record Interval(LocalTime start, LocalTime end) {
    }

    public static int minutes(LocalTime t) {
        return t.getHour() * 60 + t.getMinute();
    }

    /** Чи підходить час початку: крок 15 хв і візит закінчується не пізніше 20:00. */
    public static boolean fitsWorkDay(LocalTime start, int durationMin) {
        int from = minutes(start);
        if (start.getSecond() != 0 || start.getNano() != 0) {
            return false;
        }
        return from >= OPEN_MIN
                && (from - OPEN_MIN) % STEP_MIN == 0
                && from + durationMin <= CLOSE_MIN;
    }

    /** Чи перетинаються два інтервали (кінець одного = початок іншого не вважається перетином). */
    public static boolean overlaps(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        return minutes(start1) < minutes(end2) && minutes(start2) < minutes(end1);
    }

    /** Усі вільні часи початку для послуги заданої тривалості. */
    public static List<LocalTime> freeStarts(int durationMin, List<Interval> busy) {
        List<LocalTime> result = new ArrayList<>();
        for (int m = OPEN_MIN; m + durationMin <= CLOSE_MIN; m += STEP_MIN) {
            LocalTime start = LocalTime.of(m / 60, m % 60);
            LocalTime end = start.plusMinutes(durationMin);
            boolean free = true;
            for (Interval b : busy) {
                if (overlaps(start, end, b.start(), b.end())) {
                    free = false;
                    break;
                }
            }
            if (free) {
                result.add(start);
            }
        }
        return result;
    }
}
