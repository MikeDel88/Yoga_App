package com.openclassrooms.starterjwt.services;

import com.openclassrooms.starterjwt.exception.NotFoundException;
import com.openclassrooms.starterjwt.models.Teacher;
import com.openclassrooms.starterjwt.repository.TeacherRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TeacherServiceTest {

    @Mock
    private TeacherRepository teacherRepository;

    @InjectMocks
    private TeacherService teacherService;

    /**
     * Garantit l'isolation des tests : chaque test démarre avec un SecurityContextHolder vide,
     * indépendamment de ce que les tests précédents y ont mis.
     */
    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void findAll_success() {
        List<Teacher> teachers = List.of(
                new Teacher(1L, "test", "test", LocalDateTime.now(), LocalDateTime.now()),
                new Teacher(2L, "test", "test", LocalDateTime.now(), LocalDateTime.now())
        );
        when(teacherRepository.findAll()).thenReturn(teachers);

        List<Teacher> result = teacherService.findAll();

        verify(teacherRepository, times(1)).findAll();
        assertThat(result).isEqualTo(teachers);
    }

    @Test
    public void findById_success() {
        Teacher teacher = new Teacher(1L, "test", "test", LocalDateTime.now(), LocalDateTime.now());

        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));

        Teacher result = teacherService.findById(1L);

        assertThat(result).isEqualTo(teacher);
    }

    @Test
    public void findById_returnsNotFound() {
        when(teacherRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teacherService.findById(1L))
                .isInstanceOf(NotFoundException.class);
    }
}
