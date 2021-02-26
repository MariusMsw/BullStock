package com.mariusmihai.banchelors.BullStock.utils;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("secret")
public class AppConfiguration {
    private String tokenSecret;
}
