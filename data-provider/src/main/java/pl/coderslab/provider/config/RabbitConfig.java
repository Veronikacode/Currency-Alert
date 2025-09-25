package pl.coderslab.provider.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.Queue;

@Configuration
public class RabbitConfig {

    @Bean
    public Queue currencyRatesQueue() {
        return new Queue("currency_rates", false);
    }
}
