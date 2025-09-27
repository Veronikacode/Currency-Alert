package pl.coderslab.provider.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pl.coderslab.provider.dto.SubscriptionDto;
import pl.coderslab.provider.entity.Subscription;
import pl.coderslab.provider.entity.User;
import pl.coderslab.provider.repository.SubscriptionRepository;
import pl.coderslab.provider.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<SubscriptionDto> getSubscriptionsForUser(String email) {
        User user = requireUser(email);
        return subscriptionRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public SubscriptionDto createSubscription(String email, SubscriptionDto request) {
        User user = requireUser(email);
        Subscription subscription = new Subscription();
        subscription.setUser(user);
        apply(subscription, request);
        Subscription saved = subscriptionRepository.save(subscription);
        return toDto(saved);
    }

    public SubscriptionDto updateSubscription(String email, Long id, SubscriptionDto request) {
        Subscription subscription = requireSubscription(id);
        ensureOwnership(subscription, email);
        apply(subscription, request);
        return toDto(subscription);
    }

    public void deleteSubscription(String email, Long id) {
        Subscription subscription = requireSubscription(id);
        ensureOwnership(subscription, email);
        subscriptionRepository.delete(subscription);
    }

    private void apply(Subscription subscription, SubscriptionDto dto) {
        subscription.setBaseCurrency(dto.baseCurrency().toUpperCase());
        subscription.setTargetCurrency(dto.targetCurrency().toUpperCase());
        subscription.setThresholdPercent(dto.thresholdPercent());
        boolean active = dto.active() != null ? dto.active() : subscription.isActive();
        subscription.setActive(active);
    }

    private SubscriptionDto toDto(Subscription subscription) {
        return new SubscriptionDto(
                subscription.getId(),
                subscription.getBaseCurrency(),
                subscription.getTargetCurrency(),
                subscription.getThresholdPercent(),
                subscription.isActive(),
                subscription.getCreatedAt()
        );
    }

    private User requireUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private Subscription requireSubscription(Long id) {
        return subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subscription not found"));
    }

    private void ensureOwnership(Subscription subscription, String email) {
        if (!subscription.getUser().getEmail().equalsIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Subscription does not belong to user");
        }
    }
}
