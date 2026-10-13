package com.bank_project.custumer;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public ResponseEntity<List<Custumer>> getAll() {
        return ResponseEntity.ok(customerService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Custumer> getById(@PathVariable Long id) {
        return customerService.findById(id).
                map(ResponseEntity::ok).
                orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Custumer> create(@Valid @RequestBody Custumer custumer) {
        Custumer savedCustomer = customerService.save(custumer);
        return  ResponseEntity.status(HttpStatus.CREATED).body(savedCustomer);
    }


    @PutMapping("/{id}")
    public ResponseEntity<Custumer> update(
       @PathVariable Long id,
       @Valid @RequestBody Custumer custumer){

        Custumer updateCustomer = customerService.update(id, custumer);
        return ResponseEntity.ok(updateCustomer);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        customerService.delete(id);
        return ResponseEntity.noContent().build();
    }

}