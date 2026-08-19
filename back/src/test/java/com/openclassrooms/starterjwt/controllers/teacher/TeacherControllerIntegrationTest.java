package com.openclassrooms.starterjwt.controllers.teacher;

import com.openclassrooms.starterjwt.models.Teacher;
import com.openclassrooms.starterjwt.repository.TeacherRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser
@Transactional
public class TeacherControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TeacherRepository teacherRepository;

    @Test
    public void findById_success() throws Exception {
        Teacher savedTeacher = setupTeacher();

        mockMvc.perform(get(getUriTeacher(savedTeacher.getId())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedTeacher.getId()))
                .andExpect(jsonPath("$.firstName").value(savedTeacher.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(savedTeacher.getLastName()));
    }

    @Test
    public void findById_teacherNotFound_returnsNotFound() throws Exception {

        mockMvc.perform(get(getUriTeacher(1L)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void findAll_success() throws Exception {
        setupTeacher();
        setupTeacher();

        mockMvc.perform(get(getUriTeachers()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    public void findAll_teachersEmpty_returnsArrayEmpty() throws Exception {

        mockMvc.perform(get(getUriTeachers()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    private String getUriTeachers() {
        return "/api/teacher";
    }

    private String getUriTeacher(Long teacherId) {
        return "/api/teacher/" + teacherId;
    }

    private Teacher setupTeacher() {
        Teacher teacher = new Teacher();
        teacher.setFirstName("John");
        teacher.setLastName("Doe");
        return teacherRepository.save(teacher);
    }
}
