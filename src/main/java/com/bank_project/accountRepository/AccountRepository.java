package com.bank_project.accountRepository;

import com.bank_project.account.Account;
import org.springframework.data.jpa.repository.JpaRepository;



public interface AccountRepository extends JpaRepository <Account, Long> {}
