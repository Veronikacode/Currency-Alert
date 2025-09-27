package pl.coderslab.provider;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "pl.coderslab")
@EnableRabbit
public class DataProviderApplication {
    public static void main(String[] args) {
        SpringApplication.run(DataProviderApplication.class, args);
    }
}
