package com.mariusmihai.banchelors.BullStock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(basePackages="com.mariusmihai.banchelors.BullStock.repositories")
@SpringBootApplication
public class BullStockApplication {

	public static void main(String[] args) {
		SpringApplication.run(BullStockApplication.class, args);
	}

}
