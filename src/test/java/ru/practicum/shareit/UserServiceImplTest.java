package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.DuplicateEmailException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository);
    }

    @Test
    void shouldCreateUser() {
        UserDto userDto = UserDto.builder()
                .name("Ivan")
                .email("ivan@mail.ru")
                .build();

        when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        UserDto result = userService.create(userDto);

        assertNotNull(result.getId());
        assertEquals("Ivan", result.getName());
        assertEquals("ivan@mail.ru", result.getEmail());
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        UserDto userDto = UserDto.builder()
                .name("Petr")
                .email("ivan@mail.ru")
                .build();

        when(userRepository.existsByEmailIgnoreCase("ivan@mail.ru")).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> userService.create(userDto));
    }

    @Test
    void shouldGetUserById() {
        User user = User.builder().id(1L).name("Ivan").email("ivan@mail.ru").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDto foundUser = userService.getById(1L);

        assertNotNull(foundUser);
        assertEquals(1L, foundUser.getId());
        assertEquals("Ivan", foundUser.getName());
        assertEquals("ivan@mail.ru", foundUser.getEmail());
    }

    @Test
    void shouldReturnAllUsers() {
        User ivan = User.builder().id(1L).name("Ivan").email("ivan@mail.ru").build();
        User petr = User.builder().id(2L).name("Petr").email("petr@mail.ru").build();

        when(userRepository.findAll()).thenReturn(List.of(ivan, petr));

        List<UserDto> users = userService.getAll();

        assertEquals(2, users.size());
        assertEquals("Ivan", users.get(0).getName());
        assertEquals("Petr", users.get(1).getName());
    }

    @Test
    void shouldUpdateUserName() {
        User user = User.builder().id(1L).name("Ivan").email("ivan@mail.ru").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDto updateDto = UserDto.builder().name("Ivan Updated").build();

        UserDto updatedUser = userService.update(1L, updateDto);

        assertEquals(1L, updatedUser.getId());
        assertEquals("Ivan Updated", updatedUser.getName());
        assertEquals("ivan@mail.ru", updatedUser.getEmail());
    }

    @Test
    void shouldDeleteUser() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(User.builder().id(1L).name("Ivan").email("ivan@mail.ru").build()))
                .thenReturn(Optional.empty());

        userService.delete(1L);

        assertThrows(NotFoundException.class, () -> userService.getById(1L));
    }
}
