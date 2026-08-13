package com.openclassrooms.starterjwt.controllers.session;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclassrooms.starterjwt.mapper.SessionMapper;
import com.openclassrooms.starterjwt.models.Session;
import com.openclassrooms.starterjwt.models.Teacher;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.repository.SessionRepository;
import com.openclassrooms.starterjwt.repository.TeacherRepository;
import com.openclassrooms.starterjwt.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.MethodArgumentNotValidException;

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

    private User setupUser() {
        return userRepository.save(new User("testeur@test.com", "test", "test",
                passwordEncoder.encode("test!31"), false));
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

    @Test
    public void findById_success() throws Exception {
        User savedUser = setupUser();
        Session savedSession = setupSession(true);

        mockMvc.perform(get("/api/session/" + savedSession.getId())
                        .with(user(userDetailsService.loadUserByUsername(savedUser.getEmail()))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedSession.getId()))
                .andExpect(jsonPath("$.name").value(savedSession.getName()))
                .andExpect(jsonPath("$.teacher_id").value(savedSession.getTeacher().getId()));
    }

    @Test
    public void findById_idIsNotNumber_returnsBadRequest() throws Exception {
        User savedUser = setupUser();

        mockMvc.perform(get("/api/session/test")
                        .with(user(userDetailsService.loadUserByUsername(savedUser.getEmail()))))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(result ->  assertThat(result.getResolvedException())
                        .isInstanceOf(NumberFormatException.class));
    }

    @Test
    public void findById_sessionNotExist_returnsNotFound() throws Exception {
        User savedUser = setupUser();

        mockMvc.perform(get("/api/session/1")
                        .with(user(userDetailsService.loadUserByUsername(savedUser.getEmail()))))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void findAll_success() throws Exception {
        User savedUser = setupUser();
        setupSession(true);
        setupSession(true);
        setupSession(true);

        mockMvc.perform(get("/api/session")
                        .with(user(userDetailsService.loadUserByUsername(savedUser.getEmail()))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    public void findAll_sessionsEmpty_returnsArrayEmpty() throws Exception {
        User savedUser = setupUser();

        mockMvc.perform(get("/api/session")
                        .with(user(userDetailsService.loadUserByUsername(savedUser.getEmail()))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    public void create_session_success() throws Exception {
        User savedUser = setupUser();
        Session session = setupSession(false);

        mockMvc.perform(post("/api/session")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sessionMapper.toDto(session)))
                .with(user(userDetailsService.loadUserByUsername(savedUser.getEmail()))))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void update_session_success() throws Exception {
        User savedUser = setupUser();
        Session session = setupSession(true);
        session.setName("Yoga Session !!");

        mockMvc.perform(put("/api/session/" + session.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessionMapper.toDto(session)))
                        .with(user(userDetailsService.loadUserByUsername(savedUser.getEmail()))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Yoga Session !!"));
    }

    @Test
    public void delete_session_success() throws Exception {
        User savedUser = setupUser();
        Session session = setupSession(true);

        mockMvc.perform(delete("/api/session/" + session.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(userDetailsService.loadUserByUsername(savedUser.getEmail()))))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void delete_sessionNotExist_returnsNotFound() throws Exception {
        User savedUser = setupUser();

        mockMvc.perform(delete("/api/session/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(userDetailsService.loadUserByUsername(savedUser.getEmail()))))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void participate_session_success() throws Exception {
        User savedUser = setupUser();
        Session session = setupSession(true);

        mockMvc.perform(post("/api/session/" + session.getId() + "/participate/" + savedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(userDetailsService.loadUserByUsername(savedUser.getEmail()))))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void participate_sessionAlreadyParticipate_returnsBadRequest() throws Exception {
        User savedUser = setupUser();
        Session session = setupSession(false);
        session.setUsers(List.of(savedUser));
        sessionRepository.save(session);

        mockMvc.perform(post("/api/session/" + session.getId() + "/participate/" + savedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(userDetailsService.loadUserByUsername(savedUser.getEmail()))))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void participate_sessionNotExist_returnsNotFound() throws Exception {
        User savedUser = setupUser();

        mockMvc.perform(post("/api/session/1/participate/" + savedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(userDetailsService.loadUserByUsername(savedUser.getEmail()))))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void participate_userNotExist_returnsNotFound() throws Exception {
        User savedUser = setupUser();
        Session session = setupSession(true);

        mockMvc.perform(post("/api/session/" + session.getId() + "/participate/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(userDetailsService.loadUserByUsername(savedUser.getEmail()))))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void unparticipate_session_success() throws Exception {
        User savedUser = setupUser();
        Session session = setupSession(false);
        session.setUsers(List.of(savedUser));
        sessionRepository.save(session);

        mockMvc.perform(delete("/api/session/" + session.getId() + "/participate/" + savedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(userDetailsService.loadUserByUsername(savedUser.getEmail()))))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void unparticipate_sessionNotParticipate_returnsBadRequest() throws Exception {
        User savedUser = setupUser();
        Session session = setupSession(true);

        mockMvc.perform(delete("/api/session/" + session.getId() + "/participate/" + savedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(userDetailsService.loadUserByUsername(savedUser.getEmail()))))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void unparticipate_sessionNotExist_returnsNotFound() throws Exception {
        User savedUser = setupUser();

        mockMvc.perform(delete("/api/session/1/participate/" + savedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(userDetailsService.loadUserByUsername(savedUser.getEmail()))))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

}
