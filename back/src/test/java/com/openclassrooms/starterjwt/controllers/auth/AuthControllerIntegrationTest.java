package com.openclassrooms.starterjwt.controllers.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.payload.request.LoginRequest;
import com.openclassrooms.starterjwt.payload.request.SignupRequest;
import com.openclassrooms.starterjwt.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void register_success() throws Exception {
        mockMvc.perform(postJson(getUriRegister(), validSignupRequest()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered successfully!"));

        User savedUser = userRepository.findByEmail("testeur@test.com").orElse(null);
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getPassword()).isNotEqualTo("test!31");
    }

    @Test
    public void register_emailAlreadyTaken_returnsBadRequest() throws Exception {
        persistUser();

        mockMvc.perform(postJson(getUriRegister(), validSignupRequest()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error: Email is already taken!"));
    }

    @Test
    public void login_wrongPassword_returnsUnauthorized() throws Exception {
        persistUser();

        mockMvc.perform(postJson(getUriLogin(), loginRequest("wrong-password")))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void login_success() throws Exception {
        persistUser();

        mockMvc.perform(postJson(getUriLogin(), loginRequest("test!31")))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.username").value("testeur@test.com"))
                .andExpect(jsonPath("$.firstName").value("test"))
                .andExpect(jsonPath("$.lastName").value("test"))
                .andExpect(jsonPath("$.admin").value(false));
    }

    private SignupRequest validSignupRequest() {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setEmail("testeur@test.com");
        signupRequest.setFirstName("test");
        signupRequest.setLastName("test");
        signupRequest.setPassword("test!31");
        return signupRequest;
    }

    private LoginRequest loginRequest(String password) {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("testeur@test.com");
        loginRequest.setPassword(password);
        return loginRequest;
    }

    private void persistUser() {
        userRepository.save(new User("testeur@test.com", "test", "test",
                passwordEncoder.encode("test!31"), false));
    }

    private String getUriRegister() {
        return "/api/auth/register";
    }

    private String getUriLogin() {
        return "/api/auth/login";
    }

    private RequestBuilder postJson(String uri, Object body) throws Exception {
        return MockMvcRequestBuilders
                .post(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body));
    }
}
