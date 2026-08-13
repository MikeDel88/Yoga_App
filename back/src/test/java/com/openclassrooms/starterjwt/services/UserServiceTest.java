package com.openclassrooms.starterjwt.services;

import com.openclassrooms.starterjwt.exception.EmailExistingException;
import com.openclassrooms.starterjwt.exception.NotFoundException;
import com.openclassrooms.starterjwt.exception.UnauthorizedRequestException;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.payload.request.LoginRequest;
import com.openclassrooms.starterjwt.payload.request.SignupRequest;
import com.openclassrooms.starterjwt.payload.response.JwtResponse;
import com.openclassrooms.starterjwt.repository.UserRepository;
import com.openclassrooms.starterjwt.security.jwt.JwtUtils;
import com.openclassrooms.starterjwt.security.services.UserDetailsImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtUtils jwtUtils;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    /**
     * Garantit l'isolation des tests : chaque test démarre avec un SecurityContextHolder vide,
     * indépendamment de ce que les tests précédents y ont mis.
     */
    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void authenticateUser_success() {
        UserDetailsImpl userDetails = UserDetailsImpl.builder()
                .id(1L)
                .username("test@test.com")
                .firstName("test")
                .lastName("test")
                .admin(true)
                .password("encoded-password")
                .build();
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        User user = new User("test@test.com", "test", "test", "encoded-password", true);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtUtils.generateJwtToken(authentication)).thenReturn("fake-jwt-token");
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@test.com");
        loginRequest.setPassword("test!31");

        JwtResponse jwtResponse = userService.authenticateUser(loginRequest);

        assertThat(jwtResponse.getToken()).isEqualTo("fake-jwt-token");
        assertThat(jwtResponse.getId()).isEqualTo(1L);
        assertThat(jwtResponse.getUsername()).isEqualTo("test@test.com");
        assertThat(jwtResponse.getFirstName()).isEqualTo("test");
        assertThat(jwtResponse.getLastName()).isEqualTo("test");
        assertThat(jwtResponse.getAdmin()).isTrue();
    }

    @Test
    public void authenticateUser_userNotFoundInDatabase_returnsNonAdminJwtResponse() {
        UserDetailsImpl userDetails = UserDetailsImpl.builder()
                .id(1L)
                .username("test@test.com")
                .firstName("test")
                .lastName("test")
                .admin(false)
                .password("encoded-password")
                .build();
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtUtils.generateJwtToken(authentication)).thenReturn("fake-jwt-token");
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@test.com");
        loginRequest.setPassword("test!31");

        JwtResponse jwtResponse = userService.authenticateUser(loginRequest);

        assertThat(jwtResponse.getToken()).isEqualTo("fake-jwt-token");
        assertThat(jwtResponse.getUsername()).isEqualTo("test@test.com");
        assertThat(jwtResponse.getAdmin()).isFalse();
    }

    @Test
    public void create_emailAlreadyTaken_throwsEmailExistingException() {
        when(userRepository.existsByEmail("test@test.com")).thenReturn(true);

        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setEmail("test@test.com");
        signupRequest.setFirstName("test");
        signupRequest.setLastName("test");
        signupRequest.setPassword("test!31");

        assertThatThrownBy(() -> userService.create(signupRequest))
                .isInstanceOf(EmailExistingException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    public void create_emailAvailable_encodesPasswordAndSaves() {
        when(userRepository.existsByEmail("test@test.com")).thenReturn(false);
        when(passwordEncoder.encode("test!31")).thenReturn("encoded-password");

        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setEmail("test@test.com");
        signupRequest.setFirstName("test");
        signupRequest.setLastName("test");
        signupRequest.setPassword("test!31");

        userService.create(signupRequest);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo("test@test.com");
        assertThat(savedUser.getFirstName()).isEqualTo("test");
        assertThat(savedUser.getLastName()).isEqualTo("test");
        assertThat(savedUser.getPassword()).isEqualTo("encoded-password");
        assertThat(savedUser.isAdmin()).isFalse();
    }

    @Test
    public void findById_found_returnsUser() {
        User user = new User("test@test.com", "test", "test", "encoded-password", false);
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.findById(1L);

        assertThat(result).isEqualTo(user);
    }

    @Test
    public void findById_notFound_throwsNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    public void delete_matchingAuthenticatedUser_deletesUser() {
        User user = new User("test@test.com", "test", "test", "encoded-password", false);
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDetailsImpl userDetails = UserDetailsImpl.builder()
                .id(1L)
                .username("test@test.com")
                .build();
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));

        userService.delete(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    public void delete_differentAuthenticatedUser_throwsUnauthorized() {
        User user = new User("test@test.com", "test", "test", "encoded-password", false);
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDetailsImpl userDetails = UserDetailsImpl.builder()
                .id(2L)
                .username("someone-else@test.com")
                .build();
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));

        assertThatThrownBy(() -> userService.delete(1L))
                .isInstanceOf(UnauthorizedRequestException.class);

        verify(userRepository, never()).deleteById(any());
    }
}
