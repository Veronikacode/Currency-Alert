package pl.coderslab.provider.controller;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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

import static pl.coderslab.provider.config.OpenApiConfig.BEARER_SCHEME;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
@Tag(name = "Subskrypcje", description = "Zarządzanie progami powiadomień dla obserwowanych walut")
@SecurityRequirement(name = BEARER_SCHEME)
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping
    @Operation(summary = "Lista subskrypcji", description = "Zwraca subskrypcje zalogowanego użytkownika")
    public List<SubscriptionDto> list(@AuthenticationPrincipal(expression = "username") String email) {
        return subscriptionService.getSubscriptionsForUser(email);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Tworzenie subskrypcji", description = "Dodaje nową subskrypcję z progiem procentowym")
    public SubscriptionDto create(@AuthenticationPrincipal(expression = "username") String email,
                                  @Valid @RequestBody SubscriptionDto request) {
        return subscriptionService.createSubscription(email, request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Aktualizacja subskrypcji", description = "Modyfikuje istniejącą subskrypcję użytkownika")
    public SubscriptionDto update(@AuthenticationPrincipal(expression = "username") String email,
                                  @PathVariable Long id,
                                  @Valid @RequestBody SubscriptionDto request) {
        return subscriptionService.updateSubscription(email, id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Usuwanie subskrypcji", description = "Usuwa subskrypcję należącą do zalogowanego użytkownika")
    public void delete(@AuthenticationPrincipal(expression = "username") String email,
                       @PathVariable Long id) {
        subscriptionService.deleteSubscription(email, id);
    }
}