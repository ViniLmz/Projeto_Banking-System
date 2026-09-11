package com.bank_project.custumer;

import com.bank_project.account.Account;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.bank_project.account.AccountRequest;

import java.util.List;

@RestController
@RequestMapping("/api/customers")

public class CustomerController {

    private final CustomerService CustomerController;

    public CustomerController(CustomerService customerService) {
        this.CustomerController = customerService;
    }

    @GetMapping
    public ResponseEntity<List<Custumer>> getAll() {
        return ResponseEntity.ok(CustomerController.findAll());
    }

    @GetMapping("/{Id}")
    public ResponseEntity<Custumer> getById(@PathVariable Long Id) {
        return CustomerController.findById(Id).
                map(ResponseEntity::ok).
                orElse(ResponseEntity.notFound().build());
    }

    @PostMapping

    public ResponseEntity<Custumer> create(@Valid @RequestBody Custumer custumer) {
        Custumer savedCustomer = CustomerController.save(custumer);
        return  ResponseEntity.status(HttpStatus.CREATED).body(savedCustomer);
    }


    @PutMapping("/{id}")
    public ResponseEntity<Custumer> update(
       @PathVariable Long id,
       @Valid @RequestBody Custumer custumer){

        Custumer updateCustomer = CustomerController.update(id, custumer);
        return ResponseEntity.ok(updateCustomer);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        CustomerController.delete(id);
        return ResponseEntity.noContent().build();
    }

}