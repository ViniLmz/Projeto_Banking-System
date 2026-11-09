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

    private final CustomerMapper customerMapper;
    private final CustomerService customerService;

    public CustomerController(CustomerMapper customerMapper, CustomerService customerService) {
        this.customerMapper = customerMapper;
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
    public ResponseEntity<List<CustomerResponse>> getAll() {
        List<CustomerResponse> customers =
                customerService.findAll()
                        .stream()
                        .map(customerMapper::toResponse)
                        .toList();

        return ResponseEntity.ok(customers);
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
    public ResponseEntity<CustomerResponse> getById(@PathVariable Long id) {

        return customerService.findById(id)
                .map(customerMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
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
    public ResponseEntity<CustomerResponse> create(
            @Valid @RequestBody CustomerRequest request) {

        Custumer customer = customerMapper.toEntity(request);

        Custumer savedCustomer = customerService.save(customer);

        CustomerResponse response = customerMapper.toResponse(savedCustomer);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
    public ResponseEntity<CustomerResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequest request) {

        Custumer customer = customerMapper.toEntity(request);

        Custumer updated = customerService.update(id, customer);

        CustomerResponse response = customerMapper.toResponse(updated);

        return ResponseEntity.ok(response);
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