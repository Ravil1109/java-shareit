package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {

    ItemDto create(ItemDto itemDto, Long ownerId);

    ItemDto update(Long itemId, Long ownerId, ItemDto itemDto);

    ItemDto getById(Long itemId, Long userId);

    List<ItemDto> getOwnerItems(Long ownerId);

    List<ItemDto> search(String text);

    CommentDto addComment(Long itemId, Long userId, CommentDto commentDto);
}
