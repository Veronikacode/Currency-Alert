package pl.coderslab.currencyalert;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import pl.coderslab.provider.entity.Subscription;
import pl.coderslab.provider.entity.User;
import pl.coderslab.provider.repository.SubscriptionRepository;
import pl.coderslab.provider.repository.UserRepository;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = RepositoryTestConfiguration.class)
class SubscriptionRepositoryTest {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldFindActiveSubscriptionsForTargetCurrency() {
        User user = new User();
        user.setEmail("subscriber@example.com");
        user.setPasswordHash("hash");
        user = userRepository.save(user);

        Subscription eurSubscription = new Subscription();
        eurSubscription.setUser(user);
        eurSubscription.setBaseCurrency("USD");
        eurSubscription.setTargetCurrency("EUR");
        eurSubscription.setThresholdPercent(new BigDecimal("1.250"));
        subscriptionRepository.save(eurSubscription);

        Subscription inactive = new Subscription();
        inactive.setUser(user);
        inactive.setBaseCurrency("USD");
        inactive.setTargetCurrency("EUR");
        inactive.setThresholdPercent(new BigDecimal("2.000"));
        inactive.setActive(false);
        subscriptionRepository.save(inactive);

        Subscription gbpSubscription = new Subscription();
        gbpSubscription.setUser(user);
        gbpSubscription.setBaseCurrency("USD");
        gbpSubscription.setTargetCurrency("GBP");
        gbpSubscription.setThresholdPercent(new BigDecimal("1.000"));
        subscriptionRepository.save(gbpSubscription);

        List<Subscription> eurResults = subscriptionRepository.findByTargetCurrencyAndActiveTrue("EUR");
        assertThat(eurResults)
                .hasSize(1)
                .first()
                .extracting(Subscription::getThresholdPercent)
                .isEqualTo(new BigDecimal("1.250"));

        List<Subscription> userSubscriptions = subscriptionRepository.findByUser(user);
        assertThat(userSubscriptions).hasSize(3);
    }
}
