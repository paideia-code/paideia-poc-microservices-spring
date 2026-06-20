package io.paideia.user.service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.paideia.user.service.controller.dto.StudentRegistrationRequestDTO;
import io.paideia.user.service.enums.UserRole;
import io.paideia.user.service.exception.custom.UserEmailConflictException;
import io.paideia.user.service.exception.custom.UserNotFoundException;
import io.paideia.user.service.model.entity.UserEntity;
import io.paideia.user.service.model.repository.UserRepository;
import io.paideia.user.service.service.mapper.UserMapper;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, new UserMapper());
    }

    @Test
    void registerStudentPersistsStudentRole() {
        var request = new StudentRegistrationRequestDTO("student@paideia.io", "Course Student");

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity entity = invocation.getArgument(0);
            entity.setId(UUID.fromString("33333333-0000-0000-0000-000000000002"));
            return entity;
        });

        var response = userService.registerStudent(request);

        assertThat(response.email()).isEqualTo(request.email());
        assertThat(response.name()).isEqualTo(request.name());
        assertThat(response.role()).isEqualTo(UserRole.STUDENT);
    }

    @Test
    void registerStudentRejectsDuplicateEmail() {
        var request = new StudentRegistrationRequestDTO("student@paideia.io", "Course Student");

        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> userService.registerStudent(request))
                .isInstanceOf(UserEmailConflictException.class);
    }

    @Test
    void findByIdReturnsStudentWhenItExists() {
        UUID userId = UUID.fromString("33333333-0000-0000-0000-000000000001");
        var existing = UserEntity.builder()
                .id(userId)
                .email("student@paideia.io")
                .name("Student")
                .role(UserRole.STUDENT)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existing));

        var response = userService.findById(userId);

        assertThat(response.email()).isEqualTo("student@paideia.io");
        assertThat(response.name()).isEqualTo("Student");
        assertThat(response.role()).isEqualTo(UserRole.STUDENT);
    }

    @Test
    void findByIdRejectsUnknownStudent() {
        UUID userId = UUID.fromString("33333333-0000-0000-0000-000000000001");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(userId))
                .isInstanceOf(UserNotFoundException.class);
    }
}
