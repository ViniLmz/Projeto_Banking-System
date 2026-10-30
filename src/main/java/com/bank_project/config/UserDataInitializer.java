package com.bank_project.config;

import com.bank_project.user.AppUser;
import com.bank_project.user.AppUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class UserDataInitializer {

    @Bean
    public CommandLineRunner createInitialUser(
            AppUserRepository repository,
            PasswordEncoder passwordEncoder) {

        return args -> {
            if (repository.findByUsername("vinicius").isEmpty()) {

                AppUser user = new AppUser(
                        "vinicius",
                        passwordEncoder.encode("BankTest123!")
                );

                repository.save(user);
            }
        };
    }
}