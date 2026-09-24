
package com.bank_project.account;

import com.bank_project.custumer.Custumer;
import com.bank_project.custumer.CustumerRepository;
import com.bank_project.loan.Loan;
import com.bank_project.loan.LoanRequest;
import com.bank_project.loan.LoanService;
import com.bank_project.loan.LoanRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class LoanServiceTest  {
    @Mock
     private AccountRepository accountRepository;

    @Mock
     private LoanRepository loanRepository;


    @Mock
     private CustumerRepository custumerRepository;

    @InjectMocks
    private LoanService loanService;



 @Test
 void testSaveLoan() {

     Custumer custumer = new Custumer();
     custumer.setId(1L);

     Account account = new Account();
     account.setId(2L);
     account.setBalance(new BigDecimal("5000.00"));
     account.setStatus(AccountStatus.ACTIVE);
     account.setCustomer(custumer);

     when(custumerRepository.findById(1L))
             .thenReturn(Optional.of(custumer));

     when(accountRepository.findById(2L))
             .thenReturn(Optional.of(account));

     LoanRequest request =
             new LoanRequest(
                     1L,
                     2L,
                     new BigDecimal("1000.00"),
                     new BigDecimal("2.5"),
                     10
             );

     Loan loan = loanService.save(request);

     assertEquals(custumer, loan.getCustomer());
     assertEquals(account, loan.getAccount());
     assertEquals(new BigDecimal("1000.00"), loan.getAmount());
     assertEquals(new BigDecimal("2.5"), loan.getInterestRate());
     assertEquals(10, loan.getInstallments());
     assertEquals("PENDING", loan.getStatus());
 }



}
