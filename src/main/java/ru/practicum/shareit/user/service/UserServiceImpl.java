package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.DuplicateEmailException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dao.UserStorage;
import ru.practicum.shareit.user.mapping.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;

    @Override
    public UserDto create(UserDto userDto) {
        validateEmailUniqueness(null, userDto.getEmail());

        User user = UserMapper.toUser(userDto);


        User createdUser = userStorage.create(user);

        return UserMapper.toDto(createdUser);
    }

    @Override
    public UserDto update(Long userId, UserDto userDto) {
        User existingUser = userStorage.getById(userId);

        validateEmailUniqueness(userId, userDto.getEmail());

        if (userDto.getName() != null) {
            existingUser.setName(userDto.getName());
        }

        if (userDto.getEmail() != null) {
            existingUser.setEmail(userDto.getEmail());
        }

        User updatedUser = userStorage.update(existingUser);

        return UserMapper.toDto(updatedUser);
    }

    @Override
    public UserDto getById(Long userId) {
        return UserMapper.toDto(
                userStorage.getById(userId)
        );
    }

    @Override
    public List<UserDto> getAll() {
        return userStorage.getAll().stream()
                .map(UserMapper::toDto)
                .toList();
    }

    @Override
    public void delete(Long userId) {
        userStorage.delete(userId);
    }

    private void validateEmailUniqueness(Long currentUserId, String email) {
        boolean emailExists = userStorage.getAll().stream()
                .filter(user -> currentUserId == null || !currentUserId.equals(user.getId()))
                .anyMatch(user -> user.getEmail().equalsIgnoreCase(email));

        if (emailExists) {
            throw new DuplicateEmailException(
                    "Пользователь с email " + email + " уже существует"
            );
        }
    }
}
