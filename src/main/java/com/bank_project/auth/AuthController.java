
package com.bank_project.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;


    public AuthController(AuthService authService) {
        this.authService = authService;
    }


    @Operation(summary = "Create Login")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login created successfully"),
    })
    @PostMapping("/login")
    public  ResponseEntity<String> login(@RequestBody LoginRequest request){
        String token = authService.login(request);

        return  ResponseEntity.ok(token);
    }

}





