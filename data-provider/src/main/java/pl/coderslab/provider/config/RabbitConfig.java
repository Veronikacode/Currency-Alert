package pl.coderslab.provider.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MessagingProperties.class)
public class RabbitConfig {

    @Bean
    public TopicExchange providerCurrencyAlertExchange(MessagingProperties properties) {
        return new TopicExchange(properties.getExchange());
    }

    @Bean
    public Queue providerCurrencyAlertQueue(MessagingProperties properties) {
        return new Queue(properties.getQueue(), true);
    }

    @Bean
    public Binding providerCurrencyAlertBinding(
            TopicExchange providerCurrencyAlertExchange,
            Queue providerCurrencyAlertQueue,
            MessagingProperties properties) {
        return BindingBuilder.bind(providerCurrencyAlertQueue)
                .to(providerCurrencyAlertExchange)
                .with(properties.getRoutingKey());
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