package com.salon.config;

import com.salon.model.ServiceItem;
import com.salon.repo.ServiceRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

/** Початковий каталог послуг, якщо база порожня. */
@Configuration
public class DataLoader {

    @Bean
    public CommandLineRunner seedServices(ServiceRepository services) {
        return args -> {
            if (services.count() == 0) {
                services.save(new ServiceItem("Чоловіча стрижка", 30, new BigDecimal("300.00")));
                services.save(new ServiceItem("Жіноча стрижка", 30, new BigDecimal("500.00")));
                services.save(new ServiceItem("Укладка", 30, new BigDecimal("400.00")));
                services.save(new ServiceItem("Стрижка і укладка", 60, new BigDecimal("800.00")));
            }
        };
    }
}
