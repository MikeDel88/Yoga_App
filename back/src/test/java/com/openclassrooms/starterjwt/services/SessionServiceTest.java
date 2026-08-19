package com.openclassrooms.starterjwt.services;

import com.openclassrooms.starterjwt.dto.SessionDto;
import com.openclassrooms.starterjwt.exception.BadRequestException;
import com.openclassrooms.starterjwt.exception.NotFoundException;
import com.openclassrooms.starterjwt.mapper.SessionMapper;
import com.openclassrooms.starterjwt.models.Session;
import com.openclassrooms.starterjwt.models.Teacher;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.repository.SessionRepository;
import com.openclassrooms.starterjwt.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SessionServiceTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SessionMapper sessionMapper;

    @Mock
    private TeacherService teacherService;

    @Mock
    private UserService userService;

    @InjectMocks
    private SessionService sessionService;

    /**
     * Garantit l'isolation des tests : chaque test démarre avec un SecurityContextHolder vide,
     * indépendamment de ce que les tests précédents y ont mis.
     */
    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void findAll_found_returnsSessions() {
        Teacher teacher = createTeacher();
        List<Session> sessions = List.of(
                createSession(1L, teacher, new ArrayList<>()),
                createSession(2L, teacher, new ArrayList<>())
        );
        when(sessionRepository.findAll()).thenReturn(sessions);

        List<Session> result = sessionService.findAll();

        verify(sessionRepository, times(1)).findAll();
        assertThat(result).isEqualTo(sessions);
    }

    @Test
    public void findById_found_returnsSession() {
        Teacher teacher = createTeacher();
        Session session = createSession(1L, teacher, new ArrayList<>());

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        Session result = sessionService.getById(1L);

        assertThat(result).isEqualTo(session);
    }

    @Test
    public void findById_notFound_throwsNotFoundException() {
        when(sessionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sessionService.getById(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    public void create_success_returnsSession() {
        Teacher teacher = createTeacher();
        Session session = createSession(1L, teacher, new ArrayList<>());
        SessionDto sessionDto = createSessionDto(teacher.getId(), new ArrayList<>());

        when(teacherService.findById(teacher.getId())).thenReturn(teacher);
        when(sessionMapper.toEntity(sessionDto, teacher, new ArrayList<>())).thenReturn(session);
        when(sessionRepository.save(session)).thenReturn(session.setId(1L));

        Session result = sessionService.create(sessionDto);

        assertThat(result).isEqualTo(session);
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    public void delete_success() {
        Teacher teacher = createTeacher();
        Session session = createSession(1L, teacher, new ArrayList<>());

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        sessionService.delete(1L);

        verify(sessionRepository).deleteById(1L);
    }

    @Test
    public void delete_sessionNotFound_throwsNotFoundException() {
        when(sessionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sessionService.delete(1L))
                .isInstanceOf(NotFoundException.class);

        verify(userRepository, never()).deleteById(any());
    }

    @Test
    public void update_success_returnsSession() {
        Teacher teacher = createTeacher();
        Session session = createSession(1L, teacher, new ArrayList<>());
        SessionDto sessionDto = createSessionDto(teacher.getId(), new ArrayList<>());

        when(teacherService.findById(teacher.getId())).thenReturn(teacher);
        when(sessionMapper.toEntity(sessionDto, teacher, new ArrayList<>())).thenReturn(session);
        when(sessionRepository.save(session)).thenReturn(session.setName("Yoga Mania"));

        Session result = sessionService.update(1L, sessionDto);

        assertThat(result).isEqualTo(session);
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Yoga Mania");
    }

    @Test
    public void participate_success() {
        Teacher teacher = createTeacher();
        Session session = createSession(1L, teacher, new ArrayList<>());
        User user = createUser("test@test.com", 1L);

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        sessionService.participate(session.getId(), user.getId());

        ArgumentCaptor<Session> sessionCaptor = ArgumentCaptor.forClass(Session.class);
        verify(sessionRepository).save(sessionCaptor.capture());
        Session savedSession = sessionCaptor.getValue();
        assertThat(savedSession.getUsers().size()).isEqualTo(1);

    }

    @Test
    public void participate_sessionNotFound_throwsNotFoundException() {
        when(sessionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sessionService.participate(1L, 1L))
                .isInstanceOf(NotFoundException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    public void participate_userNotFound_throwsNotFoundException() {
        Teacher teacher = createTeacher();
        Session session = createSession(1L, teacher, new ArrayList<>());

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sessionService.participate(1L, 1L))
                .isInstanceOf(NotFoundException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    public void participate_userAlReadyParticipate_throwsBadRequestException() {
        User user = createUser("test@test.com", 1L);
        user.setId(1L);
        Teacher teacher = createTeacher();
        Session session = createSession(1L, teacher, List.of(user));

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> sessionService.participate(1L, 1L))
                .isInstanceOf(BadRequestException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    public void noLongerparticipate_success() {
        User user = createUser("test@test.com", 1L);
        Teacher teacher = createTeacher();
        Session session = createSession(1L, teacher, List.of(user));

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        sessionService.noLongerParticipate(session.getId(), user.getId());

        ArgumentCaptor<Session> sessionCaptor = ArgumentCaptor.forClass(Session.class);
        verify(sessionRepository).save(sessionCaptor.capture());
        Session savedSession = sessionCaptor.getValue();
        assertThat(savedSession.getUsers().size()).isEqualTo(0);

    }

    @Test
    public void noLongerParticipate_multipleUsers_removesOnlyTargetUser() {
        User userToRemove = createUser("remove@test.com", 1L);
        User userToKeep = createUser("keep@test.com", 2L);

        Teacher teacher = createTeacher();
        Session session = createSession(1L, teacher, new ArrayList<>(List.of(userToRemove, userToKeep)));

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        sessionService.noLongerParticipate(session.getId(), userToRemove.getId());

        ArgumentCaptor<Session> sessionCaptor = ArgumentCaptor.forClass(Session.class);
        verify(sessionRepository).save(sessionCaptor.capture());
        Session savedSession = sessionCaptor.getValue();
        assertThat(savedSession.getUsers())
                .containsExactly(userToKeep)
                .doesNotContain(userToRemove);
    }

    @Test
    public void noLongerparticipate_sessionNotFound_throwsNotFoundException() {

        when(sessionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sessionService.noLongerParticipate(1L, 1L))
                .isInstanceOf(NotFoundException.class);

        verify(userRepository, never()).save(any());

    }

    @Test
    public void noLongerparticipate_userNotParticipate_throwsBadRequestException() {

        Teacher teacher = createTeacher();
        Session session = createSession(1L, teacher, new ArrayList<>());

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> sessionService.noLongerParticipate(1L, 1L))
                .isInstanceOf(BadRequestException.class);

        verify(userRepository, never()).save(any());
    }

    private User createUser(String email, Long userId) {
        User user = new User(email, "test", "test", "password-encoded", false);
        user.setId(userId);
        return user;
    }

    private Teacher createTeacher() {
        return new Teacher(1L, "test", "test", LocalDateTime.now(), LocalDateTime.now());
    }

    private Session createSession(Long sessionId, Teacher teacher, List<User> users) {
        return new Session(sessionId, "Yoga Session", new Date(), "Yoga session description", teacher, users, LocalDateTime.now(), LocalDateTime.now());
    }

    private SessionDto createSessionDto(Long teacherId, List<Long> userIds) {
        SessionDto sessionDto = new SessionDto();
        sessionDto.setName("Yoga Session");
        sessionDto.setDate(new Date());
        sessionDto.setDescription("Yoga session description");
        sessionDto.setTeacherId(teacherId);
        sessionDto.setUsers(userIds);
        return sessionDto;
    }
}
