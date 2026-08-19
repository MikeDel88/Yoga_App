package com.openclassrooms.starterjwt.controllers.session;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclassrooms.starterjwt.controllers.SessionController;
import com.openclassrooms.starterjwt.dto.SessionDto;
import com.openclassrooms.starterjwt.mapper.SessionMapper;
import com.openclassrooms.starterjwt.security.WebSecurityConfig;
import com.openclassrooms.starterjwt.security.jwt.JwtUtils;
import com.openclassrooms.starterjwt.security.services.UserDetailsImpl;
import com.openclassrooms.starterjwt.security.services.UserDetailsServiceImpl;
import com.openclassrooms.starterjwt.services.SessionService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Date;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.openclassrooms.starterjwt.testsupport.ParameterizedValidationSupport.invalidCase;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SessionController.class)
@Import(WebSecurityConfig.class)
@ActiveProfiles("test")
public class SessionControllerValidationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SessionService sessionService;

    @MockitoBean
    private SessionMapper sessionMapper;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @MockitoBean
    private JwtUtils jwtUtils;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static UserDetailsImpl authenticatedUser() {
        return UserDetailsImpl.builder()
                .id(1L)
                .username("testeur@test.com")
                .firstName("test")
                .lastName("test")
                .admin(false)
                .password("test!31")
                .build();
    }

    private static SessionDto validSessionDto() {
        SessionDto sessionDto = new SessionDto();
        sessionDto.setName("Yoga Session");
        sessionDto.setDate(new Date());
        sessionDto.setTeacherId(1L);
        sessionDto.setDescription("Yoga session description");
        return sessionDto;
    }

    static Stream<Arguments> invalidSessionRequests() {
        return Stream.of(
                invalidCase("name is blank", (SessionDto sessionDto) -> sessionDto.setName(" ")),
                invalidCase("name too long", (SessionDto sessionDto) -> sessionDto.setName("n".repeat(51))),
                invalidCase("date is null", (SessionDto sessionDto) -> sessionDto.setDate(null)),
                invalidCase("teacher_id is null", (SessionDto sessionDto) -> sessionDto.setTeacherId(null)),
                invalidCase("description is null", (SessionDto sessionDto) -> sessionDto.setDescription(null)),
                invalidCase("description too long", (SessionDto sessionDto) -> sessionDto.setDescription("d".repeat(2501)))
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidSessionRequests")
    public void create_invalidSessionRequest_returnsBadRequest(Consumer<SessionDto> mutation) throws Exception {
        SessionDto sessionDto = validSessionDto();
        mutation.accept(sessionDto);

        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/api/session")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sessionDto))
                .with(user(authenticatedUser()));

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
