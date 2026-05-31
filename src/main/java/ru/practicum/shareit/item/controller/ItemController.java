package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;

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
            @RequestBody ItemDto itemDto) {

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
    public ItemDto getById(@PathVariable Long itemId) {
        return itemService.getById(itemId);
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
}