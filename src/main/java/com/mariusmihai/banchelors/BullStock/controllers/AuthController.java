package com.mariusmihai.banchelors.BullStock.controllers;

import com.mariusmihai.banchelors.BullStock.dtos.auth.ChangePasswordDto;
import com.mariusmihai.banchelors.BullStock.dtos.auth.LoginRequest;
import com.mariusmihai.banchelors.BullStock.dtos.auth.RefreshTokenRequest;
import com.mariusmihai.banchelors.BullStock.dtos.auth.RegisterRequest;
import com.mariusmihai.banchelors.BullStock.services.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<Object> registerUser(@RequestBody RegisterRequest registerRequest) {
        return this.authService.registerUser(registerRequest);
    }

    @PostMapping("/login")
    public ResponseEntity<Object> loginUser(@RequestBody LoginRequest loginRequest) {
        return this.authService.loginUser(loginRequest);
    }

    @PostMapping("/logout")
    public ResponseEntity<Object> logoutUser() {
        return this.authService.logoutUser();
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<Object> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        return this.authService.refreshToken(refreshTokenRequest);
    }

    @PutMapping("/password")
    public ResponseEntity<Object> changePassword(@RequestBody ChangePasswordDto passwords) {
        return this.authService.changePassword(passwords);
    }
}
