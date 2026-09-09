package com.bank_project.customerRepository;

import com.bank_project.custumerModel.Custumer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustumerRepository extends JpaRepository<Custumer, Long> {

}