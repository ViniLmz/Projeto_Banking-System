package com.bank_project.user;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    private final AppUserRepository appUserRepository;

    public CurrentUserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    public AppUser getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String username = authentication.getName();

        return appUserRepository.findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException("Authenticated user not found"));
    }

    public Long getCurrentCustomerId() {
        return getCurrentUser().getCustomer().getId();
    }


}

