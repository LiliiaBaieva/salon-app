package com.salon.service;

import com.salon.logic.DiscountCalculator;
import com.salon.logic.SlotCalculator;
import com.salon.logic.SlotCalculator.Interval;
import com.salon.model.Booking;
import com.salon.model.Client;
import com.salon.model.ServiceItem;
import com.salon.repo.BookingRepository;
import com.salon.repo.ClientRepository;
import com.salon.repo.ServiceRepository;
import com.salon.web.BookingRequest;
import com.salon.web.BookingView;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class BookingService {

    private static final Pattern PHONE = Pattern.compile("^\\+380\\d{9}$");

    private final BookingRepository bookings;
    private final ClientRepository clients;
    private final ServiceRepository services;

    public BookingService(BookingRepository bookings, ClientRepository clients, ServiceRepository services) {
        this.bookings = bookings;
        this.clients = clients;
        this.services = services;
    }

    /** Вільні часи початку на дату для послуги. */
    @Transactional(readOnly = true)
    public List<String> freeSlots(Long serviceId, LocalDate date, Long excludeBookingId) {
        ServiceItem service = findService(serviceId);
        if (date == null || date.isBefore(LocalDate.now())) {
            throw bad("Дата не може бути в минулому");
        }
        List<Interval> busy = busyIntervals(date, excludeBookingId);
        List<String> result = new ArrayList<>();
        for (LocalTime t : SlotCalculator.freeStarts(service.getDurationMin(), busy)) {
            result.add(t.toString());
        }
        return result;
    }

    @Transactional
    public BookingView create(BookingRequest r) {
        Booking b = new Booking();
        fill(b, r, true);
        return BookingView.of(bookings.save(b));
    }

    @Transactional
    public BookingView update(Long id, BookingRequest r) {
        Booking b = bookings.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Запис не знайдено"));
        fill(b, r, false);
        return BookingView.of(bookings.save(b));
    }

    @Transactional
    public void delete(Long id) {
        if (!bookings.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Запис не знайдено");
        }
        bookings.deleteById(id);
    }

    @Transactional(readOnly = true)
    public BookingView get(Long id) {
        return bookings.findById(id).map(BookingView::of)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Запис не знайдено"));
    }

    /** Історія записів з фільтрами: період, клієнт (прізвище, ім'я або телефон), послуга. */
    @Transactional(readOnly = true)
    public List<BookingView> history(LocalDate from, LocalDate to, String client, Long serviceId) {
        String q = client == null ? "" : client.trim().toLowerCase();
        return bookings.findAll().stream()
                .filter(b -> from == null || !b.getVisitDate().isBefore(from))
                .filter(b -> to == null || !b.getVisitDate().isAfter(to))
                .filter(b -> serviceId == null || b.getService().getId().equals(serviceId))
                .filter(b -> q.isEmpty() || clientText(b).contains(q))
                .sorted(Comparator.comparing(Booking::getVisitDate).thenComparing(Booking::getStartTime))
                .map(BookingView::of)
                .toList();
    }

    private String clientText(Booking b) {
        Client c = b.getClient();
        return (c.getLastName() + " " + c.getFirstName() + " " + c.getPhone()).toLowerCase();
    }

    private void fill(Booking b, BookingRequest r, boolean isNew) {
        validate(r);
        ServiceItem service = findService(r.serviceId());
        if (!SlotCalculator.fitsWorkDay(r.time(), service.getDurationMin())) {
            throw bad("Час має бути кратний 15 хвилин, а візит має закінчуватися не пізніше 20:00");
        }
        LocalTime end = r.time().plusMinutes(service.getDurationMin());
        for (Interval i : busyIntervals(r.date(), b.getId())) {
            if (SlotCalculator.overlaps(r.time(), end, i.start(), i.end())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Цей час уже зайнятий");
            }
        }

        Client client = clients.findByPhone(r.phone()).orElseGet(Client::new);
        client.setPhone(r.phone());
        client.setLastName(r.lastName().trim());
        client.setFirstName(r.firstName().trim());
        client.setEmail(r.email() == null || r.email().isBlank() ? null : r.email().trim());
        client = clients.save(client);

        int percent = isNew
                ? DiscountCalculator.percentFor(bookings.countByClientId(client.getId()))
                : b.getDiscountPercent();

        b.setClient(client);
        b.setService(service);
        b.setVisitDate(r.date());
        b.setStartTime(r.time());
        b.setEndTime(end);
        b.setDiscountPercent(percent);
        b.setFinalPrice(DiscountCalculator.finalPrice(service.getPrice(), percent));
    }

    private List<Interval> busyIntervals(LocalDate date, Long excludeBookingId) {
        List<Interval> busy = new ArrayList<>();
        for (Booking x : bookings.findByVisitDate(date)) {
            if (excludeBookingId == null || !excludeBookingId.equals(x.getId())) {
                busy.add(new Interval(x.getStartTime(), x.getEndTime()));
            }
        }
        return busy;
    }

    private void validate(BookingRequest r) {
        if (blank(r.lastName()) || r.lastName().trim().length() > 50) {
            throw bad("Прізвище обов'язкове, не більше 50 символів");
        }
        if (blank(r.firstName()) || r.firstName().trim().length() > 50) {
            throw bad("Ім'я обов'язкове, не більше 50 символів");
        }
        if (r.phone() == null || !PHONE.matcher(r.phone()).matches()) {
            throw bad("Телефон має бути у форматі +380XXXXXXXXX");
        }
        if (!blank(r.email()) && (!r.email().contains("@") || r.email().length() > 100)) {
            throw bad("Некоректний email");
        }
        if (r.serviceId() == null) {
            throw bad("Оберіть послугу");
        }
        if (r.date() == null || r.date().isBefore(LocalDate.now())) {
            throw bad("Дата не може бути в минулому");
        }
        if (r.time() == null) {
            throw bad("Оберіть час");
        }
    }

    private ServiceItem findService(Long id) {
        if (id == null) {
            throw bad("Оберіть послугу");
        }
        return services.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Послугу не знайдено"));
    }

    private static boolean blank(String s) {
        return s == null || s.isBlank();
    }

    private static ResponseStatusException bad(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
