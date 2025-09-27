package pl.coderslab.currencyalert;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
import pl.coderslab.provider.dto.SubscriptionDto;
import pl.coderslab.provider.entity.Subscription;
import pl.coderslab.provider.entity.User;
import pl.coderslab.provider.repository.SubscriptionRepository;
import pl.coderslab.provider.repository.UserRepository;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = DataProviderApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SubscriptionControllerTest {

    private static final String USER_EMAIL = "subscriber@example.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    private User user;

    @BeforeEach
    void setupUser() {
        subscriptionRepository.deleteAll();
        userRepository.deleteAll();
        user = new User();
        user.setEmail(USER_EMAIL);
        user.setPasswordHash("encoded");
        user = userRepository.save(user);
    }

    @AfterEach
    void cleanUp() {
        subscriptionRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("should create subscription for authenticated user")
    @WithMockUser(username = USER_EMAIL)
    void shouldCreateSubscription() throws Exception {
        SubscriptionDto request = new SubscriptionDto(null, "usd", "pln", new BigDecimal("1.500"), null, null);

        mockMvc.perform(post("/api/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.baseCurrency").value("USD"))
                .andExpect(jsonPath("$.targetCurrency").value("PLN"))
                .andExpect(jsonPath("$.thresholdPercent").value(1.5))
                .andExpect(jsonPath("$.active").value(true));

        assertThat(subscriptionRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("should list subscriptions for current user")
    @WithMockUser(username = USER_EMAIL)
    void shouldListSubscriptions() throws Exception {
        Subscription first = new Subscription();
        first.setUser(user);
        first.setBaseCurrency("USD");
        first.setTargetCurrency("PLN");
        first.setThresholdPercent(new BigDecimal("1.100"));
        subscriptionRepository.save(first);

        Subscription second = new Subscription();
        second.setUser(user);
        second.setBaseCurrency("EUR");
        second.setTargetCurrency("PLN");
        second.setThresholdPercent(new BigDecimal("0.900"));
        subscriptionRepository.save(second);

        mockMvc.perform(get("/api/subscriptions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("should update existing subscription")
    @WithMockUser(username = USER_EMAIL)
    void shouldUpdateSubscription() throws Exception {
        Subscription subscription = new Subscription();
        subscription.setUser(user);
        subscription.setBaseCurrency("USD");
        subscription.setTargetCurrency("PLN");
        subscription.setThresholdPercent(new BigDecimal("1.100"));
        subscription = subscriptionRepository.save(subscription);

        SubscriptionDto request = new SubscriptionDto(null, "usd", "pln", new BigDecimal("2.000"), false, null);

        mockMvc.perform(put("/api/subscriptions/{id}", subscription.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.thresholdPercent").value(2.0))
                .andExpect(jsonPath("$.active").value(false));

        Subscription updated = subscriptionRepository.findById(subscription.getId()).orElseThrow();
        assertThat(updated.getThresholdPercent()).isEqualByComparingTo("2.000");
        assertThat(updated.isActive()).isFalse();
    }

    @Test
    @DisplayName("should delete subscription when owner requests it")
    @WithMockUser(username = USER_EMAIL)
    void shouldDeleteSubscription() throws Exception {
        Subscription subscription = new Subscription();
        subscription.setUser(user);
        subscription.setBaseCurrency("USD");
        subscription.setTargetCurrency("PLN");
        subscription.setThresholdPercent(new BigDecimal("1.100"));
        subscription = subscriptionRepository.save(subscription);

        mockMvc.perform(delete("/api/subscriptions/{id}", subscription.getId()))
                .andExpect(status().isNoContent());

        assertThat(subscriptionRepository.existsById(subscription.getId())).isFalse();
    }
}
