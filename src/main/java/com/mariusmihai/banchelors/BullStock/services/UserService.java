package com.mariusmihai.banchelors.BullStock.services;

import com.mariusmihai.banchelors.BullStock.dtos.stocks.UserDto;
import com.mariusmihai.banchelors.BullStock.models.Stock;
import com.mariusmihai.banchelors.BullStock.models.User;
import com.mariusmihai.banchelors.BullStock.repositories.UserRepository;
import com.mariusmihai.banchelors.BullStock.repositories.UserStatisticsRepository;
import com.mariusmihai.banchelors.BullStock.repositories.UserStockPortofolioRepository;
import com.mariusmihai.banchelors.BullStock.utils.Helpers;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserStatisticsRepository userStatisticsRepository;
    private final UserStockPortofolioRepository userStockPortofolioRepository;

    public UserService(UserRepository userRepository,
                       UserStatisticsRepository userStatisticsRepository,
                       UserStockPortofolioRepository userStockPortofolioRepository) {
        this.userRepository = userRepository;
        this.userStatisticsRepository = userStatisticsRepository;
        this.userStockPortofolioRepository = userStockPortofolioRepository;
    }

    public ResponseEntity<List<UserDto>> getAllUsers() {
        var allUsers = this.userRepository.findAll();
        List<UserDto> usersDto = new ArrayList<>();
        for (User user : allUsers) {
            var userDto = new UserDto()
                    .setId(user.getId())
                    .setEmail(user.getEmail())
                    .setFirstName(user.getFirstName())
                    .setLastName(user.getLastName());
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
                var favorites = user.getUserStatistics()
                        .getFavoriteStocks()
                        .stream()
                        .sorted(Comparator.comparing(Stock::getSymbol))
                        .collect(Collectors.toList());
                return new ResponseEntity<>(user.getUserStatistics().getFavoriteStocks(), HttpStatus.OK);
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
