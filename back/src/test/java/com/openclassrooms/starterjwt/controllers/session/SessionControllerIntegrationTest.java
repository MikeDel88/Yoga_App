package com.openclassrooms.starterjwt.controllers.session;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclassrooms.starterjwt.mapper.SessionMapper;
import com.openclassrooms.starterjwt.models.Session;
import com.openclassrooms.starterjwt.models.Teacher;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.repository.SessionRepository;
import com.openclassrooms.starterjwt.repository.TeacherRepository;
import com.openclassrooms.starterjwt.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class SessionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SessionMapper sessionMapper;

    private User savedUser;

    private UserDetails savedUserDetails;

    @BeforeEach
    public void setupUser() {
        savedUser = userRepository.save(new User("testeur@test.com", "test", "test",
                passwordEncoder.encode("test!31"), false));
        savedUserDetails = userDetailsService.loadUserByUsername(savedUser.getEmail());
    }

    @Test
    public void findById_success() throws Exception {
        Session savedSession = setupSession(true);

        mockMvc.perform(get(getUriSession(savedSession.getId()))
                        .with(user(savedUserDetails)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedSession.getId()))
                .andExpect(jsonPath("$.name").value(savedSession.getName()))
                .andExpect(jsonPath("$.teacher_id").value(savedSession.getTeacher().getId()));
    }

    @Test
    public void findById_idIsNotNumber_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/session/test")
                        .with(user(savedUserDetails)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(result ->  assertThat(result.getResolvedException())
                        .isInstanceOf(NumberFormatException.class));
    }

    @Test
    public void findById_sessionNotExist_returnsNotFound() throws Exception {
        mockMvc.perform(get(getUriSession(1L))
                        .with(user(savedUserDetails)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void findAll_success() throws Exception {
        setupSession(true);
        setupSession(true);
        setupSession(true);

        mockMvc.perform(get(getUriSessions())
                        .with(user(savedUserDetails)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    public void findAll_sessionsEmpty_returnsArrayEmpty() throws Exception {
        mockMvc.perform(get(getUriSessions())
                        .with(user(savedUserDetails)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    public void create_session_success() throws Exception {
        Session session = setupSession(false);

        mockMvc.perform(post(getUriSessions())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sessionMapper.toDto(session)))
                .with(user(userDetailsService.loadUserByUsername(savedUser.getEmail()))))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void update_session_success() throws Exception {
        Session session = setupSession(true);
        session.setName("Yoga Session !!");

        mockMvc.perform(put(getUriSession(session.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessionMapper.toDto(session)))
                        .with(user(savedUserDetails)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Yoga Session !!"));
    }

    @Test
    public void delete_session_success() throws Exception {
        Session session = setupSession(true);

        mockMvc.perform(delete(getUriSession(session.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(savedUserDetails)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void delete_sessionNotExist_returnsNotFound() throws Exception {
        mockMvc.perform(delete(getUriSession(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(savedUserDetails)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void participate_session_success() throws Exception {
        Session session = setupSession(true);

        mockMvc.perform(post(getUriParticipate(session.getId(),savedUser.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(savedUserDetails)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void participate_sessionAlreadyParticipate_returnsBadRequest() throws Exception {
        Session session = setupSession(false);
        session.setUsers(List.of(savedUser));
        sessionRepository.save(session);

        mockMvc.perform(post(getUriParticipate(session.getId(),savedUser.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(savedUserDetails)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void participate_sessionNotExist_returnsNotFound() throws Exception {
        mockMvc.perform(post(getUriParticipate(1L,savedUser.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(savedUserDetails)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }



    @Test
    public void participate_userNotExist_returnsNotFound() throws Exception {
        Session session = setupSession(true);

        mockMvc.perform(post(getUriParticipate(session.getId(),2L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(savedUserDetails)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void unparticipate_session_success() throws Exception {
        Session session = setupSession(false);
        session.setUsers(List.of(savedUser));
        sessionRepository.save(session);

        mockMvc.perform(delete(getUriParticipate(session.getId(),savedUser.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(savedUserDetails)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void unparticipate_sessionNotParticipate_returnsBadRequest() throws Exception {
        Session session = setupSession(true);

        mockMvc.perform(delete(getUriParticipate(session.getId(),savedUser.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(savedUserDetails)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void unparticipate_sessionNotExist_returnsNotFound() throws Exception {
        mockMvc.perform(delete(getUriParticipate(1L,savedUser.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(savedUserDetails)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    private String getUriParticipate(Long sessionId, Long userId) {
        return "/api/session/" + sessionId + "/participate/" + userId;
    }

    private String getUriSession(Long sessionId) {
        return "/api/session/" + sessionId;
    }

    private String getUriSessions() {
        return "/api/session";
    }

    private Teacher setupTeacher() {
        Teacher teacher = new Teacher();
        teacher.setFirstName("john");
        teacher.setLastName("doe");
        return teacherRepository.save(teacher);
    }

    private Session setupSession(boolean save) {
        Session session = new Session();
        session.setName("Yoga Session");
        session.setDate(new Date());
        session.setDescription("Yoga session description");
        session.setTeacher(setupTeacher());
        session.setUsers(new ArrayList<>());
        return save ? sessionRepository.save(session) : session;
    }


}
