package io.spring.boot.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import io.spring.boot.entity.User;
import io.spring.boot.repository.UserRepository;
import io.spring.boot.security.JwtService;

@ExtendWith(MockitoExtension.class)
class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    UserDetailsService userDetailsService;

    @InjectMocks
    private UserService userService;

    @Test
    void register_shouldCreateUserWithEncodedPassword() {
        // given
        String email = "test@test.com";
        String rawPassword = "password";
        String encodedPassword = "ENCODED";

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);

        User savedUser = new User(email, encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // when
        User result = userService.register(email, rawPassword);

        // then
        assertEquals(email, result.getUsername());
        assertEquals(encodedPassword, result.getPassword());

        verify(passwordEncoder).encode(rawPassword);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_shouldFailIfEmailExists() {
        when(userRepository.existsByEmail("test@test.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            userService.register("test@test.com", "password");
        });
    }

    @Test
    void login_shouldSucceedWithCorrectPassword() {
        String email = "test@test.com";
        String rawPassword = "password";
        String encodedPassword = "ENCODED";

        User user = new User(email, encodedPassword);

        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(user);

        User result = userService.login(email, rawPassword);

        assertEquals(email, result.getUsername());
    }

    @Test
    void login_shouldFailWithWrongPassword() {
        String email = "test@test.com";
        String rawPassword = "password";
        String encodedPassword = "ENCODED";

        User user = new User(email, encodedPassword);

        when(userDetailsService.loadUserByUsername(email)).thenReturn(user);
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> {
            userService.login(email, rawPassword);
        });
    }

    @Test
    void generateToken_shouldDelegateToJwtService() {
        User user = new User("test@test.com", "pwd");

        when(jwtService.generateToken(user)).thenReturn("JWT_TOKEN");

        String token = userService.generateToken(user);

        assertEquals("JWT_TOKEN", token);
        verify(jwtService).generateToken(user);
    }
}

