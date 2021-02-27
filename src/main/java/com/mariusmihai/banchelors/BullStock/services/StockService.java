package com.mariusmihai.banchelors.BullStock.services;

import com.mariusmihai.banchelors.BullStock.repositories.StockRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class StockService {

    private final StockRepository stockRepository;

    public StockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    public ResponseEntity<Object> getWinners() {

        Map<String, String> logMap = new HashMap<>();
        try {
            var winners = this.stockRepository.getWinners();
            return new ResponseEntity<>(winners, HttpStatus.OK);
        } catch (Exception e) {
            logMap.put("message", "Something went wrong");
            return new ResponseEntity<>(logMap, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Object> getLosers() {
        Map<String, String> logMap = new HashMap<>();
        try {
            var losers = this.stockRepository.getLosers();
            return new ResponseEntity<>(losers, HttpStatus.OK);
        } catch (Exception e) {
            logMap.put("message", "Something went wrong");
            return new ResponseEntity<>(logMap, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Object> getAllStocks() {
        Map<String, String> logMap = new HashMap<>();
        try {
            var stocks = this.stockRepository.findAllStocks();
            return new ResponseEntity<>(stocks, HttpStatus.OK);
        } catch (Exception e) {
            logMap.put("message", "Something went wrong");
            return new ResponseEntity<>(logMap, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
