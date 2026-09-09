package com.bank_project.customerService;

import com.bank_project.custumerModel.Custumer;
import com.bank_project.customerRepository.CustumerRepository;
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


    public Custumer save (Custumer custumer)
    {
        return custumerRepository.save(custumer);
    }

    @Transactional
    public void delete (Long Id)
    {
        custumerRepository.deleteById(Id);
    }

    @Transactional
    public Custumer update (Long Id, Custumer custumer) {
        if (custumerRepository.existsById(Id))
        {
            custumer.setId(Id);
            return custumerRepository.save(custumer);
        } else throw new RuntimeException("Customer not found");
    }

}


