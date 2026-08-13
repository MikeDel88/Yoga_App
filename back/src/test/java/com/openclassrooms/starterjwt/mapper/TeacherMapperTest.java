package com.openclassrooms.starterjwt.mapper;

import com.openclassrooms.starterjwt.dto.TeacherDto;
import com.openclassrooms.starterjwt.models.Teacher;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TeacherMapperTest {

    private final TeacherMapper teacherMapper = new TeacherMapperImpl();

    @Test
    public void toDto_mapsAllFields() {
        LocalDateTime now = LocalDateTime.now();
        Teacher teacher = new Teacher();
        teacher.setId(1L);
        teacher.setFirstName("john");
        teacher.setLastName("doe");
        teacher.setCreatedAt(now);
        teacher.setUpdatedAt(now);

        TeacherDto teacherDto = teacherMapper.toDto(teacher);

        assertThat(teacherDto.getId()).isEqualTo(1L);
        assertThat(teacherDto.getFirstName()).isEqualTo("john");
        assertThat(teacherDto.getLastName()).isEqualTo("doe");
        assertThat(teacherDto.getCreatedAt()).isEqualTo(now);
        assertThat(teacherDto.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    public void toDto_null_returnsNull() {
        assertThat(teacherMapper.toDto((Teacher) null)).isNull();
    }

    @Test
    public void toEntity_mapsAllFields() {
        LocalDateTime now = LocalDateTime.now();
        TeacherDto teacherDto = new TeacherDto();
        teacherDto.setId(1L);
        teacherDto.setFirstName("john");
        teacherDto.setLastName("doe");
        teacherDto.setCreatedAt(now);
        teacherDto.setUpdatedAt(now);

        Teacher teacher = teacherMapper.toEntity(teacherDto);

        assertThat(teacher.getId()).isEqualTo(1L);
        assertThat(teacher.getFirstName()).isEqualTo("john");
        assertThat(teacher.getLastName()).isEqualTo("doe");
        assertThat(teacher.getCreatedAt()).isEqualTo(now);
        assertThat(teacher.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    public void toEntity_null_returnsNull() {
        assertThat(teacherMapper.toEntity((TeacherDto) null)).isNull();
    }

    @Test
    public void toDto_list_mapsEachEntity() {
        Teacher teacher = new Teacher();
        teacher.setId(1L);
        teacher.setFirstName("john");
        teacher.setLastName("doe");

        List<TeacherDto> teacherDtos = teacherMapper.toDto(List.of(teacher));

        assertThat(teacherDtos).hasSize(1);
        assertThat(teacherDtos.get(0).getId()).isEqualTo(1L);
        assertThat(teacherDtos.get(0).getFirstName()).isEqualTo("john");
    }

    @Test
    public void toDto_nullList_returnsNull() {
        assertThat(teacherMapper.toDto((List<Teacher>) null)).isNull();
    }

    @Test
    public void toEntity_list_mapsEachDto() {
        TeacherDto teacherDto = new TeacherDto();
        teacherDto.setId(1L);
        teacherDto.setFirstName("john");
        teacherDto.setLastName("doe");

        List<Teacher> teachers = teacherMapper.toEntity(List.of(teacherDto));

        assertThat(teachers).hasSize(1);
        assertThat(teachers.get(0).getId()).isEqualTo(1L);
        assertThat(teachers.get(0).getFirstName()).isEqualTo("john");
    }

    @Test
    public void toEntity_nullList_returnsNull() {
        assertThat(teacherMapper.toEntity((List<TeacherDto>) null)).isNull();
    }
}
