package com.bank_project.custumer;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;



@ExtendWith(MockitoExtension.class)
class CustumerServiceTest {

    @Mock
    private CustumerRepository custumerRepository;

    @InjectMocks
    private CustomerService customerService;






}


