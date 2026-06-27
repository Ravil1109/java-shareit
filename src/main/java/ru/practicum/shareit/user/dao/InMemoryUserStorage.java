package ru.practicum.shareit.user.dao;

import lombok.NoArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@NoArgsConstructor
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private Long nextId = 1L;

    @Override
    public User create(User user) {
        user.setId(nextId++);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        if (!users.containsKey(user.getId())) {
            throw new NotFoundException(
                    "Пользователь с id=" + user.getId() + " не найден"
            );
        }

        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User getById(Long id) {
        User user = users.get(id);

        if (user == null) {
            throw new NotFoundException(
                    "Пользователь с id=" + id + " не найден"
            );
        }

        return user;
    }

    @Override
    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void delete(Long id) {
        if (!users.containsKey(id)) {
            throw new NotFoundException(
                    "Пользователь с id=" + id + " не найден"
            );
        }

        users.remove(id);
    }
}
