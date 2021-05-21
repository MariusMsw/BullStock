package com.mariusmihai.banchelors.BullStock.services;

import com.mariusmihai.banchelors.BullStock.dtos.FxRateDto;
import com.mariusmihai.banchelors.BullStock.models.FxRate;
import com.mariusmihai.banchelors.BullStock.repositories.FxRateRepository;
import com.mariusmihai.banchelors.BullStock.utils.Currency;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.transaction.Transactional;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;

@Service
public class FxRateService {

    @Autowired
    private FxRateRepository fxRateRepository;

    private final Jsonb jsonb = JsonbBuilder.create();
    private final HttpClient client = HttpClient.newHttpClient();

    @Scheduled(fixedRate = 6000000)
    @Transactional
    public void fetchCurrencyRates() {
        String url = "";
        for (var currency : Currency.values()) {
            if (currency.equals(Currency.EUR)) {
                url = "https://api.exchangerate.host/latest?symbols=USD,RON&base=";
                this.fxRateRepository.deleteByBaseCurrencyAndToCurrency(currency, currency);
                this.fxRateRepository.save(new FxRate().setBaseCurrency(currency).setConversionRate(1.0).setFetchTime(Instant.now()).setToCurrency(currency));
            } else {
                url = "https://api.exchangerate.host/latest?symbols=EUR,USD,RON&base=";
            }
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url + currency))
                    .GET()
                    .build();
            try {
                var response =
                        client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == HttpStatus.SC_OK) {
                    var fxRateDtoResponse = jsonb.fromJson(response.body(), FxRateDto.class);
                    for (var rate : fxRateDtoResponse.getRates().entrySet()) {
                        var fxRate = new FxRate()
                                .setConversionRate(rate.getValue())
                                .setBaseCurrency(Currency.valueOf(fxRateDtoResponse.getBase()))
                                .setToCurrency(Currency.valueOf(rate.getKey()))
                                .setFetchTime(Instant.now());
                        this.fxRateRepository.deleteByBaseCurrencyAndToCurrency(currency, Currency.valueOf(rate.getKey()));
                        this.fxRateRepository.save(fxRate);
                    }
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
