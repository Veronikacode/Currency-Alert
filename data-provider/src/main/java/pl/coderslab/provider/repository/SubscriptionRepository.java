package pl.coderslab.provider.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.coderslab.provider.entity.Subscription;
import pl.coderslab.provider.entity.User;

import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByUser(User user);

    List<Subscription> findByTargetCurrencyAndActiveTrue(String targetCurrency);
}
