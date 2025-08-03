package com.example.bankcards;

import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Profile("test")
public class TestDataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        User admin = new User();
        admin.setUsername("test-admin");
        admin.setPassword(passwordEncoder.encode("test-password"));
        admin.setRoles(Set.of(Role.ROLE_ADMIN, Role.ROLE_USER));
        admin.setEnabled(true);
        userRepository.save(admin);
    }
}
