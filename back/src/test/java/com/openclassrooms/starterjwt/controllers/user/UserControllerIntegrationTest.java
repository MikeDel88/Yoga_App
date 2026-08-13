package com.openclassrooms.starterjwt.controllers.user;

import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserDetailsService userDetailsService;

    private User savedUser;

    private UserDetails savedUserDetails;

    @BeforeEach
    public void setupUser() {
        savedUser = createUser("testeur@test.com");
        savedUserDetails = userDetailsService.loadUserByUsername(savedUser.getEmail());
    }

    @Test
    public void findById_success() throws Exception {
        mockMvc.perform(get(getUriUser(savedUser.getId()))
                        .with(user(savedUserDetails)))
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
        mockMvc.perform(get(getUriUser(500L))
                        .with(user(savedUserDetails)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void findById_unauthenticated_returnsUnauthorized() throws Exception {
        mockMvc.perform(get(getUriUser(savedUser.getId())))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void delete_success() throws Exception {
        mockMvc.perform(delete(getUriUser(savedUser.getId()))
                        .with(user(savedUserDetails)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void delete_differentAuthenticatedUser_returnsUnauthorized() throws Exception {
        User otherUser = createUser("test@test.com");

        mockMvc.perform(delete(getUriUser(savedUser.getId()))
                        .with(user(userDetailsService.loadUserByUsername(otherUser.getEmail()))))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    private String getUriUser(Long userId) {
        return "/api/user/" + userId;
    }

    private User createUser(String email) {
        return userRepository.save(new User(email, "test", "test",
                passwordEncoder.encode("test!31"), false));
    }

}
