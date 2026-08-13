package com.openclassrooms.starterjwt.controllers.teacher;

import com.openclassrooms.starterjwt.models.Teacher;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.repository.TeacherRepository;
import com.openclassrooms.starterjwt.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class TeacherControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserDetailsService userDetailsService;

    private User setupUser() {
        return userRepository.save(new User("testeur@test.com", "test", "test",
                passwordEncoder.encode("test!31"), false));
    }

    private Teacher setupTeacher() {
        Teacher teacher = new Teacher();
        teacher.setFirstName("John");
        teacher.setLastName("Doe");
        return teacherRepository.save(teacher);
    }

    @Test
    public void findById_success() throws Exception {
        User savedUser = setupUser();
        Teacher savedTeacher = setupTeacher();

        mockMvc.perform(get("/api/teacher/" + savedTeacher.getId())
                        .with(user(userDetailsService.loadUserByUsername(savedUser.getEmail()))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedTeacher.getId()))
                .andExpect(jsonPath("$.firstName").value(savedTeacher.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(savedTeacher.getLastName()));
    }

    @Test
    public void findById_teacherNotFound_returnsNotFound() throws Exception {
        User savedUser = setupUser();

        mockMvc.perform(get("/api/teacher/1")
                        .with(user(userDetailsService.loadUserByUsername(savedUser.getEmail()))))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void findAll_success() throws Exception {
        User savedUser = setupUser();
        setupTeacher();
        setupTeacher();

        mockMvc.perform(get("/api/teacher")
                        .with(user(userDetailsService.loadUserByUsername(savedUser.getEmail()))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    public void findAll_teachersEmpty_returnsArrayEmpty() throws Exception {
        User savedUser = setupUser();

        mockMvc.perform(get("/api/teacher")
                        .with(user(userDetailsService.loadUserByUsername(savedUser.getEmail()))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
