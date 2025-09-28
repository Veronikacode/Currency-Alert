package pl.coderslab.currencyalert;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import pl.coderslab.provider.entity.CurrencyRate;
import pl.coderslab.provider.entity.Notification;
import pl.coderslab.provider.entity.Subscription;
import pl.coderslab.provider.entity.User;
import pl.coderslab.provider.enums.NotificationStatus;
import pl.coderslab.provider.repository.CurrencyRateRepository;
import pl.coderslab.provider.repository.NotificationRepository;
import pl.coderslab.provider.repository.SubscriptionRepository;
import pl.coderslab.provider.repository.UserRepository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = RepositoryTestConfiguration.class)
class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CurrencyRateRepository currencyRateRepository;

    @Test
    void shouldRetrieveNotificationsByStatus() {
        User user = new User();
        user.setEmail("notify@example.com");
        user.setPasswordHash("hash");
        user = userRepository.save(user);

        Subscription subscription = new Subscription();
        subscription.setUser(user);
        subscription.setBaseCurrency("USD");
        subscription.setTargetCurrency("CHF");
        subscription.setThresholdPercent(new BigDecimal("1.500"));
        subscription = subscriptionRepository.save(subscription);

        CurrencyRate rate = new CurrencyRate();
        rate.setBaseCurrency("USD");
        rate.setCurrencyCode("CHF");
        rate.setRate(new BigDecimal("4.123400"));
        rate.setTimestamp(OffsetDateTime.now());
        rate = currencyRateRepository.save(rate);

        Notification pending = new Notification();
        pending.setSubscription(subscription);
        pending.setCurrencyRate(rate);
        pending.setMessage("CHF crossed threshold");
        notificationRepository.save(pending);

        Notification sent = new Notification();
        sent.setSubscription(subscription);
        sent.setCurrencyRate(rate);
        sent.setStatus(NotificationStatus.SENT);
        sent.setSentAt(OffsetDateTime.now());
        sent.setMessage("Sent notification");
        notificationRepository.save(sent);

        List<Notification> pendingNotifications = notificationRepository.findByStatus(NotificationStatus.PENDING);
        assertThat(pendingNotifications)
                .hasSize(1)
                .first()
                .extracting(Notification::getMessage)
                .isEqualTo("CHF crossed threshold");

        List<Notification> sentNotifications = notificationRepository.findByStatus(NotificationStatus.SENT);
        assertThat(sentNotifications)
                .hasSize(1)
                .first()
                .extracting(Notification::getStatus)
                .isEqualTo(NotificationStatus.SENT);
    }
}
