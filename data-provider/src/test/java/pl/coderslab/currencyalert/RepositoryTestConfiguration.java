package pl.coderslab.currencyalert;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration(proxyBeanMethods = false)
@EnableAutoConfiguration
@EntityScan(basePackages = "pl.coderslab.provider.entity")
@EnableJpaRepositories(basePackages = "pl.coderslab.provider.repository")
class RepositoryTestConfiguration {
}
