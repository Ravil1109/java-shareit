package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dao.CommentRepository;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ItemServiceTestImpl {

    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;

    private ItemServiceImpl itemService;

    private User owner;

    @BeforeEach
    void setUp() {
        itemService = new ItemServiceImpl(itemRepository, userRepository, bookingRepository, commentRepository);
        owner = User.builder().id(1L).name("Ivan").email("ivan@mail.ru").build();
    }

    @Test
    void shouldCreateItem() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> {
            Item item = invocation.getArgument(0);
            item.setId(1L);
            return item;
        });

        ItemDto itemDto = ItemDto.builder()
                .name("Дрель")
                .description("Мощная дрель")
                .available(true)
                .build();

        ItemDto createdItem = itemService.create(itemDto, owner.getId());

        assertNotNull(createdItem.getId());
        assertEquals("Дрель", createdItem.getName());
        assertEquals("Мощная дрель", createdItem.getDescription());
        assertTrue(createdItem.getAvailable());
    }

    @Test
    void shouldThrowExceptionWhenNameIsBlank() {
        ItemDto itemDto = ItemDto.builder()
                .name("")
                .description("Описание")
                .available(true)
                .build();

        assertThrows(ValidationException.class, () -> itemService.create(itemDto, owner.getId()));
    }

    @Test
    void shouldThrowExceptionWhenDescriptionIsBlank() {
        ItemDto itemDto = ItemDto.builder()
                .name("Дрель")
                .description("")
                .available(true)
                .build();

        assertThrows(ValidationException.class, () -> itemService.create(itemDto, owner.getId()));
    }

    @Test
    void shouldThrowExceptionWhenAvailableIsNull() {
        ItemDto itemDto = ItemDto.builder()
                .name("Дрель")
                .description("Описание")
                .available(null)
                .build();

        assertThrows(ValidationException.class, () -> itemService.create(itemDto, owner.getId()));
    }

    @Test
    void shouldGetItemById() {
        Item item = Item.builder()
                .id(1L)
                .name("Дрель")
                .description("Мощная")
                .available(true)
                .owner(owner)
                .build();

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(commentRepository.findByItemId(1L)).thenReturn(List.of());
        when(bookingRepository.findByItemIdOrderByStartDesc(1L)).thenReturn(List.of());

        ItemDto foundItem = itemService.getById(1L, owner.getId());

        assertEquals(1L, foundItem.getId());
        assertEquals("Дрель", foundItem.getName());
    }

    @Test
    void shouldUpdateItem() {
        Item item = Item.builder()
                .id(1L)
                .name("Дрель")
                .description("Старая")
                .available(true)
                .owner(owner)
                .build();

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        ItemDto updateDto = ItemDto.builder()
                .name("Новая дрель")
                .build();

        ItemDto updatedItem = itemService.update(1L, owner.getId(), updateDto);

        assertEquals("Новая дрель", updatedItem.getName());
        assertEquals("Старая", updatedItem.getDescription());
    }
}
