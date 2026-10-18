package com.salon.web;

import com.salon.model.Booking;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public record BookingView(Long id, String lastName, String firstName, String phone, String email,
                          Long serviceId, String serviceName, LocalDate date, String startTime,
                          String endTime, int discountPercent, BigDecimal finalPrice) {

    private static final DateTimeFormatter HM = DateTimeFormatter.ofPattern("HH:mm");

    public static BookingView of(Booking b) {
        return new BookingView(b.getId(), b.getClient().getLastName(), b.getClient().getFirstName(),
                b.getClient().getPhone(), b.getClient().getEmail(), b.getService().getId(),
                b.getService().getName(), b.getVisitDate(), b.getStartTime().format(HM),
                b.getEndTime().format(HM), b.getDiscountPercent(), b.getFinalPrice());
    }
}
