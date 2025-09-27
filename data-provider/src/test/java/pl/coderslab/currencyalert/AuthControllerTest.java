package pl.coderslab.currencyalert;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import pl.coderslab.provider.DataProviderApplication;
import pl.coderslab.provider.dto.AuthRequest;
import pl.coderslab.provider.entity.User;
import pl.coderslab.provider.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = DataProviderApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @AfterEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("should register a new user and return JWT token")
    void shouldRegisterUser() throws Exception {
        AuthRequest request = new AuthRequest("register@example.com", "strongPassword1");

        mockMvc.perform(post("/api/auth/register").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty());

        User saved = userRepository.findByEmail(request.email()).orElseThrow();
        assertThat(passwordEncoder.matches(request.password(), saved.getPasswordHash())).isTrue();
    }

    @Test
    @DisplayName("should reject registration when email already exists")
    void shouldRejectDuplicateRegistration() throws Exception {
        User existing = new User();
        existing.setEmail("duplicate@example.com");
        existing.setPasswordHash(passwordEncoder.encode("somePassword1"));
        userRepository.save(existing);

        AuthRequest request = new AuthRequest(existing.getEmail(), "differentPass2");

        mockMvc.perform(post("/api/auth/register").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("should authenticate existing user and return token")
    void shouldAuthenticateUser() throws Exception {
        User existing = new User();
        existing.setEmail("login@example.com");
        existing.setPasswordHash(passwordEncoder.encode("TopSecret3"));
        userRepository.save(existing);

        AuthRequest request = new AuthRequest(existing.getEmail(), "TopSecret3");

        mockMvc.perform(post("/api/auth/login").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("should return 401 when credentials are invalid")
    void shouldRejectInvalidCredentials() throws Exception {
        AuthRequest request = new AuthRequest("missing@example.com", "doesNotMatter4");

        mockMvc.perform(post("/api/auth/login").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}