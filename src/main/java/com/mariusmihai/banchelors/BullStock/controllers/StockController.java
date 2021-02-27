package com.mariusmihai.banchelors.BullStock.controllers;

import com.mariusmihai.banchelors.BullStock.services.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stock")
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping("/winners")
    public ResponseEntity<Object> getWinners() {
        return this.stockService.getWinners();
    }

    @GetMapping("/losers")
    public ResponseEntity<Object> getLosers() {
        return this.stockService.getLosers();
    }

    @GetMapping()
    public ResponseEntity<Object> getAllStocks() {
        return this.stockService.getAllStocks();
    }

}
