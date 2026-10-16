
        package com.bank_project.repository;

import com.bank_project.account.Account;
import com.bank_project.account.AccountRepository;
import com.bank_project.account.AccountStatus;
import com.bank_project.account.AccountType;
import com.bank_project.custumer.Custumer;
import com.bank_project.custumer.CustumerRepository;
import com.bank_project.transfer.Transfer;
import com.bank_project.transfer.TransferRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class TransferRepositoryTest {

    @Autowired
    private TransferRepository transferRepository;

    @Autowired
    private CustumerRepository custumerRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Test
    void shouldSaveAndFindTransferById() {

        Custumer custumer = createCustomer("12345678");

        Account sourceAccount =
                createAccount(custumer, "11111");

        Account targetAccount =
                createAccount(custumer, "22222");

        Transfer transfer = new Transfer(
                null,
                sourceAccount,
                targetAccount,
                new BigDecimal("500.00"),
                LocalDateTime.now(),
                "COMPLETED"
        );

        Transfer savedTransfer =
                transferRepository.save(transfer);

        Optional<Transfer> foundTransfer =
                transferRepository.findById(savedTransfer.getId());

        assertTrue(foundTransfer.isPresent());

        assertEquals(
                new BigDecimal("500.00"),
                foundTransfer.get().getAmount()
        );

        assertEquals(
                "COMPLETED",
                foundTransfer.get().getStatus()
        );

        assertEquals(
                sourceAccount.getId(),
                foundTransfer.get().getSourceAccount().getId()
        );

        assertEquals(
                targetAccount.getId(),
                foundTransfer.get().getTargetAccount().getId()
        );
    }

    @Test
    void shouldFindAllTransfers() {

        Custumer custumer = createCustomer("87654321");

        Account sourceAccount =
                createAccount(custumer, "33333");

        Account targetAccount =
                createAccount(custumer, "44444");

        Transfer transfer = new Transfer(
                null,
                sourceAccount,
                targetAccount,
                new BigDecimal("200.00"),
                LocalDateTime.now(),
                "COMPLETED"
        );

        transferRepository.save(transfer);

        List<Transfer> transfers =
                transferRepository.findAll();

        assertFalse(transfers.isEmpty());
        assertEquals(1, transfers.size());

        assertEquals(
                new BigDecimal("200.00"),
                transfers.get(0).getAmount()
        );
    }

    private Custumer createCustomer(String cpf) {

        Custumer custumer = new Custumer();
        custumer.setCpf(cpf);
        custumer.setName("Vinicius");
        custumer.setEmail("test" + cpf + "@hotmail.com");

        return custumerRepository.save(custumer);
    }

    private Account createAccount(
            Custumer custumer,
            String accountNumber
    ) {

        Account account = new Account();
        account.setCustomer(custumer);
        account.setBalance(new BigDecimal("4000.00"));
        account.setStatus(AccountStatus.ACTIVE);
        account.setAccountNumber(accountNumber);
        account.setAccountType(AccountType.CHECKING);

        return accountRepository.save(account);
    }
}
