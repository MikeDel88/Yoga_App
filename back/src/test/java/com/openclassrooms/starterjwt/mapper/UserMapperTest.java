package com.openclassrooms.starterjwt.mapper;

import com.openclassrooms.starterjwt.dto.UserDto;
import com.openclassrooms.starterjwt.models.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private final UserMapper userMapper = new UserMapperImpl();

    @Test
    public void toDto_mapsAllFields() {
        LocalDateTime now = LocalDateTime.now();
        User user = new User("test@test.com", "test", "test", "encoded-password", true);
        user.setId(1L);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        UserDto userDto = userMapper.toDto(user);

        assertThat(userDto.getId()).isEqualTo(1L);
        assertThat(userDto.getEmail()).isEqualTo("test@test.com");
        assertThat(userDto.getFirstName()).isEqualTo("test");
        assertThat(userDto.getLastName()).isEqualTo("test");
        assertThat(userDto.getPassword()).isEqualTo("encoded-password");
        assertThat(userDto.isAdmin()).isTrue();
        assertThat(userDto.getCreatedAt()).isEqualTo(now);
        assertThat(userDto.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    public void toDto_null_returnsNull() {
        assertThat(userMapper.toDto((User) null)).isNull();
    }

    @Test
    public void toEntity_mapsAllFields() {
        LocalDateTime now = LocalDateTime.now();
        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setEmail("test@test.com");
        userDto.setFirstName("test");
        userDto.setLastName("test");
        userDto.setPassword("encoded-password");
        userDto.setAdmin(true);
        userDto.setCreatedAt(now);
        userDto.setUpdatedAt(now);

        User user = userMapper.toEntity(userDto);

        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getEmail()).isEqualTo("test@test.com");
        assertThat(user.getFirstName()).isEqualTo("test");
        assertThat(user.getLastName()).isEqualTo("test");
        assertThat(user.getPassword()).isEqualTo("encoded-password");
        assertThat(user.isAdmin()).isTrue();
        assertThat(user.getCreatedAt()).isEqualTo(now);
        assertThat(user.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    public void toEntity_null_returnsNull() {
        assertThat(userMapper.toEntity((UserDto) null)).isNull();
    }

    @Test
    public void toDto_list_mapsEachEntity() {
        User user = new User("test@test.com", "test", "test", "encoded-password", true);
        user.setId(1L);

        List<UserDto> userDtos = userMapper.toDto(List.of(user));

        assertThat(userDtos).hasSize(1);
        assertThat(userDtos.get(0).getId()).isEqualTo(1L);
        assertThat(userDtos.get(0).getEmail()).isEqualTo("test@test.com");
    }

    @Test
    public void toDto_nullList_returnsNull() {
        assertThat(userMapper.toDto((List<User>) null)).isNull();
    }

    @Test
    public void toEntity_list_mapsEachDto() {
        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setEmail("test@test.com");
        userDto.setFirstName("test");
        userDto.setLastName("test");
        userDto.setPassword("encoded-password");

        List<User> users = userMapper.toEntity(List.of(userDto));

        assertThat(users).hasSize(1);
        assertThat(users.get(0).getId()).isEqualTo(1L);
        assertThat(users.get(0).getEmail()).isEqualTo("test@test.com");
    }

    @Test
    public void toEntity_nullList_returnsNull() {
        assertThat(userMapper.toEntity((List<UserDto>) null)).isNull();
    }
}
