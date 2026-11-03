package com.bank_project.custumer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustumerRepository extends JpaRepository<Custumer, Long> {

    boolean existsByCpf (String cpf);
    boolean existsByCpfAndIdNot(String cpf, Long id);
    Optional<Custumer> findByCpf(String cpf);
}