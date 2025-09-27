package pl.coderslab.provider.notification;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.coderslab.currencyalert.common.messaging.CurrencyRateChangeMessage;
import pl.coderslab.provider.entity.CurrencyRate;
import pl.coderslab.provider.entity.Notification;
import pl.coderslab.provider.entity.Subscription;
import pl.coderslab.provider.enums.NotificationStatus;
import pl.coderslab.provider.repository.NotificationRepository;
import pl.coderslab.provider.repository.SubscriptionRepository;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final NotificationTemplateRenderer templateRenderer;
    private final JavaMailSender mailSender;

    /**
     * Processes an incoming rate change event by locating matching subscriptions and sending notifications.
     *
     * @param rate     persisted rate snapshot that triggered the alert
     * @param message  original message published by the data gatherer service
     * @return number of notifications that were successfully marked as sent
     */
    public int handleRateChange(CurrencyRate rate, CurrencyRateChangeMessage message) {
        BigDecimal changePercent = message.changePercentage();
        if (changePercent == null) {
            return 0;
        }

        String baseCurrency = normalize(message.baseCurrency());
        String targetCurrency = normalize(message.currency());

        if (baseCurrency == null || targetCurrency == null) {
            return 0;
        }

        List<Subscription> subscriptions = subscriptionRepository
                .findByBaseCurrencyAndTargetCurrencyAndActiveTrue(baseCurrency, targetCurrency);

        int sentCount = 0;
        for (Subscription subscription : subscriptions) {
            if (subscription.getThresholdPercent() == null
                    || changePercent.compareTo(subscription.getThresholdPercent()) < 0) {
                continue;
            }

            Notification notification = new Notification();
            notification.setCurrencyRate(rate);
            subscription.addNotification(notification);

            NotificationContent content = templateRenderer.renderRateAlert(subscription, rate, changePercent);
            notification.setMessage(content.body());
            notificationRepository.save(notification);

            try {
                sendEmail(subscription, content);
                notification.setStatus(NotificationStatus.SENT);
                notification.setSentAt(OffsetDateTime.now());
                sentCount++;
            } catch (MailException | MessagingException ex) {
                notification.setStatus(NotificationStatus.FAILED);
                log.warn("Failed to send notification for subscription {}: {}", subscription.getId(), ex.getMessage());
            }
        }
        return sentCount;
    }

    private void sendEmail(Subscription subscription, NotificationContent content)
            throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                StandardCharsets.UTF_8.name());
        helper.setTo(subscription.getUser().getEmail());
        helper.setSubject(content.subject());
        helper.setText(content.body(), true);
        mailSender.send(message);
    }

    private String normalize(String currency) {
        return currency == null ? null : currency.toUpperCase(Locale.ENGLISH);
    }
}