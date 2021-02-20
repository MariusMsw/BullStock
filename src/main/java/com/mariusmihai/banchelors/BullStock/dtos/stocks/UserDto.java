package com.mariusmihai.banchelors.BullStock.dtos.stocks;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Accessors(chain = true)
public class UserDto {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
}
