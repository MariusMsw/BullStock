package com.mariusmihai.banchelors.BullStock.dtos.auth;

import com.mariusmihai.banchelors.BullStock.utils.Currency;
import lombok.Data;

@Data
public class RegisterRequest {
    private String email;
    private String firstName;
    private String lastName;
    private String password;
    private Currency currency;
}
