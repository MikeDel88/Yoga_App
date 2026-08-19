package com.openclassrooms.starterjwt.mapper;

import com.openclassrooms.starterjwt.dto.SessionDto;
import com.openclassrooms.starterjwt.models.Session;
import com.openclassrooms.starterjwt.models.Teacher;
import com.openclassrooms.starterjwt.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@Mapper(componentModel = "spring")
public abstract class SessionMapper implements EntityMapper<SessionDto, Session> {

    @Mappings({
            @Mapping(target = "teacher", ignore = true),
            @Mapping(target = "users", ignore = true),
    })
    public abstract Session toEntity(SessionDto sessionDto);

    @Mappings({
            @Mapping(target = "teacherId", source = "teacher.id"),
            @Mapping(target = "users", expression = "java(usersToUserIds(session.getUsers()))"),
    })
    public abstract SessionDto toDto(Session session);

    public Session toEntity(SessionDto sessionDto, Teacher teacher, List<User> users) {
        Session session = toEntity(sessionDto);
        session.setTeacher(teacher);
        session.setUsers(Objects.requireNonNullElseGet(users, ArrayList::new));
        return session;
    }

    protected List<Long> usersToUserIds(List<User> users) {
        if (users == null) {
            return new ArrayList<>();
        }
        return users.stream().map(User::getId).collect(Collectors.toList());
    }
}
