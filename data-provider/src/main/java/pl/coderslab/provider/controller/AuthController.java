package pl.coderslab.provider.controller;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pl.coderslab.provider.dto.AuthRequest;
import pl.coderslab.provider.dto.AuthResponse;
import pl.coderslab.provider.service.UserService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Rejestracja nowych użytkowników oraz logowanie z generowaniem tokenów JWT")
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Rejestracja użytkownika", description = "Tworzy konto i zwraca token JWT dla nowego użytkownika")
    public AuthResponse register(@Valid @RequestBody AuthRequest request) {
        return userService.register(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Logowanie użytkownika", description = "Weryfikuje dane logowania i zwraca token JWT")
    public AuthResponse login(@Valid @RequestBody AuthRequest request) {
        return userService.authenticate(request);
    }
}
