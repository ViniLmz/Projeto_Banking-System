package com.bank_project.custumer;

import com.bank_project.exception.CpfAlreadyExistsException;
import com.bank_project.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Service
public class CustomerService {

    private final   CustumerRepository custumerRepository;


    public CustomerService(CustumerRepository custumerRepository) {
        this.custumerRepository = custumerRepository;
    }

    public List<Custumer> findAll()
    {
        return custumerRepository.findAll();
    }

    public Optional <Custumer> findById(Long id)
    {
        return custumerRepository.findById(id);
    }


    public Custumer save (Custumer custumer) {

        if(custumerRepository.existsByCpf(custumer.getCpf())){
            throw new CpfAlreadyExistsException("CPF already exists");
        }

        return custumerRepository.save(custumer);
    }

    @Transactional
    public void delete(Long id) {

        if (!custumerRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Customer not found with id: " + id
            );
        }

        custumerRepository.deleteById(id);
    }

    @Transactional
    public Custumer update(Long id, Custumer custumer) {

        if (!custumerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Customer not found");
        }

        if (custumerRepository.existsByCpfAndIdNot(custumer.getCpf(), id)) {
            throw new CpfAlreadyExistsException("CPF already exists");
        }

        custumer.setId(id);
        return custumerRepository.save(custumer);
    }

}


