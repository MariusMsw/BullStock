package com.mariusmihai.banchelors.BullStock.services;

import com.mariusmihai.banchelors.BullStock.dtos.stocks.BasicStockDto;
import com.mariusmihai.banchelors.BullStock.dtos.stocks.StockChartRequest;
import com.mariusmihai.banchelors.BullStock.dtos.stocks.StockChartResponse;
import com.mariusmihai.banchelors.BullStock.dtos.stocks.StockScreenDto;
import com.mariusmihai.banchelors.BullStock.models.Stock;
import com.mariusmihai.banchelors.BullStock.models.User;
import com.mariusmihai.banchelors.BullStock.repositories.StockRepository;
import com.mariusmihai.banchelors.BullStock.repositories.UserRepository;
import com.mariusmihai.banchelors.BullStock.repositories.UserStockPortofolioRepository;
import com.mariusmihai.banchelors.BullStock.utils.Helpers;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static com.mariusmihai.banchelors.BullStock.utils.Helpers.round;

@Service
public class StockService {

    private final StockRepository stockRepository;
    private final UserRepository userRepository;
    private final UserStockPortofolioRepository userStockPortofolioRepository;
    private final Jsonb jsonb = JsonbBuilder.create();

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
            refreshStockPrices();
            var updatedStock = this.stockRepository.findBySymbol(userStockPortofolio.getStock().getSymbol()).get();

            userStockPortofolio.setProfit(userStockPortofolio.getVolume() * (updatedStock.getAsk() - userStockPortofolio.getAveragePrice()));
            var sign = 1;
            if (updatedStock.getAsk() > userStockPortofolio.getAveragePrice()) sign = -1;
            userStockPortofolio.setYield(((updatedStock.getAsk() - userStockPortofolio.getAveragePrice()) / Math.abs(userStockPortofolio.getAveragePrice())) * 100 * sign);
            userStockPortofolio.getUser().getUserStatistics().setPortofolioValue(userPortofolioValue);
            userProfit += userStockPortofolio.getProfit();
            updatedStock.setLastUpdatedPrice(updatedStock.getAsk());
            this.userStockPortofolioRepository.save(userStockPortofolio);
            this.stockRepository.save(updatedStock);
        }
        user.getUserStatistics().setProfit(userProfit == 0 ? user.getUserStatistics().getProfit() : userProfit);
        this.userRepository.save(user);
    }

    @Scheduled(cron = "0/10 * * * * ?")
    public void refreshStockPrices() {
        var allStocks = this.stockRepository.findAllStocks();
        for (var stock : allStocks) {
            stock.setAsk(getNextPrice(stock.getAsk()));
            stock.setBid(getNextPrice(stock.getBid()));
            this.stockRepository.save(stock);
        }
    }

    private double getNextPrice(double oldPrice) {
        var _random = new Random();
        // Instead of a fixed volatility, pick a random volatility
        // each time, between 2 and 10.
        double volatility = _random.nextFloat() * 3 + 2;

        double rnd = _random.nextFloat();

        double changePercent = 2 * volatility * rnd;

        if (changePercent > volatility) {
            changePercent -= (2 * volatility);
        }
        double changeAmount = oldPrice * changePercent / 100;
        double newPrice = oldPrice + changeAmount;

        // Add a ceiling and floor.
        if (newPrice < 0.7 * oldPrice) {
            newPrice += Math.abs(changeAmount) * 2;
        } else if (newPrice > 1.3 * oldPrice) {
            newPrice -= Math.abs(changeAmount) * 2;
        }

        return round(newPrice, 2);

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
            Integer volume = 0;
            var user = getLoggedUser();
            boolean loggedIn = null != user;
            for (Stock stock : stocks) {
                var stockDto = new BasicStockDto()
                        .setAsk(round(stock.getAsk(), 2))
                        .setBid(round(stock.getBid(), 2))
                        .setId(stock.getId())
                        .setName(stock.getName())
                        .setPriceChangeLastDay(round(stock.getPriceChangeLastDay(), 2))
                        .setSymbol(stock.getSymbol())
                        .setFavorite(checkIfStockIsFavorite(stock.getSymbol()))
                        .setSharesOwned(loggedIn ? getVolume(stock.getSymbol(), user.getId()) : 0);
                response.add(stockDto);
            }
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            logMap.put("message", "Something went wrong");
            return new ResponseEntity<>(logMap, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Object> addStock(String stockString) {
        Map<String, String> logMap = new HashMap<>();
        try {
            Stock stock = jsonb.fromJson(stockString, Stock.class);
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

    public ResponseEntity<Object> getStockScreen(StockChartRequest request) {

        Map<String, String> logMap = new HashMap<>();
        try {
            var existingStock = this.stockRepository.findBySymbol(request.getSymbol());
            if (existingStock.isEmpty()) {
                logMap.put("message", "This stock does not exists");
                return new ResponseEntity<>(logMap, HttpStatus.NOT_FOUND);
            }
            List<StockChartResponse> data = new ArrayList<>();
            switch (request.getPeriod()) {
                case ONE_HOUR:
                    data = computeStockPricesBasedOnPeriod(Instant.now().minusSeconds(3600),
                            10, existingStock.get().getAsk());
                    break;
                case ONE_DAY:
                    data = computeStockPricesBasedOnPeriod(Instant.now().minusSeconds(86400),
                            30, existingStock.get().getAsk());
                    break;
                case ONE_WEEK:
                    data = computeStockPricesBasedOnPeriod(Instant.now().minusSeconds(604800),
                            30, existingStock.get().getAsk());
                    break;
                case ONE_MONTH:
                    data = computeStockPricesBasedOnPeriod(Instant.now().minusSeconds(2629743),
                            30, existingStock.get().getAsk());
                    break;
                case ONE_YEAR:
                    data = computeStockPricesBasedOnPeriod(Instant.now().minusSeconds(31556926),
                            30, existingStock.get().getAsk());
                    break;
            }
            var user = getLoggedUser();
            var favorite = false;
            Integer volume = 0;
            if (user != null) {
                favorite = user.getUserStatistics().getFavoriteStocks().contains(existingStock.get());
                volume = userStockPortofolioRepository.findVolumeBySymbol(existingStock.get().getSymbol(), user.getId());
            }
            if (null == volume) volume = 0;
            StockScreenDto response = new StockScreenDto()
                    .setData(data)
                    .setStockName(existingStock.get().getName())
                    .setSharePrice(existingStock.get().getAsk())
                    .setFavorite(favorite)
                    .setSharesOwned(volume);

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            logMap.put("message", "Something went wrong");
            return new ResponseEntity<>(logMap, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Object> getVolumeBySymbol(String symbol) {
        var user = getLoggedUser();
        if (null != user) {
            return new ResponseEntity<>(getVolume(symbol, user.getId()), HttpStatus.OK);
        }
        return new ResponseEntity<>(0, HttpStatus.BAD_REQUEST);
    }

    private Integer getVolume(String symbol, Integer userId) {
        var volume = this.userStockPortofolioRepository.findVolumeBySymbol(symbol, userId);
        if (null == volume) volume = 0;
        return volume;
    }

    private List<StockChartResponse> computeStockPricesBasedOnPeriod(Instant from, int numberOfIntervals, double lastPrice) {
        List<StockChartResponse> response = new ArrayList<>();
        Instant lastValue = from;
        long totalLength = Instant.now().getEpochSecond() - from.getEpochSecond();
        long subrangeLength = totalLength / numberOfIntervals;
        for (int i = 0; i < numberOfIntervals; i++) {
            StockChartResponse value = new StockChartResponse()
                    .setPeriod(lastValue)
                    .setPrice(getNextPrice(lastPrice));
            lastValue = lastValue.plusSeconds(subrangeLength);
            response.add(value);
        }

        return response;
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

}
