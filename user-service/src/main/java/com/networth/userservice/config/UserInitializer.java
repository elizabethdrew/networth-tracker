package com.networth.userservice.config;

import com.networth.userservice.dto.TaxRate;
import com.networth.userservice.entity.User;
import com.networth.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class UserInitializer {

    @Autowired
    private UserRepository userRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void insertSeedUser() {
        User user = new User();
        user.setUserId(1L); // Assuming user_id is of type Long
        user.setKeycloakId("0c77e0c4-95f4-4704-a24f-ee22deb43609");
        user.setUsername("seeduser");
        user.setEmail("seeduser@example.co.uk");
        user.setActiveUser(true);
        user.setTaxRate(TaxRate.valueOf("BASIC"));
        user.setDateOfBirth(LocalDate.parse("1990-01-01"));
        user.setDateOpened(LocalDateTime.parse("2023-06-06T12:00:00"));
        user.setDateUpdated(LocalDateTime.parse("2023-10-06T12:00:00"));

        // Check if the user already exists to avoid duplicates
        userRepository.findById(user.getUserId()).orElseGet(() -> userRepository.save(user));
    }
}
