package com.openclassrooms.starterjwt.controllers.session;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclassrooms.starterjwt.dto.SessionDto;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.repository.UserRepository;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Date;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.openclassrooms.starterjwt.testsupport.ParameterizedValidationSupport.invalidCase;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class SessionControllerValidationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserDetailsService userDetailsService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private User setupUser() {
        return userRepository.save(new User("testeur@test.com", "test", "test",
                passwordEncoder.encode("test!31"), false));
    }

    private static SessionDto validSessionDto() {
        SessionDto sessionDto = new SessionDto();
        sessionDto.setName("Yoga Session");
        sessionDto.setDate(new Date());
        sessionDto.setTeacher_id(1L);
        sessionDto.setDescription("Yoga session description");
        return sessionDto;
    }

    static Stream<Arguments> invalidSessionRequests() {
        return Stream.of(
                invalidCase("name is blank", (SessionDto sessionDto) -> sessionDto.setName(" ")),
                invalidCase("name too long", (SessionDto sessionDto) -> sessionDto.setName("n".repeat(51))),
                invalidCase("date is null", (SessionDto sessionDto) -> sessionDto.setDate(null)),
                invalidCase("teacher_id is null", (SessionDto sessionDto) -> sessionDto.setTeacher_id(null)),
                invalidCase("description is null", (SessionDto sessionDto) -> sessionDto.setDescription(null)),
                invalidCase("description too long", (SessionDto sessionDto) -> sessionDto.setDescription("d".repeat(2501)))
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidSessionRequests")
    public void create_invalidSessionRequest_returnsBadRequest(Consumer<SessionDto> mutation) throws Exception {
        User savedUser = setupUser();

        SessionDto sessionDto = validSessionDto();
        mutation.accept(sessionDto);

        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/api/session")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sessionDto))
                .with(user(userDetailsService.loadUserByUsername(savedUser.getEmail())));

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
