package com.openclassrooms.starterjwt.controllers.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclassrooms.starterjwt.payload.request.LoginRequest;
import com.openclassrooms.starterjwt.payload.request.SignupRequest;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AuthControllerValidationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static SignupRequest validSignupRequest() {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setEmail("testeur@test.com");
        signupRequest.setFirstName("test");
        signupRequest.setLastName("test");
        signupRequest.setPassword("test!31");
        return signupRequest;
    }

    private static LoginRequest validLoginRequest() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("testeur@test.com");
        loginRequest.setPassword("test!31");
        return loginRequest;
    }

    private static <T> Arguments invalidCase(String name, Consumer<T> mutation) {
        return Arguments.of(Named.of(name, mutation));
    }

    static Stream<Arguments> invalidSignupRequests() {
        return Stream.of(
                invalidCase("email is blank", (SignupRequest r) -> r.setEmail(" ")),
                invalidCase("wrong email", (SignupRequest r) -> r.setEmail("testeur.test.com")),
                invalidCase("email too long", (SignupRequest r) -> r.setEmail("t".repeat(50) + ".test.com")),
                invalidCase("first name is blank", (SignupRequest r) -> r.setFirstName(" ")),
                invalidCase("first name too short", (SignupRequest r) -> r.setFirstName("te")),
                invalidCase("first name too long", (SignupRequest r) -> r.setFirstName("t".repeat(21))),
                invalidCase("last name is blank", (SignupRequest r) -> r.setLastName(" ")),
                invalidCase("last name too short", (SignupRequest r) -> r.setLastName("te")),
                invalidCase("last name too long", (SignupRequest r) -> r.setLastName("t".repeat(21))),
                invalidCase("password is blank", (SignupRequest r) -> r.setPassword(" ")),
                invalidCase("password too short", (SignupRequest r) -> r.setPassword("test")),
                invalidCase("password too long", (SignupRequest r) -> r.setPassword("t".repeat(41)))
        );
    }

    static Stream<Arguments> invalidLoginRequests() {
        return Stream.of(
                invalidCase("email is blank", (LoginRequest r) -> r.setEmail(" ")),
                invalidCase("password is blank", (LoginRequest r) -> r.setPassword(" "))
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidSignupRequests")
    public void register_invalidSignupRequest_returnsBadRequest(Consumer<SignupRequest> mutation) throws Exception {
        SignupRequest signupRequest = validSignupRequest();
        mutation.accept(signupRequest);

        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(result ->
                        assertThat(result.getResponse().getErrorMessage())
                                .isEqualTo("Invalid request content.")
                )
                .andExpect(result ->
                        assertThat(result.getResolvedException())
                                .isInstanceOf(MethodArgumentNotValidException.class)
                );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidLoginRequests")
    public void login_invalidLoginRequest_returnsBadRequest(Consumer<LoginRequest> mutation) throws Exception {
        LoginRequest loginRequest = validLoginRequest();
        mutation.accept(loginRequest);

        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(result ->
                        assertThat(result.getResponse().getErrorMessage())
                                .isEqualTo("Invalid request content.")
                )
                .andExpect(result ->
                        assertThat(result.getResolvedException())
                                .isInstanceOf(MethodArgumentNotValidException.class)
                );
    }
}
