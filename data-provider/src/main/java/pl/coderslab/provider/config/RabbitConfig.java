package pl.coderslab.provider.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    public TopicExchange providerCurrencyAlertExchange(
            @Value("${app.messaging.exchange:currency.alerts.exchange}") String exchangeName) {
        return new TopicExchange(exchangeName);
    }

    @Bean
    public Queue providerCurrencyAlertQueue(
            @Value("${app.messaging.queue:currency.alerts.queue}") String queueName) {
        return new Queue(queueName, true);
    }

    @Bean
    public Binding providerCurrencyAlertBinding(
            TopicExchange providerCurrencyAlertExchange,
            Queue providerCurrencyAlertQueue,
            @Value("${app.messaging.routing-key:currency.alerts.rate-change}") String routingKey) {
        return BindingBuilder.bind(providerCurrencyAlertQueue)
                .to(providerCurrencyAlertExchange)
                .with(routingKey);
    }

    @Bean
    public Jackson2JsonMessageConverter providerMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            SimpleRabbitListenerContainerFactoryConfigurer configurer,
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter providerMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setMessageConverter(providerMessageConverter);
        return factory;
    }
}