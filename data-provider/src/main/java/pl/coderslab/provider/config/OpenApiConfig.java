package pl.coderslab.provider.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Alarm Walutowy API",
                version = "v1",
                description = "REST API serwisu DataProvider umożliwiające rejestrację użytkowników, zarządzanie subskrypcjami " +
                        "oraz przegląd historii kursów walut.",
                contact = @Contact(name = "Alarm Walutowy", url = "https://github.com/Veronikacode/")),
        security = @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
)
@SecurityScheme(
        name = OpenApiConfig.BEARER_SCHEME,
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {

    public static final String BEARER_SCHEME = "bearerAuth";
}