package com.openclassrooms.starterjwt.controllers.user;

import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser("testeur@test.com")
@Transactional
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User savedUser;

    @BeforeEach
    public void setupUser() {
        savedUser = createUser();
    }

    @Test
    public void findById_success() throws Exception {
        mockMvc.perform(get(getUriUser(savedUser.getId())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedUser.getId()))
                .andExpect(jsonPath("$.email").value(savedUser.getEmail()))
                .andExpect(jsonPath("$.firstName").value(savedUser.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(savedUser.getLastName()))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.admin").value(false));
    }

    @Test
    public void findById_userNotFound_returnsNotFound() throws Exception {
        mockMvc.perform(get(getUriUser(500L)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void delete_success() throws Exception {
        mockMvc.perform(delete(getUriUser(savedUser.getId())))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @WithMockUser("test@test.com")
    @Test
    public void delete_differentAuthenticatedUser_returnsUnauthorized() throws Exception {
        mockMvc.perform(delete(getUriUser(savedUser.getId())))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    private String getUriUser(Long userId) {
        return "/api/user/" + userId;
    }

    private User createUser() {
        return userRepository.save(new User("testeur@test.com", "test", "test",
                passwordEncoder.encode("test!31"), false));
    }

}
