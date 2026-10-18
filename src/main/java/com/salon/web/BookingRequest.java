package com.salon.web;

import java.time.LocalDate;
import java.time.LocalTime;

public record BookingRequest(String lastName, String firstName, String phone, String email,
                             Long serviceId, LocalDate date, LocalTime time) {
}
