package com.bank_project.custumer;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CustumerRepository extends JpaRepository<Custumer, Long> {

    boolean existsByCpf (String cpf);
    boolean existsByCpfAndIdNot(String cpf, Long id);
}