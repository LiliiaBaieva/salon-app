package com.salon.repo;

import com.salon.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByVisitDate(LocalDate visitDate);
    long countByClientId(Long clientId);
    boolean existsByServiceId(Long serviceId);
}
