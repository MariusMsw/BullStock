package com.mariusmihai.banchelors.BullStock.services;

import com.mariusmihai.banchelors.BullStock.dtos.auth.AuthResponse;
import com.mariusmihai.banchelors.BullStock.dtos.auth.LoginRequest;
import com.mariusmihai.banchelors.BullStock.dtos.auth.RefreshTokenRequest;
import com.mariusmihai.banchelors.BullStock.dtos.auth.RegisterRequest;
import com.mariusmihai.banchelors.BullStock.dtos.stocks.UserDto;
import com.mariusmihai.banchelors.BullStock.models.UserTokens;
import com.mariusmihai.banchelors.BullStock.models.User;
import com.mariusmihai.banchelors.BullStock.repositories.TokensRepository;
import com.mariusmihai.banchelors.BullStock.repositories.UserRepository;
import com.mariusmihai.banchelors.BullStock.security.AppUserDetailsService;
import com.mariusmihai.banchelors.BullStock.security.UserPrincipal;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final TokensRepository tokensRepository;
    private final AppUserDetailsService userDetailsService;
    private final AuthenticationProvider authenticationProvider;
    private final JwtService jwtService;

    @Autowired
    public UserService(
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

            User newUser = User.builder()
                    .firstName(registerRequest.getFirstName())
                    .lastName(registerRequest.getLastName())
                    .email(registerRequest.getEmail())
                    .password(BCrypt.hashpw(registerRequest.getPassword(), BCrypt.gensalt()))
                    .build();
            userRepository.save(newUser);

            return new ResponseEntity<>(registerRequest, HttpStatus.OK);
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

            UserTokens userTokens = new UserTokens().setAccessToken(token).setRefreshToken(refreshToken).setUserId(authenticatedUser.get().getId());
            this.tokensRepository.save(userTokens);
            return new ResponseEntity<>(new AuthResponse(token, refreshToken), HttpStatus.OK);
        } catch (BadCredentialsException e) {
            logMap.put("message", "Bad credentials.");
            return new ResponseEntity<>(logMap, HttpStatus.NOT_FOUND);
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

    @Transactional
    public ResponseEntity<Object> logoutUser(Long userId) {
        var userTokensOptional = this.tokensRepository.findByUserId(userId);
        Map<String, Object> logMap = new HashMap<>();
        if (userTokensOptional.isEmpty()) {
            logMap.put("message", "This user has no valid tokens!");
            return new ResponseEntity<>(logMap, HttpStatus.NOT_FOUND);
        }
        this.tokensRepository.deleteByUserId(userId);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        authentication.setAuthenticated(false);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
