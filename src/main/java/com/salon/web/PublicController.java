package com.salon.web;

import com.salon.model.ServiceItem;
import com.salon.service.BookingService;
import com.salon.service.CatalogService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/** Відкриті методи для відвідувачів сайту. */
@RestController
@RequestMapping("/api")
public class PublicController {

    private final CatalogService catalog;
    private final BookingService bookings;

    public PublicController(CatalogService catalog, BookingService bookings) {
        this.catalog = catalog;
        this.bookings = bookings;
    }

    @GetMapping("/services")
    public List<ServiceItem> services() {
        return catalog.list();
    }

    @GetMapping("/slots")
    public List<String> slots(@RequestParam Long serviceId,
                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return bookings.freeSlots(serviceId, date, null);
    }

    @PostMapping("/bookings")
    @ResponseStatus(HttpStatus.CREATED)
    public BookingView book(@RequestBody BookingRequest request) {
        return bookings.create(request);
    }
}
