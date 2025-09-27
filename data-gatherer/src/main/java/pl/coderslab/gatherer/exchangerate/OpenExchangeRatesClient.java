package pl.coderslab.gatherer.exchangerate;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import pl.coderslab.gatherer.config.ExchangeRateProperties;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.util.Map;

@Component
public class OpenExchangeRatesClient implements ExchangeRateClient {

    private final RestTemplate restTemplate;
    private final ExchangeRateProperties properties;

    public OpenExchangeRatesClient(RestTemplateBuilder restTemplateBuilder, ExchangeRateProperties properties) {
        this.restTemplate = restTemplateBuilder.build();
        this.properties = properties;
    }

    @Override
    public ExchangeRateSnapshot fetchLatestRates() {
        String appId = properties.getAppId();
        if (appId == null || appId.isBlank()) {
            throw new IllegalStateException("Open Exchange Rates app id must be configured");
        }

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(properties.getBaseUrl())
                .path(properties.getLatestEndpoint())
                .queryParam("app_id", appId);

        properties.getBaseCurrency().ifPresent(base -> {
            if (!base.isBlank()) {
                builder.queryParam("base", base);
            }
        });

        URI uri = builder.build(true).toUri();

        ResponseEntity<OpenExchangeRatesResponse> response = restTemplate.getForEntity(uri, OpenExchangeRatesResponse.class);
        OpenExchangeRatesResponse body = response.getBody();
        if (body == null) {
            throw new IllegalStateException("Empty response received from Open Exchange Rates API");
        }
        Instant timestamp = Instant.ofEpochSecond(body.timestamp());
        Map<String, BigDecimal> rates = body.rates();
        return new ExchangeRateSnapshot(body.base(), rates, timestamp);
    }

    private record OpenExchangeRatesResponse(String base, Map<String, BigDecimal> rates, long timestamp) {
    }
}