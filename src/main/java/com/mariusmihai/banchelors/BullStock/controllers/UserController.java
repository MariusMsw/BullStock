package com.mariusmihai.banchelors.BullStock.controllers;

import com.mariusmihai.banchelors.BullStock.dtos.CashDto;
import com.mariusmihai.banchelors.BullStock.dtos.stocks.TradeStockDto;
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

    @GetMapping("/portfolio-metadata")
    public ResponseEntity<Object> getPortfolioMetadata() {
        return this.userService.getPortfolioMetadata();
    }

    @GetMapping("/favorite")
    public ResponseEntity<Object> getFavoriteStocks() {
        return this.userService.getFavorite();
    }

    @GetMapping("/portofolio")
    public ResponseEntity<Object> getPortofolioStocks() {
        return this.userService.getPortofolioStocks();
    }

    @GetMapping("/history")
    public ResponseEntity<Object> getHistory() {
        return this.userService.getHistory();
    }

    @PostMapping("/favorite/{symbol}")
    public ResponseEntity<Object> changeFavoriteStatus(@PathVariable("symbol") String symbol) {
        return this.userService.changeFavoriteStatus(symbol);
    }

    @PostMapping("/stock-buy")
    public ResponseEntity<Object> buyStock(@RequestBody TradeStockDto request) {
        return this.userService.buyStock(request);
    }

    @PostMapping("/stock-sell")
    public ResponseEntity<Object> sellStock(@RequestBody TradeStockDto request) {
        return this.userService.sellStock(request);
    }

    @PostMapping("/deposit")
    public ResponseEntity<Object> depositMoney(@RequestBody CashDto request) {
        return this.userService.depositMoney(request);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<Object> withdrawMoney(@RequestBody CashDto request) {
        return this.userService.withdrawMoney(request);
    }

}
