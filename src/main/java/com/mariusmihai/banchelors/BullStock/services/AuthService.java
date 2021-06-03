package com.mariusmihai.banchelors.BullStock.services;

import com.mariusmihai.banchelors.BullStock.dtos.auth.*;
import com.mariusmihai.banchelors.BullStock.models.UserStatistics;
import com.mariusmihai.banchelors.BullStock.models.UserTokens;
import com.mariusmihai.banchelors.BullStock.models.User;
import com.mariusmihai.banchelors.BullStock.repositories.TokensRepository;
import com.mariusmihai.banchelors.BullStock.repositories.UserRepository;
import com.mariusmihai.banchelors.BullStock.security.AppUserDetailsService;
import com.mariusmihai.banchelors.BullStock.security.UserPrincipal;
import com.mariusmihai.banchelors.BullStock.utils.Currency;
import com.mariusmihai.banchelors.BullStock.utils.Helpers;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final TokensRepository tokensRepository;
    private final AppUserDetailsService userDetailsService;
    private final AuthenticationProvider authenticationProvider;
    private final JwtService jwtService;

    @Autowired
    public AuthService(
            UserRepository userRepository,
            TokensRepository tokensRepository,
            AppUserDetailsService userDetailsService,
            AuthenticationProvider authenticationProvider,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.tokensRepository = tokensRepository;
        this.userDetailsService = userDetailsService;
        this.authenticationProvider = authenticationProvider;
        this.jwtService = jwtService;
    }

    public ResponseEntity<Object> registerUser(RegisterRequest registerRequest) {
        Map<String, Object> logMap = new HashMap<>();

        try {
            Optional<User> existingUser = userRepository.findByEmail(registerRequest.getEmail());

            if (existingUser.isPresent()) {
                logMap.put("message", "Email already used");
                return new ResponseEntity<>(logMap, HttpStatus.CONFLICT);
            }
            var userStatistics = new UserStatistics()
                    .setBalance(0)
                    .setPortofolioValue(0)
                    .setProfit(0)
                    .setFavoriteStocks(List.of());

            User newUser = User.builder()
                    .email(registerRequest.getEmail())
                    .password(BCrypt.hashpw(registerRequest.getPassword(), BCrypt.gensalt()))
                    .currency(registerRequest.getCurrency() == null ? Currency.USD : registerRequest.getCurrency())
                    .userStatistics(userStatistics)
                    .build();
            userRepository.save(newUser);
            logMap.put("message", "User saved with success");
            return new ResponseEntity<>(logMap, HttpStatus.OK);
        } catch (Exception e) {
            logMap.put("message", "An error has occurred. Please try again later.");
            System.out.println(e.getMessage());
            return new ResponseEntity<>(logMap, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Object> loginUser(LoginRequest loginRequest) {
        Map<String, Object> logMap = new HashMap<>();
        try {
            authenticationProvider.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

            Optional<User> authenticatedUser = userRepository.findByEmail(loginRequest.getEmail());

            if (authenticatedUser.isEmpty()) {
                logMap.put("message", "Bad credentials.");
                return new ResponseEntity<>(logMap, HttpStatus.NOT_FOUND);
            }

            String token = jwtService.generateToken(authenticatedUser.get());
            String refreshToken = jwtService.generateRefreshToken(authenticatedUser.get());

            UserTokens userTokens = new UserTokens().setAccessToken(token).setRefreshToken(refreshToken).setUser(authenticatedUser.get());
            this.tokensRepository.save(userTokens);
            return new ResponseEntity<>(new AuthResponse(token, refreshToken), HttpStatus.OK);
        } catch (BadCredentialsException e) {
            logMap.put("message", "Bad credentials.");
            return new ResponseEntity<>(logMap, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logMap.put("message", "An error has occurred. Please try again later.");
            return new ResponseEntity<>(logMap, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Object> refreshToken(RefreshTokenRequest refreshTokenRequest) {
        Map<String, Object> logMap = new HashMap<>();
        try {
            String email = jwtService.extractClaim(refreshTokenRequest.getRefreshToken(), Claims::getSubject);
            if (email == null) {
                return new ResponseEntity<>(logMap, HttpStatus.BAD_REQUEST);
            }

            UserPrincipal userPrincipal = (UserPrincipal) userDetailsService.loadUserByUsername(email);

            if (!jwtService.validateToken(refreshTokenRequest.getRefreshToken(), userPrincipal.getUser())) {
                return new ResponseEntity<>(logMap, HttpStatus.UNAUTHORIZED);
            }

            String token = jwtService.generateToken(userPrincipal.getUser());
            String refreshToken = jwtService.generateRefreshToken(userPrincipal.getUser());
            return new ResponseEntity<>(new AuthResponse(token, refreshToken), HttpStatus.OK);
        } catch (Exception e) {
            logMap.put("message", "An error has occurred. Please try again later.");
            System.out.println(e.getMessage());
            return new ResponseEntity<>(logMap, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public Map<String, Object> getMapFromClaims(Claims claims) {
        Map<String, Object> expectedMap = new HashMap<String, Object>();
        for (Map.Entry<String, Object> entry : claims.entrySet()) {
            expectedMap.put(entry.getKey(), entry.getValue());
        }
        return expectedMap;
    }

    @Transactional
    public ResponseEntity<Object> logoutUser() {
        Map<String, Object> logMap = new HashMap<>();
        try {
            var userEmail = Helpers.getCurrentUserEmail();

            if (userEmail.isEmpty()) {
                logMap.put("message", "This user has no valid tokens!");
                return new ResponseEntity<>(logMap, HttpStatus.NOT_FOUND);
            }
            var user = this.userRepository.findByEmail(userEmail.get());
            if (user.isPresent()) {
                var userTokens = this.tokensRepository.findAllByUserId(user.get().getId());
                if (userTokens.isEmpty()) {
                    logMap.put("message", "This user has no valid tokens!");
                    return new ResponseEntity<>(logMap, HttpStatus.NOT_FOUND);
                }
                userTokens.forEach(token -> this.tokensRepository.deleteAllByUserId(user.get().getId()));
                logMap.put("message", "Success");
                return new ResponseEntity<>(logMap, HttpStatus.OK);
            }
            logMap.put("message", "Could not logout user");
            return new ResponseEntity<>(logMap, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logMap.put("message", "An error has occurred. Please try again later.");
            System.out.println(e.getMessage());
            return new ResponseEntity<>(logMap, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    public ResponseEntity<Object> changePassword(ChangePasswordDto passwords) {
        Map<String, Object> logMap = new HashMap<>();
        try {
            var userEmail = Helpers.getCurrentUserEmail();
            if (userEmail.isEmpty()) {
                logMap.put("message", "This user has no valid tokens!");
                return new ResponseEntity<>(logMap, HttpStatus.NOT_FOUND);
            }
            var encoder = new BCryptPasswordEncoder();
            var userOptional = this.userRepository.findByEmailAndPassword(userEmail.get(), passwords.getOldPassword());
            if (userOptional.isPresent()) {
                var user = userOptional.get();
                user.setPassword(encoder.encode(passwords.getNewPassword()));
                return new ResponseEntity<>(userRepository.save(user), HttpStatus.OK);
            }
            logMap.put("message", "Could not change password");
            return new ResponseEntity<>(logMap, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logMap.put("message", "An error has occurred. Please try again later.");
            System.out.println(e.getMessage());
            return new ResponseEntity<>(logMap, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
