package pl.coderslab.provider.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.coderslab.currencyalert.common.messaging.CurrencyRateChangeMessage;
import pl.coderslab.provider.service.CurrencyRateService;
import pl.coderslab.provider.repository.CurrencyRateRepository;

@Service
public class CurrencyRateListener {

    private static final Logger log = LoggerFactory.getLogger(CurrencyRateListener.class);

    private final CurrencyRateService currencyRateService;

    @Autowired
    public CurrencyRateListener(CurrencyRateService currencyRateService) {
        this.currencyRateService = currencyRateService;
    }

    public CurrencyRateListener(CurrencyRateRepository repository) {
        this(new CurrencyRateService(repository));
    }

    @RabbitListener(queues = "#{currencyAlertQueue.name}")
    public void receiveMessage(CurrencyRateChangeMessage message) {
        currencyRateService.recordRateChange(message);
        log.debug("Stored rate update for {}", message.currency());
    }
}