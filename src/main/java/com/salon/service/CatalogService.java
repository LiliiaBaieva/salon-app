package com.salon.service;

import com.salon.model.ServiceItem;
import com.salon.repo.BookingRepository;
import com.salon.repo.ServiceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CatalogService {

    private final ServiceRepository services;
    private final BookingRepository bookings;

    public CatalogService(ServiceRepository services, BookingRepository bookings) {
        this.services = services;
        this.bookings = bookings;
    }

    public List<ServiceItem> list() {
        return services.findAll();
    }

    public ServiceItem create(ServiceItem s) {
        validate(s);
        if (services.existsByName(s.getName().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Послуга з такою назвою вже існує");
        }
        return services.save(new ServiceItem(s.getName().trim(), s.getDurationMin(), s.getPrice()));
    }

    public ServiceItem update(Long id, ServiceItem s) {
        validate(s);
        ServiceItem old = services.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Послугу не знайдено"));
        if (services.existsByNameAndIdNot(s.getName().trim(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Послуга з такою назвою вже існує");
        }
        old.setName(s.getName().trim());
        old.setDurationMin(s.getDurationMin());
        old.setPrice(s.getPrice());
        return services.save(old);
    }

    public void delete(Long id) {
        if (!services.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Послугу не знайдено");
        }
        if (bookings.existsByServiceId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Послугу не можна видалити: на неї є записи");
        }
        services.deleteById(id);
    }

    private void validate(ServiceItem s) {
        if (s.getName() == null || s.getName().isBlank() || s.getName().trim().length() > 100) {
            throw bad("Назва обов'язкова, не більше 100 символів");
        }
        if (s.getDurationMin() != 30 && s.getDurationMin() != 60) {
            throw bad("Тривалість має бути 30 або 60 хвилин");
        }
        if (s.getPrice() == null || s.getPrice().compareTo(BigDecimal.ZERO) <= 0
                || s.getPrice().compareTo(new BigDecimal("99999.99")) > 0) {
            throw bad("Ціна має бути більшою за 0 і не більшою за 99999.99");
        }
    }

    private static ResponseStatusException bad(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
