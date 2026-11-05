package com.bank_project.JwtPasswordTest;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordTest {

    public static void main(String[] args) {

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String hash = encoder.encode("BankTest123");

        System.out.println(hash);
    }
}