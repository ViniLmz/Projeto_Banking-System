
package com.bank_project.config;

import com.bank_project.custumer.Custumer;
import com.bank_project.custumer.CustumerRepository;
import com.bank_project.user.AppUser;
import com.bank_project.user.AppUserRepository;
import com.bank_project.user.UserRole;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class UserDataInitializer {

    @Bean
    public CommandLineRunner createInitialUser(
            AppUserRepository repository,
            CustumerRepository custumerRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            if (repository.findByUsername("vinicius").isEmpty()) {

                Custumer customer = custumerRepository.findByCpf("12345678900")
                        .orElseThrow(() ->
                                new RuntimeException("Customer not found"));

                AppUser user = new AppUser(
                        "vinicius",
                        passwordEncoder.encode("BankTest123!"),
                        customer,
                        UserRole.CUSTOMER
                );

                repository.save(user);
            }

            if (repository.findByUsername("admin").isEmpty()) {

                AppUser admin = new AppUser(
                        "admin",
                        passwordEncoder.encode("AdminTest123!"),
                        null,
                        UserRole.ADMIN
                );

                repository.save(admin);
            }
        };
    }
}
