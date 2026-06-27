package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dao.InMemoryItemStorage;
import ru.practicum.shareit.item.dao.ItemStorage;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.user.dao.InMemoryUserStorage;
import ru.practicum.shareit.user.dao.UserStorage;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.user.service.UserServiceImpl;

import static org.junit.jupiter.api.Assertions.*;

public class ItemServiceTestImpl {
    private ItemStorage itemStorage;
    private UserStorage userStorage;
    private ItemService itemService;
    private UserService userService;


    @BeforeEach
    void setUp() {
        itemStorage = new InMemoryItemStorage();
        userStorage = new InMemoryUserStorage();
        itemService = new ItemServiceImpl(itemStorage, userStorage);
        userService = new UserServiceImpl(userStorage);
    }

    @Test
    void shouldCreateItem() {
        UserDto owner = userService.create(
                UserDto.builder()
                        .name("Ivan")
                        .email("ivan@mail.ru")
                        .build()
        );

        ItemDto itemDto = ItemDto.builder()
                .name("Дрель")
                .description("Мощная дрель")
                .available(true)
                .build();

        ItemDto createdItem = itemService.create(
                itemDto,
                owner.getId()
        );

        assertNotNull(createdItem.getId());
        assertEquals("Дрель", createdItem.getName());
        assertEquals("Мощная дрель", createdItem.getDescription());
        assertTrue(createdItem.getAvailable());
    }

    @Test
    void shouldThrowExceptionWhenNameIsBlank() {
        UserDto owner = userService.create(
                UserDto.builder()
                        .name("Ivan")
                        .email("ivan@mail.ru")
                        .build()
        );

        ItemDto itemDto = ItemDto.builder()
                .name("")
                .description("Описание")
                .available(true)
                .build();

        assertThrows(ValidationException.class,
                () -> itemService.create(itemDto, owner.getId())
        );
    }

    @Test
    void shouldThrowExceptionWhenDescriptionIsBlank() {
        UserDto owner = userService.create(
                UserDto.builder()
                        .name("Ivan")
                        .email("ivan@mail.ru")
                        .build()
        );

        ItemDto itemDto = ItemDto.builder()
                .name("Дрель")
                .description("")
                .available(true)
                .build();

        assertThrows(ValidationException.class,
                () -> itemService.create(itemDto, owner.getId())
        );
    }

    @Test
    void shouldThrowExceptionWhenAvailableIsNull() {
        UserDto owner = userService.create(
                UserDto.builder()
                        .name("Ivan")
                        .email("ivan@mail.ru")
                        .build()
        );

        ItemDto itemDto = ItemDto.builder()
                .name("Дрель")
                .description("Описание")
                .available(null)
                .build();

        assertThrows(ValidationException.class,
                () -> itemService.create(itemDto, owner.getId())
        );
    }

    @Test
    void shouldGetItemById() {
        UserDto owner = userService.create(
                UserDto.builder()
                        .name("Ivan")
                        .email("ivan@mail.ru")
                        .build()
        );

        ItemDto createdItem = itemService.create(
                ItemDto.builder()
                        .name("Дрель")
                        .description("Мощная")
                        .available(true)
                        .build(),
                owner.getId()
        );

        ItemDto foundItem = itemService.getById(
                createdItem.getId()
        );

        assertEquals(createdItem.getId(), foundItem.getId());
        assertEquals("Дрель", foundItem.getName());
    }

    @Test
    void shouldUpdateItem() {
        UserDto owner = userService.create(
                UserDto.builder()
                        .name("Ivan")
                        .email("ivan@mail.ru")
                        .build()
        );

        ItemDto createdItem = itemService.create(
                ItemDto.builder()
                        .name("Дрель")
                        .description("Старая")
                        .available(true)
                        .build(),
                owner.getId()
        );

        ItemDto updateDto = ItemDto.builder()
                .name("Новая дрель")
                .build();

        ItemDto updatedItem = itemService.update(
                createdItem.getId(),
                owner.getId(),
                updateDto
        );

        assertEquals("Новая дрель", updatedItem.getName());
        assertEquals("Старая", updatedItem.getDescription());
    }
}
