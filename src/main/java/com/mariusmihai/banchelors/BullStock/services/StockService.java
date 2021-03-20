package com.mariusmihai.banchelors.BullStock.services;

import com.mariusmihai.banchelors.BullStock.dtos.stocks.BasicStockDto;
import com.mariusmihai.banchelors.BullStock.models.Stock;
import com.mariusmihai.banchelors.BullStock.models.User;
import com.mariusmihai.banchelors.BullStock.models.UserStatistics;
import com.mariusmihai.banchelors.BullStock.repositories.StockRepository;
import com.mariusmihai.banchelors.BullStock.repositories.UserRepository;
import com.mariusmihai.banchelors.BullStock.repositories.UserStatisticsRepository;
import com.mariusmihai.banchelors.BullStock.repositories.UserStockPortofolioRepository;
import com.mariusmihai.banchelors.BullStock.utils.Helpers;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class StockService {

    private final StockRepository stockRepository;
    private final UserRepository userRepository;
    private final UserStockPortofolioRepository userStockPortofolioRepository;
    private List<Stock> allStocks;

    public StockService(StockRepository stockRepository, UserRepository userRepository, UserStockPortofolioRepository userStockPortofolioRepository) {
        this.stockRepository = stockRepository;
        this.userRepository = userRepository;
        this.userStockPortofolioRepository = userStockPortofolioRepository;
    }

    public void calculateUserProfit(User user) {
        var userPortofolioValue = user.getUserStatistics().getPortofolioValue();
        double userProfit = 0;

        var userStockPortofolioList = this.userStockPortofolioRepository.getPortofolio(user.getId());
        if (userStockPortofolioList.isEmpty()) {
            user.getUserStatistics().setProfit(0);
            user.getUserStatistics().setPortofolioValue(0);
            this.userRepository.save(user);
            return;
        }
        for (var userStockPortofolio : userStockPortofolioList) {
            var updatedStock = this.stockRepository.findBySymbol(userStockPortofolio.getStock().getSymbol()).get();

            if (updatedStock.getLastUpdatedPrice() == updatedStock.getAsk()) {
                continue;
            }
            userStockPortofolio.setProfit(userStockPortofolio.getVolume() * (updatedStock.getAsk() - userStockPortofolio.getAveragePrice()));
            var sign = 1;
            if (updatedStock.getAsk() > userStockPortofolio.getAveragePrice()) sign = -1;
            userStockPortofolio.setYield(((updatedStock.getAsk() - userStockPortofolio.getAveragePrice()) / Math.abs(userStockPortofolio.getAveragePrice())) * 100 * sign);
            userPortofolioValue += userStockPortofolio.getProfit();
            userStockPortofolio.getUser().getUserStatistics().setPortofolioValue(userPortofolioValue);
            userProfit += userStockPortofolio.getProfit();
            updatedStock.setLastUpdatedPrice(updatedStock.getAsk());
            this.userStockPortofolioRepository.save(userStockPortofolio);
            this.stockRepository.save(updatedStock);
        }
        user.getUserStatistics().setProfit(userProfit == 0 ? user.getUserStatistics().getProfit() : userProfit);
        this.userRepository.save(user);
    }

    public ResponseEntity<Object> getWinners() {

        Map<String, String> logMap = new HashMap<>();
        try {
            var winners = this.stockRepository.getWinners().stream().limit(5).collect(Collectors.toList());
            return getWinnersOrLosers(winners);
        } catch (Exception e) {
            logMap.put("message", "Something went wrong");
            return new ResponseEntity<>(logMap, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Object> getLosers() {
        Map<String, String> logMap = new HashMap<>();
        try {
            var losers = this.stockRepository.getLosers().stream().limit(5).collect(Collectors.toList());
            return getWinnersOrLosers(losers);
        } catch (Exception e) {
            logMap.put("message", "Something went wrong");
            return new ResponseEntity<>(logMap, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Object> getAllStocks() {
        Map<String, String> logMap = new HashMap<>();
        try {
            var stocks = this.stockRepository.findAllStocks();
            var response = new ArrayList<BasicStockDto>();
            for (Stock stock : stocks) {
                var stockDto = new BasicStockDto()
                        .setAsk(stock.getAsk())
                        .setBid(stock.getBid())
                        .setId(stock.getId())
                        .setName(stock.getName())
                        .setPriceChangeLastDay(stock.getPriceChangeLastDay())
                        .setSymbol(stock.getSymbol())
                        .setFavorite(checkIfStockIsFavorite(stock.getSymbol()));
                response.add(stockDto);
            }
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            logMap.put("message", "Something went wrong");
            return new ResponseEntity<>(logMap, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Object> addStock(Stock stock) {
        Map<String, String> logMap = new HashMap<>();
        try {
            var existingStock = this.stockRepository.findBySymbol(stock.getSymbol());
            if (existingStock.isPresent()) {
                logMap.put("message", "This stock already exists");
                return new ResponseEntity<>(logMap, HttpStatus.CONFLICT);
            }
            stock = this.stockRepository.save(stock);
            return new ResponseEntity<>(stock, HttpStatus.OK);
        } catch (Exception e) {
            logMap.put("message", "Something went wrong");
            return new ResponseEntity<>(logMap, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Object> getStockDetails(int id) {
        Map<String, String> logMap = new HashMap<>();
        try {
            var existingStock = this.stockRepository.findById(id);
            if (existingStock.isEmpty()) {
                logMap.put("message", "This stock does not exists");
                return new ResponseEntity<>(logMap, HttpStatus.NOT_FOUND);
            }
            existingStock.get().setFavorite(checkIfStockIsFavorite(existingStock.get().getSymbol()));
            return new ResponseEntity<>(existingStock.get(), HttpStatus.OK);
        } catch (Exception e) {
            logMap.put("message", "Something went wrong");
            return new ResponseEntity<>(logMap, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private User getLoggedUser() {
        try {
            var emailOptional = Helpers.getCurrentUserEmail();
            if (emailOptional.isPresent()) {
                var userOptional = this.userRepository.findByEmail(emailOptional.get());
                if (userOptional.isPresent()) {
                    return userOptional.get();
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
        return null;
    }

    private boolean checkIfStockIsFavorite(String symbol) {
        var user = getLoggedUser();
        if (null != user) {
            var stockThatIsFavorite = user
                    .getUserStatistics()
                    .getFavoriteStocks().stream()
                    .filter(s -> s.getSymbol().equals(symbol))
                    .findAny();
            return stockThatIsFavorite.isPresent();
        }
        return false;
    }

    private ResponseEntity<Object> getWinnersOrLosers(List<Stock> winners) {
        var response = new ArrayList<BasicStockDto>();
        for (Stock winner : winners) {
            var basicFavoriteStockDto = new BasicStockDto()
                    .setFavorite(checkIfStockIsFavorite(winner.getSymbol()))
                    .setAsk(winner.getAsk())
                    .setBid(winner.getBid())
                    .setId(winner.getId())
                    .setName(winner.getName())
                    .setPriceChangeLastDay(winner.getPriceChangeLastDay())
                    .setSymbol(winner.getSymbol());
            response.add(basicFavoriteStockDto);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private Stock getStock(String symbol) {
        return this.allStocks.stream().filter(stock -> stock.getSymbol().equals(symbol)).findFirst().orElse(null);
    }

}
