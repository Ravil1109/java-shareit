package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    /**
     * Добавление новой вещи
     */
    @PostMapping
    public ItemDto create(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @Valid @RequestBody ItemDto itemDto) {

        return itemService.create(itemDto, userId);
    }

    /**
     * Редактирование вещи
     */
    @PatchMapping("/{itemId}")
    public ItemDto update(
            @PathVariable Long itemId,
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestBody ItemDto itemDto) {

        return itemService.update(itemId, userId, itemDto);
    }

    /**
     * Просмотр информации о конкретной вещи по её идентификатору
     */
    @GetMapping("/{itemId}")
    public ItemDto getById(
            @PathVariable Long itemId,
            @RequestHeader("X-Sharer-User-Id") Long userId) {

        return itemService.getById(itemId, userId);
    }

    /**
     * Просмотр владельцем списка всех его вещей с указанием названия и описания для каждой из них
     */
    @GetMapping
    public List<ItemDto> getOwnerItems(
            @RequestHeader("X-Sharer-User-Id") Long userId) {

        return itemService.getOwnerItems(userId);
    }

    /**
     * Поиск вещи потенциальным арендатором
     */
    @GetMapping("/search")
    public List<ItemDto> search(
            @RequestParam String text) {

        return itemService.search(text);
    }

    /**
     * Добавление комментария к вещи
     */
    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(
            @PathVariable Long itemId,
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @Valid @RequestBody CommentDto commentDto) {

        return itemService.addComment(itemId, userId, commentDto);
    }
}
