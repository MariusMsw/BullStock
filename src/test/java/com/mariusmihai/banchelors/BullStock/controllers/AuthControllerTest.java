package com.mariusmihai.banchelors.BullStock.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mariusmihai.banchelors.BullStock.dtos.auth.LoginRequest;
import com.mariusmihai.banchelors.BullStock.dtos.auth.RegisterRequest;
import com.mariusmihai.banchelors.BullStock.repositories.TokensRepository;
import com.mariusmihai.banchelors.BullStock.repositories.UserRepository;
import com.mariusmihai.banchelors.BullStock.services.JwtService;
import com.mariusmihai.banchelors.BullStock.utils.Currency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokensRepository tokensRepository;

    public static String VALID_TOKEN;

    @BeforeEach
    @Transactional
    void beforeEach() throws Exception {

        var user = userRepository.findByEmail("valid@email.com");
        if (user.isPresent()) {
            tokensRepository.deleteAllByUserId(user.get().getId());
            userRepository.deleteByEmail("valid@email.com");
        }
        var registerRequest = new RegisterRequest()
                .setEmail("valid@email.com")
                .setCurrency(Currency.USD)
                .setPassword("password");

        mvc.perform(MockMvcRequestBuilders
                .post("/auth/register")
                .content(asJsonString(registerRequest))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        VALID_TOKEN = JwtService.generateToken("valid@email.com");
    }

    @Test
    @Transactional
    public void testRegisterSuccess() throws Exception {

        var user = userRepository.findByEmail("valid@email.com");
        if (user.isPresent()) {
            tokensRepository.deleteAllByUserId(user.get().getId());
            userRepository.deleteByEmail("valid@email.com");
        }

        var registerRequest = new RegisterRequest()
                .setEmail("valid@email.com")
                .setCurrency(Currency.USD)
                .setPassword("password");

        mvc.perform(MockMvcRequestBuilders
                .post("/auth/register")
                .content(asJsonString(registerRequest))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @Transactional
    public void testRegisterConflict() throws Exception {

        var registerRequest = new RegisterRequest()
                .setEmail("valid@email.com")
                .setCurrency(Currency.USD)
                .setPassword("password");

        mvc.perform(MockMvcRequestBuilders
                .post("/auth/register")
                .content(asJsonString(registerRequest))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    @Transactional
    public void testLoginSuccess() throws Exception {

        var loginRequest = new LoginRequest()
                .setEmail("valid@email.com")
                .setPassword("password");

        mvc.perform(MockMvcRequestBuilders
                .post("/auth/login")
                .content(asJsonString(loginRequest))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @Transactional
    public void testLoginFailed() throws Exception {

        var loginRequest = new LoginRequest()
                .setEmail("valid@email.com")
                .setPassword("wrongPassword");

        mvc.perform(MockMvcRequestBuilders
                .post("/auth/login")
                .content(asJsonString(loginRequest))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}