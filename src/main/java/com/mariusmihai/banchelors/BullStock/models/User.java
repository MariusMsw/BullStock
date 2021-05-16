package com.mariusmihai.banchelors.BullStock.models;

import com.mariusmihai.banchelors.BullStock.utils.Currency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Accessors(chain = true)
@Entity
@Table(name = "users")
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String email;
    private String password;
    @OneToOne(cascade = CascadeType.ALL)
    private UserStatistics userStatistics;
    @Enumerated(value = EnumType.STRING)
    private Currency currency;
}
