package com.openclassrooms.starterjwt.mapper;

import com.openclassrooms.starterjwt.dto.SessionDto;
import com.openclassrooms.starterjwt.models.Session;
import com.openclassrooms.starterjwt.models.Teacher;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.services.TeacherService;
import com.openclassrooms.starterjwt.services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionMapperTest {

    @Mock
    private TeacherService teacherService;

    @Mock
    private UserService userService;

    @InjectMocks
    private SessionMapperImpl sessionMapper;

    @Test
    public void toDto_mapsTeacherIdAndUserIds() {
        Teacher teacher = new Teacher();
        teacher.setId(2L);

        User user = new User("test@test.com", "test", "test", "encoded-password", false);
        user.setId(3L);

        LocalDateTime now = LocalDateTime.now();
        Date date = new Date();
        Session session = Session.builder()
                .id(1L)
                .name("Yoga Session")
                .date(date)
                .description("Yoga session description")
                .teacher(teacher)
                .users(List.of(user))
                .createdAt(now)
                .updatedAt(now)
                .build();

        SessionDto sessionDto = sessionMapper.toDto(session);

        assertThat(sessionDto.getId()).isEqualTo(1L);
        assertThat(sessionDto.getName()).isEqualTo("Yoga Session");
        assertThat(sessionDto.getDate()).isEqualTo(date);
        assertThat(sessionDto.getDescription()).isEqualTo("Yoga session description");
        assertThat(sessionDto.getTeacher_id()).isEqualTo(2L);
        assertThat(sessionDto.getUsers()).containsExactly(3L);
        assertThat(sessionDto.getCreatedAt()).isEqualTo(now);
        assertThat(sessionDto.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    public void toDto_noTeacherAndNoUsers_returnsNullTeacherIdAndEmptyUsers() {
        Session session = Session.builder()
                .id(1L)
                .name("Yoga Session")
                .date(new Date())
                .description("Yoga session description")
                .teacher(null)
                .users(null)
                .build();

        SessionDto sessionDto = sessionMapper.toDto(session);

        assertThat(sessionDto.getTeacher_id()).isNull();
        assertThat(sessionDto.getUsers()).isEmpty();
    }

    @Test
    public void toEntity_mapsTeacherIdToTeacher() {
        Teacher teacher = new Teacher();
        teacher.setId(2L);
        when(teacherService.findById(2L)).thenReturn(teacher);

        SessionDto sessionDto = new SessionDto();
        sessionDto.setName("Yoga Session");
        sessionDto.setDate(new Date());
        sessionDto.setDescription("Yoga session description");
        sessionDto.setTeacher_id(2L);
        sessionDto.setUsers(List.of());

        Session session = sessionMapper.toEntity(sessionDto);

        assertThat(session.getTeacher()).isEqualTo(teacher);
    }

    @Test
    public void toEntity_nullTeacherId_returnsNullTeacher() {
        SessionDto sessionDto = new SessionDto();
        sessionDto.setName("Yoga Session");
        sessionDto.setDate(new Date());
        sessionDto.setDescription("Yoga session description");
        sessionDto.setTeacher_id(null);
        sessionDto.setUsers(List.of());

        Session session = sessionMapper.toEntity(sessionDto);

        assertThat(session.getTeacher()).isNull();
    }

    @Test
    public void toEntity_mapsUserIdsToUsers() {
        User user = new User("test@test.com", "test", "test", "encoded-password", false);
        user.setId(3L);
        when(userService.findById(3L)).thenReturn(user);

        SessionDto sessionDto = new SessionDto();
        sessionDto.setName("Yoga Session");
        sessionDto.setDate(new Date());
        sessionDto.setDescription("Yoga session description");
        sessionDto.setUsers(List.of(3L));

        Session session = sessionMapper.toEntity(sessionDto);

        assertThat(session.getUsers()).containsExactly(user);
    }

    @Test
    public void toEntity_nullUsersList_returnsEmptyUsersList() {
        SessionDto sessionDto = new SessionDto();
        sessionDto.setName("Yoga Session");
        sessionDto.setDate(new Date());
        sessionDto.setDescription("Yoga session description");
        sessionDto.setUsers(null);

        Session session = sessionMapper.toEntity(sessionDto);

        assertThat(session.getUsers()).isEmpty();
    }

    @Test
    public void toDto_list_mapsEachEntity() {
        Session session = Session.builder()
                .id(1L)
                .name("Yoga Session")
                .date(new Date())
                .description("Yoga session description")
                .build();

        List<SessionDto> sessionDtos = sessionMapper.toDto(List.of(session));

        assertThat(sessionDtos).hasSize(1);
        assertThat(sessionDtos.get(0).getId()).isEqualTo(1L);
        assertThat(sessionDtos.get(0).getName()).isEqualTo("Yoga Session");
    }

    @Test
    public void toDto_nullList_returnsNull() {
        assertThat(sessionMapper.toDto((List<Session>) null)).isNull();
    }

    @Test
    public void toEntity_list_mapsEachDto() {
        SessionDto sessionDto = new SessionDto();
        sessionDto.setId(1L);
        sessionDto.setName("Yoga Session");
        sessionDto.setDate(new Date());
        sessionDto.setDescription("Yoga session description");

        List<Session> sessions = sessionMapper.toEntity(List.of(sessionDto));

        assertThat(sessions).hasSize(1);
        assertThat(sessions.get(0).getId()).isEqualTo(1L);
        assertThat(sessions.get(0).getName()).isEqualTo("Yoga Session");
    }

    @Test
    public void toEntity_nullList_returnsNull() {
        assertThat(sessionMapper.toEntity((List<SessionDto>) null)).isNull();
    }
}
