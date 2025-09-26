package pl.coderslab.gatherer.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import pl.coderslab.currencyalert.common.messaging.CurrencyRateChangeMessage;
import pl.coderslab.gatherer.config.MessagingProperties;

@Component
public class CurrencyChangePublisher {

    private static final Logger logger = LoggerFactory.getLogger(CurrencyChangePublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final MessagingProperties messagingProperties;

    public CurrencyChangePublisher(RabbitTemplate rabbitTemplate, MessagingProperties messagingProperties) {
        this.rabbitTemplate = rabbitTemplate;
        this.messagingProperties = messagingProperties;
    }

    public void publish(CurrencyRateChangeMessage message) {
        rabbitTemplate.convertAndSend(messagingProperties.getExchange(),
                messagingProperties.getRoutingKey(),
                message);
        logger.debug("Published currency change message for {}", message.currency());
    }
}
