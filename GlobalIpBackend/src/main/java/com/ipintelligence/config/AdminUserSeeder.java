package com.ipintelligence.config;

import com.ipintelligence.model.User;
import com.ipintelligence.repo.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class AdminUserSeeder {

    @Bean
    public CommandLineRunner seedAdminUsers(UserRepository userRepository) {
        return args -> {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

            if (!userRepository.existsByEmail("admin1@ipintelligence.com") && !userRepository.existsByUsername("admin1")) {
                User admin1 = new User();
                admin1.setUsername("admin1");
                admin1.setEmail("admin1@ipintelligence.com");
                admin1.setPassword(encoder.encode("Admin123!"));
                admin1.setFirstName("Admin");
                admin1.setLastName("One");
                admin1.setRole("ADMIN");
                admin1.setCreatedAt(java.time.LocalDateTime.now());
                userRepository.save(admin1);
            }

            if (!userRepository.existsByEmail("admin2@ipintelligence.com") && !userRepository.existsByUsername("admin2")) {
                User admin2 = new User();
                admin2.setUsername("admin2");
                admin2.setEmail("admin2@ipintelligence.com");
                admin2.setPassword(encoder.encode("Admin456!"));
                admin2.setFirstName("Admin");
                admin2.setLastName("Two");
                admin2.setRole("ADMIN");
                admin2.setCreatedAt(java.time.LocalDateTime.now());
                userRepository.save(admin2);
            }
        };
    }
}
