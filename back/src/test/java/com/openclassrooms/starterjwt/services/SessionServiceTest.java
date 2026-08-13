package com.openclassrooms.starterjwt.services;

import com.openclassrooms.starterjwt.exception.BadRequestException;
import com.openclassrooms.starterjwt.exception.NotFoundException;
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
        Teacher teacher = new Teacher(1L, "test", "test", LocalDateTime.now(), LocalDateTime.now());
        List<Session> sessions = List.of(
                new Session(1L, "Yoga Session", new Date(), "Yoga session description", teacher, List.of(), LocalDateTime.now(), LocalDateTime.now()),
                new Session(2L, "Yoga Session 2", new Date(), "Yoga session description 2", teacher, List.of(), LocalDateTime.now(), LocalDateTime.now())
        );
        when(sessionRepository.findAll()).thenReturn(sessions);

        List<Session> result = sessionService.findAll();

        verify(sessionRepository, times(1)).findAll();
        assertThat(result).isEqualTo(sessions);
    }

    @Test
    public void findById_found_returnsSession() {
        Teacher teacher = new Teacher(1L, "test", "test", LocalDateTime.now(), LocalDateTime.now());
        Session session = new Session(1L, "Yoga Session", new Date(), "Yoga session description", teacher, List.of(), LocalDateTime.now(), LocalDateTime.now());

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
        Teacher teacher = new Teacher(1L, "test", "test", LocalDateTime.now(), LocalDateTime.now());
        Session session = new Session(null, "Yoga Session", new Date(), "Yoga session description", teacher, List.of(), LocalDateTime.now(), LocalDateTime.now());

        when(sessionRepository.save(session)).thenReturn(session.setId(1L));
        Session result = sessionService.create(session);

        assertThat(result).isEqualTo(session);
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    public void delete_success() {
        Teacher teacher = new Teacher(1L, "test", "test", LocalDateTime.now(), LocalDateTime.now());
        Session session = new Session(1L, "Yoga Session", new Date(), "Yoga session description", teacher, List.of(), LocalDateTime.now(), LocalDateTime.now());

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
        Teacher teacher = new Teacher(1L, "test", "test", LocalDateTime.now(), LocalDateTime.now());
        Session session = new Session(null, "Yoga Session", new Date(), "Yoga session description", teacher, List.of(), LocalDateTime.now(), LocalDateTime.now());

        when(sessionRepository.save(session)).thenReturn(session.setName("Yoga Mania"));
        Session result = sessionService.update(1L, session);

        assertThat(result).isEqualTo(session);
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Yoga Mania");
    }

    @Test
    public void participate_success() {
        Teacher teacher = new Teacher(1L, "test", "test", LocalDateTime.now(), LocalDateTime.now());
        Session session = new Session(1L, "Yoga Session", new Date(), "Yoga session description", teacher, new ArrayList<>(), LocalDateTime.now(), LocalDateTime.now());
        User user = new User("test@test.com", "test", "test", "password-encoded",false);
        user.setId(1L);

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
        Teacher teacher = new Teacher(1L, "test", "test", LocalDateTime.now(), LocalDateTime.now());
        Session session = new Session(1L, "Yoga Session", new Date(), "Yoga session description", teacher, new ArrayList<>(), LocalDateTime.now(), LocalDateTime.now());

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sessionService.participate(1L, 1L))
                .isInstanceOf(NotFoundException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    public void participate_userAlReadyParticipate_throwsBadRequestException() {
        User user = new User("test@test.com", "test", "test", "password-encoded",false);
        user.setId(1L);
        Teacher teacher = new Teacher(1L, "test", "test", LocalDateTime.now(), LocalDateTime.now());
        Session session = new Session(1L, "Yoga Session", new Date(), "Yoga session description", teacher, List.of(user), LocalDateTime.now(), LocalDateTime.now());

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> sessionService.participate(1L, 1L))
                .isInstanceOf(BadRequestException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    public void noLongerparticipate_success() {
        User user = new User("test@test.com", "test", "test", "password-encoded",false);
        user.setId(1L);
        Teacher teacher = new Teacher(1L, "test", "test", LocalDateTime.now(), LocalDateTime.now());
        Session session = new Session(1L, "Yoga Session", new Date(), "Yoga session description", teacher, List.of(user), LocalDateTime.now(), LocalDateTime.now());

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        sessionService.noLongerParticipate(session.getId(), user.getId());

        ArgumentCaptor<Session> sessionCaptor = ArgumentCaptor.forClass(Session.class);
        verify(sessionRepository).save(sessionCaptor.capture());
        Session savedSession = sessionCaptor.getValue();
        assertThat(savedSession.getUsers().size()).isEqualTo(0);

    }

    @Test
    public void noLongerParticipate_multipleUsers_removesOnlyTargetUser() {
        User userToRemove = new User("remove@test.com", "test", "test", "password-encoded", false);
        userToRemove.setId(1L);
        User userToKeep = new User("keep@test.com", "test", "test", "password-encoded", false);
        userToKeep.setId(2L);

        Teacher teacher = new Teacher(1L, "test", "test", LocalDateTime.now(), LocalDateTime.now());
        Session session = new Session(1L, "Yoga Session", new Date(), "Yoga session description",
                teacher, new ArrayList<>(List.of(userToRemove, userToKeep)), LocalDateTime.now(), LocalDateTime.now());

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

        Teacher teacher = new Teacher(1L, "test", "test", LocalDateTime.now(), LocalDateTime.now());
        Session session = new Session(1L, "Yoga Session", new Date(), "Yoga session description", teacher, new ArrayList<>(), LocalDateTime.now(), LocalDateTime.now());

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> sessionService.noLongerParticipate(1L, 1L))
                .isInstanceOf(BadRequestException.class);

        verify(userRepository, never()).save(any());
    }
}
