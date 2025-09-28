package pl.coderslab.provider.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import pl.coderslab.provider.entity.CurrencyRate;
import pl.coderslab.provider.entity.Subscription;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.math.RoundingMode;

@Component
@RequiredArgsConstructor
public class NotificationTemplateRenderer {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm 'UTC'")
            .withLocale(Locale.UK);

    private final TemplateEngine templateEngine;

    public NotificationContent renderRateAlert(Subscription subscription,
                                               CurrencyRate rate,
                                               BigDecimal changePercent) {
        BigDecimal formattedChange = changePercent.setScale(2, RoundingMode.HALF_UP);
        String baseCurrency = rate.getBaseCurrency() != null ? rate.getBaseCurrency() : subscription.getBaseCurrency();

        Context context = new Context();
        context.setVariable("baseCurrency", baseCurrency);
        context.setVariable("targetCurrency", subscription.getTargetCurrency());
        context.setVariable("threshold", subscription.getThresholdPercent());
        context.setVariable("changePercent", changePercent);
        context.setVariable("formattedChangePercent", formattedChange.toPlainString());
        context.setVariable("rate", rate.getRate());
        context.setVariable("timestamp", rate.getTimestamp().format(TIMESTAMP_FORMATTER));

        String body = templateEngine.process("notification-email", context);
        String subject = String.format(Locale.ENGLISH,
                "Alert: %s/%s moved by %s%%",
                baseCurrency,
                subscription.getTargetCurrency(),
                formattedChange.toPlainString());
        return new NotificationContent(subject, body);
    }
}