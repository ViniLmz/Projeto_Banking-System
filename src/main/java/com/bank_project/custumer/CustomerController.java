package com.bank_project.custumer;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Operation(
            summary = "List all customers",
            description = "Returns a list of all customers registered in the system"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Customers retrieved successfully"
            )
    })
    @GetMapping
    public ResponseEntity<List<Custumer>> getAll() {
        return ResponseEntity.ok(customerService.findAll());
    }

    @Operation(
            summary = "Get customer by ID",
            description = "Returns a customer by their ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Customer found successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Customer not found"
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<Custumer> getById(@PathVariable Long id) {
        return customerService.findById(id).
                map(ResponseEntity::ok).
                orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Create a customer",
            description = "Creates a new customer"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Customer created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid customer data"
            )
    })
    @PostMapping
    public ResponseEntity<Custumer> create(@Valid @RequestBody Custumer custumer) {
        Custumer savedCustomer = customerService.save(custumer);
        return  ResponseEntity.status(HttpStatus.CREATED).body(savedCustomer);
    }


    @Operation(
            summary = "Update customer",
            description = "Updates an existing customer's information using their ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Customer updated successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid customer data"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Customer not found"
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<Custumer> update(
       @PathVariable Long id,
       @Valid @RequestBody Custumer custumer){

        Custumer updateCustomer = customerService.update(id, custumer);
        return ResponseEntity.ok(updateCustomer);
    }

    @Operation(
            summary = "Delete customer",
            description = "Deletes a customer using their ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Customer deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Customer not found"
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        customerService.delete(id);
        return ResponseEntity.noContent().build();
    }

}