package pl.coderslab.provider.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import pl.coderslab.currencyalert.common.messaging.CurrencyRateChangeMessage;
import pl.coderslab.provider.entity.CurrencyRate;
import pl.coderslab.provider.notification.NotificationService;
import pl.coderslab.provider.service.CurrencyRateService;

@Service
public class CurrencyRateListener {

    private static final Logger log = LoggerFactory.getLogger(CurrencyRateListener.class);

    private final CurrencyRateService currencyRateService;
    private final NotificationService notificationService;

    public CurrencyRateListener(CurrencyRateService currencyRateService, NotificationService notificationService) {
        this.currencyRateService = currencyRateService;
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = "#{currencyAlertQueue.name}")
    public void receiveMessage(CurrencyRateChangeMessage message) {
        CurrencyRate saved = currencyRateService.recordRateChange(message);
        notificationService.handleRateChange(saved, message);
        log.debug("Stored rate update for {}", message.currency());
    }
}