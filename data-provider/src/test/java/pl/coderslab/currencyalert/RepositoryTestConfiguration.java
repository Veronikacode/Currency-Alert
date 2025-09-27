package pl.coderslab.currencyalert;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootConfiguration(proxyBeanMethods = false)
@EnableAutoConfiguration
@EntityScan(basePackages = "pl.coderslab.provider.entity")
@EnableJpaRepositories(basePackages = "pl.coderslab.provider.repository")
public class RepositoryTestConfiguration {
}
