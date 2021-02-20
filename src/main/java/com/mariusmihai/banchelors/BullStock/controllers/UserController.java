package com.mariusmihai.banchelors.BullStock.controllers;

import com.mariusmihai.banchelors.BullStock.dtos.auth.LoginRequest;
import com.mariusmihai.banchelors.BullStock.dtos.auth.RefreshTokenRequest;
import com.mariusmihai.banchelors.BullStock.dtos.auth.RegisterRequest;
import com.mariusmihai.banchelors.BullStock.dtos.stocks.UserDto;
import com.mariusmihai.banchelors.BullStock.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<UserDto>> geAllUsers() {
        return this.userService.getAllUsers();
    }

    @PostMapping("/register")
    public ResponseEntity<Object> registerUser(@RequestBody RegisterRequest registerRequest) {
        return this.userService.registerUser(registerRequest);
    }

    @PostMapping("/login")
    public ResponseEntity<Object> loginUser(@RequestBody LoginRequest loginRequest) {
        return this.userService.loginUser(loginRequest);
    }

    @PostMapping("/logout/{userId}")
    public ResponseEntity<Object> logoutUser(@PathVariable Long userId) {
        return this.userService.logoutUser(userId);
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<Object> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        return this.userService.refreshToken(refreshTokenRequest);
    }
}
