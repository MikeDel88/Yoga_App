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
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SessionService {
    private final SessionRepository sessionRepository;

    private final UserRepository userRepository;

    private final SessionMapper sessionMapper;

    private final TeacherService teacherService;

    private final UserService userService;

    public SessionService(SessionRepository sessionRepository, UserRepository userRepository,
                           SessionMapper sessionMapper, TeacherService teacherService, UserService userService) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
        this.sessionMapper = sessionMapper;
        this.teacherService = teacherService;
        this.userService = userService;
    }

    public Session create(SessionDto sessionDto) {
        return this.sessionRepository.save(toEntity(sessionDto));
    }

    public void delete(Long id) {
        Session session = this.sessionRepository.findById(id).orElse(null);
        if (session == null) {
            throw new NotFoundException();
        }
        this.sessionRepository.deleteById(id);
    }

    public List<Session> findAll() {
        return this.sessionRepository.findAll();
    }

    public Session getById(Long id) {
        Session session = this.sessionRepository.findById(id).orElse(null);
        if (session == null) {
            throw new NotFoundException();
        }
        return session;
    }

    public Session update(Long id, SessionDto sessionDto) {
        Session session = toEntity(sessionDto);
        session.setId(id);
        return this.sessionRepository.save(session);
    }

    private Session toEntity(SessionDto sessionDto) {
        Teacher teacher = this.teacherService.findById(sessionDto.getTeacherId());
        List<User> users = resolveUsers(sessionDto.getUsers());
        return this.sessionMapper.toEntity(sessionDto, teacher, users);
    }

    private List<User> resolveUsers(List<Long> userIds) {
        if (userIds == null) {
            return new ArrayList<>();
        }
        return userIds.stream().map(this.userService::findById).collect(Collectors.toList());
    }

    public void participate(Long id, Long userId) {
        Session session = this.sessionRepository.findById(id).orElse(null);
        User user = this.userRepository.findById(userId).orElse(null);
        if (session == null || user == null) {
            throw new NotFoundException();
        }

        boolean alreadyParticipate = session.getUsers().stream().anyMatch(u -> u.getId().equals(userId));
        if (alreadyParticipate) {
            throw new BadRequestException();
        }

        session.getUsers().add(user);

        this.sessionRepository.save(session);
    }

    public void noLongerParticipate(Long id, Long userId) {
        Session session = this.sessionRepository.findById(id).orElse(null);
        if (session == null) {
            throw new NotFoundException();
        }

        boolean alreadyParticipate = session.getUsers().stream().anyMatch(user -> user.getId().equals(userId));
        if (!alreadyParticipate) {
            throw new BadRequestException();
        }

        session.setUsers(session.getUsers().stream().filter(user -> !user.getId().equals(userId)).collect(Collectors.toList()));

        this.sessionRepository.save(session);
    }
}
