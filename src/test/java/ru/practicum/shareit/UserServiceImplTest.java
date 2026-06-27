package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.exception.DuplicateEmailException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dao.InMemoryUserStorage;
import ru.practicum.shareit.user.dao.UserStorage;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceImplTest {

    private UserStorage userStorage;
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userStorage = new InMemoryUserStorage();
        userService = new UserServiceImpl(userStorage);
    }

    @Test
    void shouldCreateUser() {
        UserDto userDto = UserDto.builder()
                .name("Ivan")
                .email("ivan@mail.ru")
                .build();

        UserDto result = userService.create(userDto);

        assertNotNull(result.getId());
        assertEquals("Ivan", result.getName());
        assertEquals("ivan@mail.ru", result.getEmail());
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        UserDto firstUser = UserDto.builder()
                .name("Ivan")
                .email("ivan@mail.ru")
                .build();

        UserDto secondUser = UserDto.builder()
                .name("Petr")
                .email("ivan@mail.ru")
                .build();

        userService.create(firstUser);

        DuplicateEmailException exception = assertThrows(
                DuplicateEmailException.class,
                () -> userService.create(secondUser)
        );

        assertEquals(
                String.format("Пользователь с email %s уже существует", secondUser.getEmail()),
                exception.getMessage()
        );
    }

    @Test
    void shouldGetUserById() {
        UserDto userDto = UserDto.builder()
                .name("Ivan")
                .email("ivan@mail.ru")
                .build();

        UserDto createdUser = userService.create(userDto);

        UserDto foundUser = userService.getById(createdUser.getId());

        assertNotNull(foundUser);
        assertEquals(createdUser.getId(), foundUser.getId());
        assertEquals("Ivan", foundUser.getName());
        assertEquals("ivan@mail.ru", foundUser.getEmail());
    }

    @Test
    void shouldReturnAllUsers() {
        userService.create(UserDto.builder()
                .name("Ivan")
                .email("ivan@mail.ru")
                .build());

        userService.create(UserDto.builder()
                .name("Petr")
                .email("petr@mail.ru")
                .build());

        List<UserDto> users = userService.getAll();

        assertEquals(2, users.size());

        assertEquals("Ivan", users.get(0).getName());
        assertEquals("ivan@mail.ru", users.get(0).getEmail());

        assertEquals("Petr", users.get(1).getName());
        assertEquals("petr@mail.ru", users.get(1).getEmail());
    }

    @Test
    void shouldUpdateUserName() {
        UserDto createdUser = userService.create(
                UserDto.builder()
                        .name("Ivan")
                        .email("ivan@mail.ru")
                        .build()
        );

        UserDto updateDto = UserDto.builder()
                .name("Ivan Updated")
                .build();

        UserDto updatedUser = userService.update(
                createdUser.getId(),
                updateDto
        );

        assertEquals(createdUser.getId(), updatedUser.getId());
        assertEquals("Ivan Updated", updatedUser.getName());

        assertEquals("ivan@mail.ru", updatedUser.getEmail());
    }

    @Test
    void shouldDeleteUser() {
        UserDto createdUser = userService.create(
                UserDto.builder()
                        .name("Ivan")
                        .email("ivan@mail.ru")
                        .build()
        );

        userService.delete(createdUser.getId());

        assertThrows(
                NotFoundException.class,
                () -> userService.getById(createdUser.getId())
        );
    }
}
