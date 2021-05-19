package com.mariusmihai.banchelors.BullStock.controllers;

import com.mariusmihai.banchelors.BullStock.dtos.stocks.StockChartRequest;
import com.mariusmihai.banchelors.BullStock.services.StockService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping
    public ResponseEntity<Object> addStock(@RequestBody String stock) {
        return this.stockService.addStock(stock);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getStockDetails(@PathVariable("id") int id) {
        return this.stockService.getStockDetails(id);
    }

    @PostMapping("/screen")
    public ResponseEntity<Object> getStockScreen(@RequestBody StockChartRequest request) {
        return this.stockService.getStockScreen(request);
    }

    @GetMapping("/volume/{symbol}")
    public ResponseEntity<Object> getVolumeBySymbol(@PathVariable("symbol") String symbol) {
        return this.stockService.getVolumeBySymbol(symbol);
    }
}
