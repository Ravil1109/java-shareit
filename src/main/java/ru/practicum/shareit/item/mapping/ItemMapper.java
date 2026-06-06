package ru.practicum.shareit.item.mapping;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

public class ItemMapper {
    public ItemMapper() {
    }

    public static ItemDto toDto(Item item) {
        if (item == null) {
            return null;
        }

        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .requestId(item.getRequest() != null
                        ? item.getRequest().getId()
                        : null)
                .build();
    }

    public static Item toItem(ItemDto dto) {
        if (dto == null) {
            return null;
        }

        Item item = Item.builder()
                .id(dto.getId())
                .name(dto.getName())
                .description(dto.getDescription())
                .available(dto.getAvailable())
                .build();

        return item;
    }
}
