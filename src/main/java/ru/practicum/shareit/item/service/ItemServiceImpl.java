package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dao.ItemStorage;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapping.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dao.UserStorage;
import ru.practicum.shareit.user.model.User;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    @Override
    public ItemDto create(ItemDto itemDto, Long userId) {
        validateItem(itemDto);
        User owner = userStorage.getById(userId);

        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(owner);

        Item savedItem = itemStorage.save(item);

        return ItemMapper.toDto(savedItem);
    }

    @Override
    public ItemDto update(Long itemId, Long userId, ItemDto itemDto) {
        Item item = itemStorage.findById(itemId);

        if (!item.getOwner().getId().equals(userId)) {
            throw new ForbiddenException(
                    "Редактировать вещь может только владелец"
            );
        }

        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }

        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }

        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }

        Item updatedItem = itemStorage.update(item);

        return ItemMapper.toDto(updatedItem);
    }

    @Override
    public ItemDto getById(Long itemId) {
        Item item = itemStorage.findById(itemId);

        return ItemMapper.toDto(item);
    }

    @Override
    public List<ItemDto> getOwnerItems(Long userId) {
        return itemStorage.findAll().stream()
                .filter(item -> item.getOwner().getId().equals(userId))
                .map(ItemMapper::toDto)
                .toList();
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        String searchText = text.toLowerCase(Locale.ROOT);

        return itemStorage.findAll().stream()
                .filter(item -> Boolean.TRUE.equals(item.getAvailable()))
                .filter(item ->
                        item.getName().toLowerCase(Locale.ROOT).contains(searchText)
                                || item.getDescription().toLowerCase(Locale.ROOT).contains(searchText))
                .map(ItemMapper::toDto)
                .toList();
    }

    private void validateItem(ItemDto itemDto) {
        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            throw new ValidationException("Название вещи не может быть пустым");
        }

        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            throw new ValidationException("Описание вещи не может быть пустым");
        }

        if (itemDto.getAvailable() == null) {
            throw new ValidationException("Статус доступности обязателен");
        }
    }
}
