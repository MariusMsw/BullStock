package com.mariusmihai.banchelors.BullStock.services;

import com.mariusmihai.banchelors.BullStock.dtos.stocks.BasicStockDto;
import com.mariusmihai.banchelors.BullStock.dtos.stocks.TradeStockDto;
import com.mariusmihai.banchelors.BullStock.dtos.stocks.UserDto;
import com.mariusmihai.banchelors.BullStock.dtos.stocks.UserHistory;
import com.mariusmihai.banchelors.BullStock.models.*;
import com.mariusmihai.banchelors.BullStock.repositories.*;
import com.mariusmihai.banchelors.BullStock.utils.Helpers;
import com.mariusmihai.banchelors.BullStock.utils.TransactionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserStatisticsRepository userStatisticsRepository;
    private final UserStockPortofolioRepository userStockPortofolioRepository;
    private final TransactionRepository transactionRepository;
    private final UserTransactionRepository userTransactionRepository;
    private final StockRepository stockRepository;
    private final FxRateRepository fxRateRepository;
    private final UserHistoryRepository userHistoryRepository;

    @Autowired
    public UserService(UserRepository userRepository,
                       UserStatisticsRepository userStatisticsRepository,
                       UserStockPortofolioRepository userStockPortofolioRepository,
                       TransactionRepository transactionRepository,
                       UserTransactionRepository userTransactionRepository,
                       StockRepository stockRepository,
                       FxRateRepository fxRateRepository, UserHistoryRepository userHistoryRepository) {
        this.userRepository = userRepository;
        this.userStatisticsRepository = userStatisticsRepository;
        this.userStockPortofolioRepository = userStockPortofolioRepository;
        this.transactionRepository = transactionRepository;
        this.userTransactionRepository = userTransactionRepository;
        this.stockRepository = stockRepository;
        this.fxRateRepository = fxRateRepository;
        this.userHistoryRepository = userHistoryRepository;
    }

    public ResponseEntity<List<UserDto>> getAllUsers() {
        var allUsers = this.userRepository.findAll();
        List<UserDto> usersDto = new ArrayList<>();
        for (User user : allUsers) {
            var userDto = new UserDto()
                    .setId(user.getId())
                    .setEmail(user.getEmail())
                    .setFirstName(user.getFirstName())
                    .setLastName(user.getLastName())
                    .setUserStatistics(user.getUserStatistics());
            usersDto.add(userDto);
        }
        return new ResponseEntity<>(usersDto, HttpStatus.OK);
    }

    public ResponseEntity<Object> getStatistics() {

        Map<String, Object> logMap = new HashMap<>();
        try {
            var user = getLoggedUser();
            if (null != user) {
                return new ResponseEntity<>(user.getUserStatistics(), HttpStatus.OK);
            }
            logMap.put("message", "Could not fetch statistics");
            return new ResponseEntity<>(logMap, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logMap.put("message", "An error has occurred. Please try again later.");
            System.out.println(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    public ResponseEntity<Object> getFavorite() {
        Map<String, Object> logMap = new HashMap<>();
        try {
            var user = getLoggedUser();
            if (null != user) {
                var response = new ArrayList<BasicStockDto>();
                var favorites = user.getUserStatistics()
                        .getFavoriteStocks()
                        .stream()
                        .sorted(Comparator.comparing(Stock::getSymbol))
                        .collect(Collectors.toList());
                for (var favoriteStock : favorites) {
                    var basicFavoriteStockDto = new BasicStockDto()
                            .setFavorite(true)
                            .setAsk(favoriteStock.getAsk())
                            .setBid(favoriteStock.getBid())
                            .setId(favoriteStock.getId())
                            .setName(favoriteStock.getName())
                            .setPriceChangeLastDay(favoriteStock.getPriceChangeLastDay())
                            .setSymbol(favoriteStock.getSymbol());
                    response.add(basicFavoriteStockDto);
                }
                return new ResponseEntity<>(response, HttpStatus.OK);
            }
            logMap.put("message", "Could not fetch favorite stocks");
            return new ResponseEntity<>(logMap, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logMap.put("message", "An error has occurred. Please try again later.");
            System.out.println(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Object> getPortofolioStocks() {
        Map<String, Object> logMap = new HashMap<>();
        try {
            var user = getLoggedUser();
            if (null != user) {
                var portofolio = this.userStockPortofolioRepository.getPortofolio();
                return new ResponseEntity<>(portofolio, HttpStatus.OK);
            }
            logMap.put("message", "Could not fetch portofolio");
            return new ResponseEntity<>(logMap, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logMap.put("message", "An error has occurred. Please try again later.");
            System.out.println(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Object> getLoggedInUser() {
        Map<String, Object> logMap = new HashMap<>();
        try {
            var user = getLoggedUser();
            if (null != user) {
                var response = new UserDto()
                        .setEmail(user.getEmail())
                        .setFirstName(user.getFirstName())
                        .setId(user.getId())
                        .setLastName(user.getLastName())
                        .setUserStatistics(user.getUserStatistics());
                return new ResponseEntity<>(response, HttpStatus.OK);
            }
            logMap.put("message", "Could not fetch user details");
            return new ResponseEntity<>(logMap, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logMap.put("message", "An error has occurred. Please try again later.");
            System.out.println(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    public ResponseEntity<Object> getHistory() {
        Map<String, Object> logMap = new HashMap<>();
        try {
            var user = getLoggedUser();
            if (null != user) {
                var history = this.userHistoryRepository.findAllByUserIdOrderByOpenDate(user.getId());
                return new ResponseEntity<>(history, HttpStatus.OK);
            }
            logMap.put("message", "Could not fetch history");
            return new ResponseEntity<>(logMap, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logMap.put("message", "An error has occurred. Please try again later.");
            System.out.println(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Object> addFavoriteStock(String symbol) {
        Map<String, Object> logMap = new HashMap<>();
        try {
            var user = getLoggedUser();
            if (null != user) {
                var stock = this.stockRepository.findBySymbol(symbol);
                if (stock.isEmpty()) {
                    logMap.put("message", "This stock does not exists");
                    return new ResponseEntity<>(logMap, HttpStatus.NOT_FOUND);
                }
                var alreadyFavorites = user.getUserStatistics().getFavoriteStocks();
                if (alreadyFavorites.contains(stock.get())) {
                    logMap.put("message", "This stock is already favorite");
                    return new ResponseEntity<>(logMap, HttpStatus.CONFLICT);
                }
                alreadyFavorites.add(stock.get());
                user.getUserStatistics().setFavoriteStocks(alreadyFavorites);
                return new ResponseEntity<>(userRepository.save(user), HttpStatus.OK);
            }
            logMap.put("message", "Could not add this stock as favorite");
            return new ResponseEntity<>(logMap, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logMap.put("message", "An error has occurred. Please try again later.");
            System.out.println(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public ResponseEntity<Object> buyStock(TradeStockDto request) {
        Map<String, Object> logMap = new HashMap<>();
        try {
            var user = getLoggedUser();
            if (null != user) {
                var stockOptional = this.stockRepository.findBySymbol(request.getSymbol());
                if (stockOptional.isEmpty()) {
                    logMap.put("message", "This symbol does not exists in DB");
                    return new ResponseEntity<>(logMap, HttpStatus.BAD_REQUEST);
                }
                var exchangeRate = this.fxRateRepository.findConversionRateByBaseCurrencyAndToCurrency(stockOptional.get().getCurrency(), user.getCurrency());
                var commission = user.getCurrency().equals(stockOptional.get().getCurrency()) ? 0 : 0.005;

                if (user.getUserStatistics().getBalance() < (request.getVolume() * stockOptional.get().getBid() * (exchangeRate + commission))) {
                    logMap.put("message", "Insufficient funds");
                    return new ResponseEntity<>(logMap, HttpStatus.BAD_REQUEST);
                }
                var transaction = new Transaction()
                        .setCurrency(user.getCurrency())
                        .setExchangeRate(exchangeRate)
                        .setOpenDate(Instant.now().toEpochMilli())
                        .setOpenPrice(stockOptional.get().getBid() * (exchangeRate + commission))
                        .setStock(stockOptional.get())
                        .setType(TransactionType.BUY)
                        .setVolume(request.getVolume())
                        .setTotalPricePayed(request.getVolume() * stockOptional.get().getBid() * (exchangeRate + commission));
                var userTransaction = new UserTransaction().setTransaction(transaction).setUser(user);
                user.getUserStatistics().setBalance(user.getUserStatistics().getBalance() - transaction.getTotalPricePayed());
                user.getUserStatistics().setPortofolioValue(user.getUserStatistics().getPortofolioValue() + transaction.getTotalPricePayed());
                var userStockPortofolio = new UserStockPortofolio().setStock(stockOptional.get()).setUser(user)
                        .setVolume(request.getVolume()).setProfit(0).setYield(0);
                var userHistoryItem = new History()
                        .setCloseDate(userTransaction.getTransaction().getCloseDate())
                        .setClosePrice(userTransaction.getTransaction().getClosePrice())
                        .setOpenDate(userTransaction.getTransaction().getOpenDate())
                        .setOpenPrice(userTransaction.getTransaction().getOpenPrice())
                        .setProfit(0)
                        .setUserId(user.getId())
                        .setSymbol(userTransaction.getTransaction().getStock().getSymbol())
                        .setTransactionId(userTransaction.getTransaction().getId())
                        .setVolume(userTransaction.getTransaction().getVolume())
                        .setType(userTransaction.getTransaction().getType());
                this.transactionRepository.save(transaction);
                this.userTransactionRepository.save(userTransaction);
                this.userRepository.save(user);
                this.userStockPortofolioRepository.save(userStockPortofolio);
                this.userStatisticsRepository.save(user.getUserStatistics());
                this.userHistoryRepository.save(userHistoryItem);
                logMap.put("message", "Stock bought");
                return new ResponseEntity<>(logMap, HttpStatus.OK);
            }
            logMap.put("message", "Could not buy this stock");
            return new ResponseEntity<>(logMap, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logMap.put("message", "An error has occurred. Please try again later.");
            System.out.println(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
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

}
