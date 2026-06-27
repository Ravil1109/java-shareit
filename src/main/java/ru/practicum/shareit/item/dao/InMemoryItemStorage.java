package ru.practicum.shareit.item.dao;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class InMemoryItemStorage implements ItemStorage {
    private final Map<Long, Item> items = new HashMap<>();
    private Long nextId = 1L;

    @Override
    public Item save(Item item) {
        item.setId(nextId++);
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item update(Item item) {
        if (!items.containsKey(item.getId())) {
            throw new NotFoundException(
                    "Вещь с id=" + item.getId() + " не найдена"
            );
        }

        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item findById(Long id) {
        Item item = items.get(id);

        if (item == null) {
            throw new NotFoundException(
                    "Вещь с id=" + id + " не найдена"
            );
        }

        return item;
    }

    @Override
    public List<Item> findAll() {
        return new ArrayList<>(items.values());
    }

}
