package com.mariusmihai.banchelors.BullStock.services;

import com.mariusmihai.banchelors.BullStock.dtos.CashDto;
import com.mariusmihai.banchelors.BullStock.dtos.stocks.BasicStockDto;
import com.mariusmihai.banchelors.BullStock.dtos.stocks.PortfolioMetadataDto;
import com.mariusmihai.banchelors.BullStock.dtos.stocks.TradeStockDto;
import com.mariusmihai.banchelors.BullStock.dtos.stocks.UserDto;
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
    private final StockService stockService;

    @Autowired
    public UserService(UserRepository userRepository,
                       UserStatisticsRepository userStatisticsRepository,
                       UserStockPortofolioRepository userStockPortofolioRepository,
                       TransactionRepository transactionRepository,
                       UserTransactionRepository userTransactionRepository,
                       StockRepository stockRepository,
                       FxRateRepository fxRateRepository, UserHistoryRepository userHistoryRepository,
                       StockService stockService) {
        this.userRepository = userRepository;
        this.userStatisticsRepository = userStatisticsRepository;
        this.userStockPortofolioRepository = userStockPortofolioRepository;
        this.transactionRepository = transactionRepository;
        this.userTransactionRepository = userTransactionRepository;
        this.stockRepository = stockRepository;
        this.fxRateRepository = fxRateRepository;
        this.userHistoryRepository = userHistoryRepository;
        this.stockService = stockService;
    }

    public ResponseEntity<List<UserDto>> getAllUsers() {
        var allUsers = this.userRepository.findAll();
        List<UserDto> usersDto = new ArrayList<>();
        for (User user : allUsers) {
            var userDto = new UserDto()
                    .setId(user.getId())
                    .setEmail(user.getEmail())
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
                this.stockService.calculateUserProfit(user);
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
                this.stockService.calculateUserProfit(user);
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
                this.stockService.calculateUserProfit(user);
                var portofolio = this.userStockPortofolioRepository.getPortofolio(user.getId());
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
                        .setId(user.getId())
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

    public ResponseEntity<Object> removeFavoriteStock(String symbol) {
        Map<String, Object> logMap = new HashMap<>();
        try {
            var user = getLoggedUser();
            if (null != user) {
                var stock = this.stockRepository.findBySymbol(symbol);
                if (stock.isEmpty()) {
                    logMap.put("message", "This stock does not exists");
                    return new ResponseEntity<>(logMap, HttpStatus.NOT_FOUND);
                }
                var favoriteStocks = user.getUserStatistics().getFavoriteStocks();
                if (!favoriteStocks.contains(stock.get())) {
                    logMap.put("message", "This stock is not favorite");
                    return new ResponseEntity<>(logMap, HttpStatus.BAD_REQUEST);
                }
                favoriteStocks.remove(stock.get());
                user.getUserStatistics().setFavoriteStocks(favoriteStocks);
                return new ResponseEntity<>(userRepository.save(user), HttpStatus.OK);
            }
            logMap.put("message", "Could not remove this stock from favorite");
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

                if (user.getUserStatistics().getBalance() < (request.getVolume() * stockOptional.get().getAsk() * (exchangeRate + commission))) {
                    logMap.put("message", "Insufficient funds");
                    return new ResponseEntity<>(logMap, HttpStatus.BAD_REQUEST);
                }
                Transaction transaction = createBuyTransaction(request, user, stockOptional.get(), exchangeRate, commission);

                var userTransaction = new UserTransaction().setTransaction(transaction).setUser(user);
                user.getUserStatistics().setBalance(user.getUserStatistics().getBalance() - transaction.getTotalPricePayed());
                user.getUserStatistics().setPortofolioValue(user.getUserStatistics().getPortofolioValue()
                        + request.getVolume() * stockOptional.get().getAsk() * exchangeRate);


                UserStockPortofolio userStockPortofolio = createOrUpdateUserStockPortofolioForBuyType(user, stockOptional.get(), transaction.getVolume(),
                        userTransaction);
                persistTransaction(user, transaction, userTransaction, userStockPortofolio);

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

    @Transactional
    public ResponseEntity<Object> sellStock(TradeStockDto request) {
        Map<String, Object> logMap = new HashMap<>();
        try {
            var user = getLoggedUser();
            if (null != user) {
                var stockOptional = this.stockRepository.findBySymbol(request.getSymbol());
                if (stockOptional.isEmpty()) {
                    logMap.put("message", "This symbol does not exists in DB");
                    return new ResponseEntity<>(logMap, HttpStatus.BAD_REQUEST);
                }
                if (!userHasStock(user.getId(), stockOptional.get())) {
                    logMap.put("message", "User can not sell this stock");
                    return new ResponseEntity<>(logMap, HttpStatus.BAD_REQUEST);
                }
                var userStockPortofolioOptional = this.userStockPortofolioRepository.getPortofolio(user.getId())
                        .stream().filter(stockFromPortofolio -> stockFromPortofolio.getStock().equals(stockOptional.get())).findAny();
                if (userStockPortofolioOptional.isEmpty()) {
                    throw new Exception("Stock does not exists in portofolio");
                }
                if (request.getVolume() > userStockPortofolioOptional.get().getVolume()) {
                    logMap.put("message", "Can not sell this volume");
                    return new ResponseEntity<>(logMap, HttpStatus.BAD_REQUEST);
                }
                var exchangeRate = this.fxRateRepository.findConversionRateByBaseCurrencyAndToCurrency(stockOptional.get().getCurrency(), user.getCurrency());
                var commission = user.getCurrency().equals(stockOptional.get().getCurrency()) ? 0 : 0.005;
                Transaction transaction = createSellTransaction(request, user, stockOptional.get(), exchangeRate, commission, userStockPortofolioOptional.get());

                user.getUserStatistics().setBalance(user.getUserStatistics().getBalance() +
                        request.getVolume() * transaction.getClosePrice() - request.getVolume() * transaction.getClosePrice() * commission * exchangeRate);
                user.getUserStatistics().setPortofolioValue(user.getUserStatistics().getPortofolioValue()
                        - request.getVolume() * stockOptional.get().getAsk() * exchangeRate);
                var userTransaction = new UserTransaction().setTransaction(transaction).setUser(user);

                var userStockPortofolio = userStockPortofolioOptional.get()
                        .setVolume(userStockPortofolioOptional.get().getVolume() - request.getVolume());
                persistTransaction(user, transaction, userTransaction, userStockPortofolio);
                if (userStockPortofolio.getVolume() == 0) {
                    this.userStockPortofolioRepository.delete(userStockPortofolio);
                }
                logMap.put("message", "Stock sold");
                return new ResponseEntity<>(logMap, HttpStatus.OK);
            }
            logMap.put("message", "Could not sell this stock");
            return new ResponseEntity<>(logMap, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logMap.put("message", "An error has occurred. Please try again later.");
            System.out.println(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Object> getPortfolioMetadata() {
        Map<String, Object> logMap = new HashMap<>();
        try {
            var user = getLoggedUser();
            if (user != null) {
                this.stockService.calculateUserProfit(user);
                var portfolioMetadata = new PortfolioMetadataDto()
                        .setPortfolioValue(Helpers.round(user.getUserStatistics().getPortofolioValue(), 2))
                        .setBalance(Helpers.round(user.getUserStatistics().getBalance(), 2));
                return new ResponseEntity<>(portfolioMetadata, HttpStatus.OK);
            }
            logMap.put("message", "Could not fetch portfolio metadata");
            return new ResponseEntity<>(logMap, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logMap.put("message", "An error has occurred. Please try again later.");
            System.out.println(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Object> depositMoney(CashDto request) {
        Map<String, Object> logMap = new HashMap<>();
        var user = getLoggedUser();
        if (null != user) {
            user.getUserStatistics().setBalance(user.getUserStatistics().getBalance() + request.getAmount());
            this.userStatisticsRepository.save(user.getUserStatistics());
            this.userRepository.save(user);
            logMap.put("message", "Success");
            return new ResponseEntity<>(logMap, HttpStatus.OK);
        }
        logMap.put("message", "Failed");
        return new ResponseEntity<>(logMap, HttpStatus.BAD_REQUEST);
    }


    public ResponseEntity<Object> withdrawMoney(CashDto request) {
        Map<String, Object> logMap = new HashMap<>();
        var user = getLoggedUser();
        if (null != user) {
            var userBalance = user.getUserStatistics().getBalance();
            if (request.getAmount() > userBalance) {
                logMap.put("message", "Too much!");
                return new ResponseEntity<>(logMap, HttpStatus.BAD_REQUEST);
            }
            user.getUserStatistics().setBalance(user.getUserStatistics().getBalance() - request.getAmount());
            this.userStatisticsRepository.save(user.getUserStatistics());
            this.userRepository.save(user);
            logMap.put("message", "Success");
            return new ResponseEntity<>(logMap, HttpStatus.OK);
        }
        logMap.put("message", "Failed");
        return new ResponseEntity<>(logMap, HttpStatus.BAD_REQUEST);
    }

    private void persistTransaction(User user, Transaction transaction, UserTransaction userTransaction, UserStockPortofolio userStockPortofolio) {
        this.userStockPortofolioRepository.save(userStockPortofolio);

        var transactionId = this.transactionRepository.save(transaction).getId();
        this.userRepository.save(user);
        this.userTransactionRepository.save(userTransaction);
        this.userStatisticsRepository.save(user.getUserStatistics());
        userTransaction.getTransaction().setId(transactionId);
        History userHistoryItem = createHistory(user, userTransaction, userStockPortofolio.getAveragePrice(), transaction.getType());
        this.userHistoryRepository.save(userHistoryItem);
    }

    private UserStockPortofolio createOrUpdateUserStockPortofolioForBuyType(User user, Stock stock, int volume,
                                                                            UserTransaction userTransaction) {
        var userStockPortofolioOptional = this.userStockPortofolioRepository.getPortofolio(user.getId())
                .stream().filter(stockFromPortofolio -> stockFromPortofolio.getStock().equals(stock)).findAny();
        UserStockPortofolio userStockPortofolio;
        if (userStockPortofolioOptional.isPresent()) {
            userStockPortofolio = userStockPortofolioOptional.get();
            userStockPortofolio.setVolume(userStockPortofolio.getVolume() + volume)
                    .setAveragePrice((userStockPortofolio.getAveragePrice() + userTransaction.getTransaction().getOpenPrice()) / 2);
        } else {
            userStockPortofolio = new UserStockPortofolio()
                    .setYield(0).setProfit(0).setVolume(volume).setUser(user)
                    .setStock(stock).setAveragePrice(userTransaction.getTransaction().getStock().getAsk());
        }
        return userStockPortofolio;
    }

    private boolean userHasStock(int userId, Stock stock) {
        var portofolioStocks = this.userStockPortofolioRepository.getPortofolio(userId);
        for (UserStockPortofolio portofolioStock : portofolioStocks) {
            if (portofolioStock.getStock().equals(stock)) {
                return true;
            }
        }
        return false;
    }

    private History createHistory(User user, UserTransaction userTransaction, double averagePrice, TransactionType transactionType) {
        var openPrice = transactionType == TransactionType.BUY ? userTransaction.getTransaction().getOpenPrice() :
                averagePrice;
        return new History()
                .setCloseDate(userTransaction.getTransaction().getCloseDate())
                .setClosePrice(userTransaction.getTransaction().getClosePrice())
                .setOpenDate(userTransaction.getTransaction().getOpenDate())
                .setOpenPrice(openPrice)
                .setProfit(userTransaction.getTransaction().getProfitMade())
                .setUserId(user.getId())
                .setSymbol(userTransaction.getTransaction().getStock().getSymbol())
                .setTransactionId(userTransaction.getTransaction().getId())
                .setVolume(userTransaction.getTransaction().getVolume())
                .setType(userTransaction.getTransaction().getType());
    }

    private Transaction createBuyTransaction(TradeStockDto request, User user, Stock stock, double exchangeRate,
                                             double commission) {
        return new Transaction()
                .setCurrency(user.getCurrency())
                .setExchangeRate(exchangeRate)
                .setOpenDate(Instant.now().toEpochMilli())
                .setOpenPrice(stock.getAsk())
                .setStock(stock)
                .setType(TransactionType.BUY)
                .setVolume(request.getVolume())
                .setTotalPricePayed(request.getVolume() * stock.getAsk() * (commission + exchangeRate));
    }

    private Transaction createSellTransaction(TradeStockDto request, User user, Stock stock, double exchangeRate,
                                              double commission, UserStockPortofolio userStockPortofolio) {

        return new Transaction()
                .setCurrency(user.getCurrency())
                .setExchangeRate(exchangeRate)
                .setCloseDate(Instant.now().toEpochMilli())
                .setClosePrice(stock.getAsk() * (exchangeRate + commission))
                .setStock(stock)
                .setType(TransactionType.SELL)
                .setVolume(request.getVolume())
                .setProfitMade((request.getVolume() * stock.getAsk() * exchangeRate)
                        - (request.getVolume() * userStockPortofolio.getAveragePrice()));
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
