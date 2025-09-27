package pl.coderslab.currencyalert;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.server.ResponseStatusException;
import pl.coderslab.provider.dto.AuthRequest;
import pl.coderslab.provider.dto.AuthResponse;
import pl.coderslab.provider.entity.User;
import pl.coderslab.provider.repository.UserRepository;
import pl.coderslab.provider.service.JwtService;
import pl.coderslab.provider.service.UserService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private UserService userService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @Captor
    private ArgumentCaptor<UserDetails> userDetailsCaptor;

    @Captor
    private ArgumentCaptor<UsernamePasswordAuthenticationToken> authenticationTokenCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldRegisterNewUser() {
        AuthRequest request = new AuthRequest("test@example.com", "password123");
        User saved = new User();
        saved.setEmail(request.email());
        saved.setPasswordHash("encoded");

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(saved);
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("jwt-token");

        AuthResponse response = userService.register(request);

        assertThat(response.token()).isEqualTo("jwt-token");
        verify(userRepository).save(userCaptor.capture());
        User persisted = userCaptor.getValue();
        assertThat(persisted.getEmail()).isEqualTo(request.email());
        assertThat(persisted.getPasswordHash()).isEqualTo("encoded");
        verify(jwtService).generateToken(userDetailsCaptor.capture());
        assertThat(userDetailsCaptor.getValue().getUsername()).isEqualTo(request.email());
    }

    @Test
    void shouldRejectRegistrationWhenEmailAlreadyUsed() {
        AuthRequest request = new AuthRequest("duplicate@example.com", "password123");
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void shouldAuthenticateExistingUser() {
        AuthRequest request = new AuthRequest("user@example.com", "secretpass");
        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash("hash");

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("jwt-token");

        AuthResponse response = userService.authenticate(request);

        assertThat(response.token()).isEqualTo("jwt-token");
        verify(authenticationManager).authenticate(authenticationTokenCaptor.capture());
        UsernamePasswordAuthenticationToken token = authenticationTokenCaptor.getValue();
        assertThat(token.getPrincipal()).isEqualTo(request.email());
        assertThat(token.getCredentials()).isEqualTo(request.password());
    }

    @Test
    void shouldRejectAuthenticationWhenUserMissing() {
        AuthRequest request = new AuthRequest("missing@example.com", "password123");
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.authenticate(request))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
