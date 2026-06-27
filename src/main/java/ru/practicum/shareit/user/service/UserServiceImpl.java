package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.DuplicateEmailException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapping.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDto create(UserDto userDto) {
        if (userRepository.existsByEmailIgnoreCase(userDto.getEmail())) {
            throw new DuplicateEmailException(
                    "Пользователь с email " + userDto.getEmail() + " уже существует"
            );
        }

        User user = UserMapper.toUser(userDto);

        User createdUser = userRepository.save(user);

        return UserMapper.toDto(createdUser);
    }

    @Override
    @Transactional
    public UserDto update(Long userId, UserDto userDto) {
        User existingUser = getUserOrThrow(userId);

        if (userDto.getEmail() != null && !userDto.getEmail().equalsIgnoreCase(existingUser.getEmail())
                && userRepository.existsByEmailIgnoreCase(userDto.getEmail())) {
            throw new DuplicateEmailException(
                    "Пользователь с email " + userDto.getEmail() + " уже существует"
            );
        }

        if (userDto.getName() != null) {
            existingUser.setName(userDto.getName());
        }

        if (userDto.getEmail() != null) {
            existingUser.setEmail(userDto.getEmail());
        }

        return UserMapper.toDto(existingUser);
    }

    @Override
    public UserDto getById(Long userId) {
        return UserMapper.toDto(getUserOrThrow(userId));
    }

    @Override
    public List<UserDto> getAll() {
        return userRepository.findAll().stream()
                .map(UserMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void delete(Long userId) {
        getUserOrThrow(userId);
        userRepository.deleteById(userId);
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        "Пользователь с id=" + userId + " не найден"
                ));
    }
}
