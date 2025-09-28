package pl.coderslab.currencyalert;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import pl.coderslab.provider.DataProviderApplication;
import pl.coderslab.provider.entity.CurrencyRate;
import pl.coderslab.provider.repository.CurrencyRateRepository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = DataProviderApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CurrencyRateControllerTest {

    private static final String BASE_CURRENCY = "USD";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CurrencyRateRepository currencyRateRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @AfterEach
    void cleanUp() {
        currencyRateRepository.deleteAll();
    }

    @Test
    @DisplayName("should return most recent rates limited by parameter")
    @WithMockUser
    void shouldReturnLatestRates() throws Exception {
        saveRate("USD", new BigDecimal("4.32"), OffsetDateTime.now().minusMinutes(5));
        saveRate("EUR", new BigDecimal("4.68"), OffsetDateTime.now().minusMinutes(3));
        saveRate("USD", new BigDecimal("4.35"), OffsetDateTime.now().minusMinutes(1));

        mockMvc.perform(get("/api/rates/latest")
                        .param("limit", "2")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].timestamp").isNotEmpty());
    }

    @Test
    @DisplayName("should return paged history filtered by currency and range")
    @WithMockUser
    void shouldReturnHistoryForCurrency() throws Exception {
        OffsetDateTime now = OffsetDateTime.now();
        saveRate("USD", new BigDecimal("4.30"), now.minusHours(3));
        saveRate("USD", new BigDecimal("4.40"), now.minusHours(2));
        saveRate("USD", new BigDecimal("4.50"), now.minusHours(1));
        saveRate("EUR", new BigDecimal("4.70"), now.minusHours(1));

        mockMvc.perform(get("/api/rates/history")
                        .param("currency", "usd")
                        .param("from", now.minusHours(2).minusMinutes(30).toString())
                        .param("to", now.toString())
                        .param("size", "2")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].currency").value("USD"))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    private void saveRate(String currency, BigDecimal rate, OffsetDateTime timestamp) {
        CurrencyRate entity = new CurrencyRate();
        entity.setBaseCurrency(BASE_CURRENCY);
        entity.setCurrencyCode(currency);
        entity.setRate(rate);
        entity.setTimestamp(timestamp);
        currencyRateRepository.save(entity);
    }
}
