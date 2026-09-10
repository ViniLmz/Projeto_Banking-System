package com.bank_project;

import com.bank_project.custumerModel.Custumer;
import com.bank_project.customerService.CustomerService;
import jakarta.validation.Valid;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")

public class customerController {

    private final CustomerService customerService;

    public customerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public ResponseEntity<List<Custumer>> getAll() {
        return ResponseEntity.ok(customerService.findAll());
    }

    @GetMapping("/{Id}")
    public ResponseEntity<Custumer> getById(@PathVariable Long Id) {
        return customerService.findById(Id).
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