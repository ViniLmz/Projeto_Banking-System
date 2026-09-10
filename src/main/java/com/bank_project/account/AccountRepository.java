package com.bank_project.account;

import org.springframework.data.jpa.repository.JpaRepository;


public interface AccountRepository extends  JpaRepository<Account, Long> { }