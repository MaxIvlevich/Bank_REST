package com.example.bankcards.config;

import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${initial.user.login}")
    private String adminUsername;

    @Value("${initial.user.pass}")
    private String adminPassword;

    /**
     * This method is executed upon application startup.
     * It checks for the existence of the admin user and creates one if necessary.
     */
    @Override
    @Transactional
    public void run(String... args)  {

        if (userRepository.existsByUsername(adminUsername)) {
            log.info("INITIALIZATION: Admin user '{}' already exists. Skipping creation.", adminUsername);
        } else {
            log.info("INITIALIZATION: Admin user '{}' not found. Creating new admin user.", adminUsername);

            User admin = new User();
            admin.setUsername(adminUsername);
            admin.setPassword(passwordEncoder.encode(adminPassword));

            admin.setRoles(Set.of(Role.ROLE_ADMIN, Role.ROLE_USER));

            admin.setEnabled(true);
            admin.setAccountNonExpired(true);
            admin.setAccountNonLocked(true);
            admin.setCredentialsNonExpired(true);

            userRepository.save(admin);
            log.info("INITIALIZATION: Admin user '{}' created successfully.", adminUsername);
        }
    }
}
