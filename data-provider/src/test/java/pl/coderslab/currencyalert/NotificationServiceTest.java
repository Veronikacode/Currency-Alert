package pl.coderslab.currencyalert;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import pl.coderslab.currencyalert.common.messaging.CurrencyRateChangeMessage;
import pl.coderslab.provider.entity.CurrencyRate;
import pl.coderslab.provider.entity.Notification;
import pl.coderslab.provider.entity.Subscription;
import pl.coderslab.provider.entity.User;
import pl.coderslab.provider.enums.NotificationStatus;
import pl.coderslab.provider.notification.NotificationContent;
import pl.coderslab.provider.notification.NotificationService;
import pl.coderslab.provider.notification.NotificationTemplateRenderer;
import pl.coderslab.provider.repository.NotificationRepository;
import pl.coderslab.provider.repository.SubscriptionRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private NotificationTemplateRenderer templateRenderer;
    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private NotificationService notificationService;

    private Subscription subscription;
    private CurrencyRate rate;
    private CurrencyRateChangeMessage message;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setEmail("alerts@example.com");

        subscription = new Subscription();
        subscription.setUser(user);
        subscription.setBaseCurrency("USD");
        subscription.setTargetCurrency("EUR");
        subscription.setThresholdPercent(new BigDecimal("1.000"));

        rate = new CurrencyRate();
        rate.setCurrencyCode("EUR");
        rate.setRate(new BigDecimal("4.123400"));
        rate.setTimestamp(OffsetDateTime.ofInstant(Instant.parse("2023-10-05T12:30:00Z"), ZoneOffset.UTC));

        message = new CurrencyRateChangeMessage(
                "usd",
                "eur",
                new BigDecimal("4.123400"),
                new BigDecimal("1.25"),
                Instant.parse("2023-10-05T12:30:00Z")
        );
    }

    @Test
    void shouldSendNotificationWhenThresholdMet() {
        when(subscriptionRepository.findByBaseCurrencyAndTargetCurrencyAndActiveTrue("USD", "EUR"))
                .thenReturn(List.of(subscription));
        when(templateRenderer.renderRateAlert(subscription, rate, message.changePercentage()))
                .thenReturn(new NotificationContent("subject", "body"));
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));

        int sent = notificationService.handleRateChange(rate, message);

        assertThat(sent).isEqualTo(1);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        Notification saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(saved.getSentAt()).isNotNull();
        assertThat(saved.getMessage()).isEqualTo("body");
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void shouldSkipNotificationWhenThresholdNotReached() {
        subscription.setThresholdPercent(new BigDecimal("5.000"));
        when(subscriptionRepository.findByBaseCurrencyAndTargetCurrencyAndActiveTrue("USD", "EUR"))
                .thenReturn(List.of(subscription));

        int sent = notificationService.handleRateChange(rate, message);

        assertThat(sent).isZero();
        verifyNoInteractions(notificationRepository);
        verifyNoInteractions(mailSender);
    }

    @Test
    void shouldMarkNotificationAsFailedWhenEmailDeliveryThrows() {
        when(subscriptionRepository.findByBaseCurrencyAndTargetCurrencyAndActiveTrue("USD", "EUR"))
                .thenReturn(List.of(subscription));
        when(templateRenderer.renderRateAlert(subscription, rate, message.changePercentage()))
                .thenReturn(new NotificationContent("subject", "body"));
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
        doThrow(new MailSendException("SMTP down")).when(mailSender).send(any(MimeMessage.class));

        int sent = notificationService.handleRateChange(rate, message);

        assertThat(sent).isZero();

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        Notification saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(saved.getSentAt()).isNull();
    }
}
