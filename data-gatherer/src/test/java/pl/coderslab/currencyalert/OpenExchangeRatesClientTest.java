package pl.coderslab.currencyalert;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import pl.coderslab.gatherer.config.ExchangeRateProperties;
import pl.coderslab.gatherer.exchangerate.ExchangeRateSnapshot;
import pl.coderslab.gatherer.exchangerate.OpenExchangeRatesClient;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OpenExchangeRatesClientTest {

    private ExchangeRateProperties properties;
    private MockRestServiceServer server;
    private OpenExchangeRatesClient client;

    @BeforeEach
    void setUp() {
        properties = new ExchangeRateProperties();
        properties.setAppId("test-app");
        properties.setBaseUrl("https://example.com");
        properties.setLatestEndpoint("/latest.json");
        properties.setBaseCurrency("USD");

        RestTemplate restTemplate = new RestTemplateBuilder().build();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        client = new OpenExchangeRatesClient(new RestTemplateBuilder() {
            @Override
            public RestTemplate build() {
                return restTemplate;
            }
        }, properties);
    }

    @Test
    void shouldFetchRatesFromRemoteApi() {
        String expectedUrl = "https://example.com/latest.json?app_id=test-app&base=USD";
        server.expect(requestTo(expectedUrl))
                .andRespond(withSuccess("{" +
                                "\"base\":\"USD\"," +
                                "\"timestamp\":1690000000," +
                                "\"rates\":{\"PLN\":4.12,\"EUR\":0.92}}",
                        MediaType.APPLICATION_JSON));

        ExchangeRateSnapshot snapshot = client.fetchLatestRates();

        assertThat(snapshot.baseCurrency()).isEqualTo("USD");
        assertThat(snapshot.retrievedAt()).isEqualTo(Instant.ofEpochSecond(1_690_000_000));
        assertThat(snapshot.rates()).containsExactlyInAnyOrderEntriesOf(Map.of(
                "PLN", new BigDecimal("4.12"),
                "EUR", new BigDecimal("0.92")
        ));
    }
}
