package com.mariusmihai.banchelors.BullStock.dtos.stocks;

import com.mariusmihai.banchelors.BullStock.models.UserStatistics;
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

    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private UserStatistics userStatistics;
}
