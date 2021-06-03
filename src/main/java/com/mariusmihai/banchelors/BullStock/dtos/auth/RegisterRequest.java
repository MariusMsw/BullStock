package com.mariusmihai.banchelors.BullStock.dtos.auth;

import com.mariusmihai.banchelors.BullStock.utils.Currency;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class RegisterRequest {
    private String email;
    private String password;
    private Currency currency;
}
