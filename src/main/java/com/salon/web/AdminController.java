package com.salon.web;

import com.salon.model.ServiceItem;
import com.salon.service.BookingService;
import com.salon.service.CatalogService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/** Методи адміністратора (захищені логіном і паролем). */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final CatalogService catalog;
    private final BookingService bookings;

    public AdminController(CatalogService catalog, BookingService bookings) {
        this.catalog = catalog;
        this.bookings = bookings;
    }

    @GetMapping("/bookings")
    public List<BookingView> history(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String client,
            @RequestParam(required = false) Long serviceId) {
        return bookings.history(from, to, client, serviceId);
    }

    @GetMapping("/bookings/{id}")
    public BookingView get(@PathVariable Long id) {
        return bookings.get(id);
    }

    @PostMapping("/bookings")
    @ResponseStatus(HttpStatus.CREATED)
    public BookingView create(@RequestBody BookingRequest request) {
        return bookings.create(request);
    }

    @PutMapping("/bookings/{id}")
    public BookingView update(@PathVariable Long id, @RequestBody BookingRequest request) {
        return bookings.update(id, request);
    }

    @DeleteMapping("/bookings/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        bookings.delete(id);
    }

    @GetMapping("/slots")
    public List<String> slots(@RequestParam Long serviceId,
                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                              @RequestParam(required = false) Long excludeBookingId) {
        return bookings.freeSlots(serviceId, date, excludeBookingId);
    }

    @PostMapping("/services")
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceItem createService(@RequestBody ServiceItem service) {
        return catalog.create(service);
    }

    @PutMapping("/services/{id}")
    public ServiceItem updateService(@PathVariable Long id, @RequestBody ServiceItem service) {
        return catalog.update(id, service);
    }

    @DeleteMapping("/services/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteService(@PathVariable Long id) {
        catalog.delete(id);
    }
}
