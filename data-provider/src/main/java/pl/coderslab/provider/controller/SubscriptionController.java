package pl.coderslab.provider.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pl.coderslab.provider.dto.SubscriptionDto;
import pl.coderslab.provider.service.SubscriptionService;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping
    public List<SubscriptionDto> list(@AuthenticationPrincipal(expression = "username") String email) {
        return subscriptionService.getSubscriptionsForUser(email);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SubscriptionDto create(@AuthenticationPrincipal(expression = "username") String email,
                                  @Valid @RequestBody SubscriptionDto request) {
        return subscriptionService.createSubscription(email, request);
    }

    @PutMapping("/{id}")
    public SubscriptionDto update(@AuthenticationPrincipal(expression = "username") String email,
                                  @PathVariable Long id,
                                  @Valid @RequestBody SubscriptionDto request) {
        return subscriptionService.updateSubscription(email, id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal(expression = "username") String email,
                       @PathVariable Long id) {
        subscriptionService.deleteSubscription(email, id);
    }
}