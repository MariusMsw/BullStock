package com.mariusmihai.banchelors.BullStock.controllers;

import com.mariusmihai.banchelors.BullStock.dtos.stocks.UserDto;
import com.mariusmihai.banchelors.BullStock.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<UserDto>> geAllUsers() {
        return this.userService.getAllUsers();
    }

    @GetMapping
    public ResponseEntity<Object> getLoggedInUser() {
        return this.userService.getLoggedInUser();
    }

    @GetMapping("/statistics")
    public ResponseEntity<Object> getStatistics() {
        return this.userService.getStatistics();
    }

    @GetMapping("/favorite")
    public ResponseEntity<Object> getFavoriteStocks() {
        return this.userService.getFavorite();
    }

    @GetMapping("/portofolio")
    public ResponseEntity<Object> getPortofolioStocks() {
        return this.userService.getPortofolioStocks();
    }

}
