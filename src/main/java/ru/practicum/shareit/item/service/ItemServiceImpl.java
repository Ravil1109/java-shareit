package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dao.CommentRepository;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapping.CommentMapper;
import ru.practicum.shareit.item.mapping.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public ItemDto create(ItemDto itemDto, Long userId) {
        validateItem(itemDto);
        User owner = getUserOrThrow(userId);

        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(owner);

        Item savedItem = itemRepository.save(item);

        return ItemMapper.toDto(savedItem);
    }

    @Override
    @Transactional
    public ItemDto update(Long itemId, Long userId, ItemDto itemDto) {
        Item item = getItemOrThrow(itemId);

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

        return ItemMapper.toDto(item);
    }

    @Override
    public ItemDto getById(Long itemId, Long userId) {
        Item item = getItemOrThrow(itemId);

        ItemDto itemDto = ItemMapper.toDto(item);
        itemDto.setComments(getComments(itemId));

        if (item.getOwner().getId().equals(userId)) {
            attachBookingDates(itemDto, bookingRepository.findByItemIdOrderByStartDesc(itemId));
        }

        return itemDto;
    }

    @Override
    public List<ItemDto> getOwnerItems(Long userId) {
        List<Item> items = itemRepository.findByOwnerId(userId);

        List<Long> itemIds = items.stream().map(Item::getId).toList();

        Map<Long, List<Booking>> bookingsByItem = bookingRepository
                .findByItemIdInOrderByStartDesc(itemIds).stream()
                .collect(Collectors.groupingBy(b -> b.getItem().getId()));

        Map<Long, List<Comment>> commentsByItem = commentRepository
                .findByItemIdIn(itemIds).stream()
                .collect(Collectors.groupingBy(c -> c.getItem().getId()));

        return items.stream()
                .map(item -> {
                    ItemDto dto = ItemMapper.toDto(item);
                    dto.setComments(commentsByItem.getOrDefault(item.getId(), List.of()).stream()
                            .map(CommentMapper::toDto)
                            .toList());
                    attachBookingDates(dto, bookingsByItem.getOrDefault(item.getId(), List.of()));
                    return dto;
                })
                .toList();
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        return itemRepository.search(text.toLowerCase(Locale.ROOT)).stream()
                .map(ItemMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public CommentDto addComment(Long itemId, Long userId, CommentDto commentDto) {
        Item item = getItemOrThrow(itemId);
        User author = getUserOrThrow(userId);

        LocalDateTime now = LocalDateTime.now();
        boolean hasBooked = !bookingRepository
                .findByItemIdAndBookerIdAndEndBefore(itemId, userId, now).isEmpty();

        if (!hasBooked) {
            throw new ValidationException(
                    "Оставить отзыв может только пользователь, бравший вещь в аренду"
            );
        }

        Comment comment = Comment.builder()
                .text(commentDto.getText())
                .item(item)
                .author(author)
                .created(now)
                .build();

        return CommentMapper.toDto(commentRepository.save(comment));
    }

    private List<CommentDto> getComments(Long itemId) {
        return commentRepository.findByItemId(itemId).stream()
                .map(CommentMapper::toDto)
                .toList();
    }

    private void attachBookingDates(ItemDto dto, List<Booking> bookings) {
        LocalDateTime now = LocalDateTime.now();

        bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.APPROVED)
                .filter(b -> b.getStart().isBefore(now))
                .max(Comparator.comparing(Booking::getStart))
                .ifPresent(b -> dto.setLastBooking(
                        BookingShortDto.builder().id(b.getId()).bookerId(b.getBooker().getId()).build()));

        bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.APPROVED)
                .filter(b -> b.getStart().isAfter(now))
                .min(Comparator.comparing(Booking::getStart))
                .ifPresent(b -> dto.setNextBooking(
                        BookingShortDto.builder().id(b.getId()).bookerId(b.getBooker().getId()).build()));
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

    private Item getItemOrThrow(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + itemId + " не найдена"));
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
    }
}
