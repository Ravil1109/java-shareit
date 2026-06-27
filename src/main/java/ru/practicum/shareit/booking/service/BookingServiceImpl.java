package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingDto;
import ru.practicum.shareit.booking.mapping.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BookingDto create(Long userId, NewBookingDto newBookingDto) {
        if (!newBookingDto.getEnd().isAfter(newBookingDto.getStart())) {
            throw new ValidationException("Дата окончания бронирования должна быть позже даты начала");
        }

        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        Item item = itemRepository.findById(newBookingDto.getItemId())
                .orElseThrow(() -> new NotFoundException(
                        "Вещь с id=" + newBookingDto.getItemId() + " не найдена"));

        if (!Boolean.TRUE.equals(item.getAvailable())) {
            throw new ValidationException("Вещь недоступна для бронирования");
        }

        if (item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Владелец не может забронировать собственную вещь");
        }

        Booking booking = Booking.builder()
                .start(newBookingDto.getStart())
                .end(newBookingDto.getEnd())
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();

        return BookingMapper.toDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingDto approve(Long userId, Long bookingId, boolean approved) {
        Booking booking = getBookingOrThrow(bookingId);

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Подтверждать бронирование может только владелец вещи");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Бронирование уже обработано");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);

        return BookingMapper.toDto(booking);
    }

    @Override
    public BookingDto getById(Long userId, Long bookingId) {
        Booking booking = getBookingOrThrow(bookingId);

        boolean isBooker = booking.getBooker().getId().equals(userId);
        boolean isOwner = booking.getItem().getOwner().getId().equals(userId);

        if (!isBooker && !isOwner) {
            throw new NotFoundException(
                    "Бронирование с id=" + bookingId + " не найдено для пользователя " + userId);
        }

        return BookingMapper.toDto(booking);
    }

    @Override
    public List<BookingDto> getByBooker(Long userId, BookingState state) {
        getUserOrThrow(userId);

        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = switch (state) {
            case CURRENT -> bookingRepository
                    .findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId, now, now);
            case PAST -> bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(userId, now);
            case FUTURE -> bookingRepository.findByBookerIdAndStartAfterOrderByStartDesc(userId, now);
            case WAITING -> bookingRepository
                    .findByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);
            case REJECTED -> bookingRepository
                    .findByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED);
            default -> bookingRepository.findByBookerIdOrderByStartDesc(userId);
        };

        return bookings.stream()
                .sorted(Comparator.comparing(Booking::getStart).reversed())
                .map(BookingMapper::toDto)
                .toList();
    }

    @Override
    public List<BookingDto> getByOwner(Long userId, BookingState state) {
        getUserOrThrow(userId);

        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = switch (state) {
            case CURRENT -> bookingRepository
                    .findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId, now, now);
            case PAST -> bookingRepository.findByItemOwnerIdAndEndBeforeOrderByStartDesc(userId, now);
            case FUTURE -> bookingRepository.findByItemOwnerIdAndStartAfterOrderByStartDesc(userId, now);
            case WAITING -> bookingRepository
                    .findByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);
            case REJECTED -> bookingRepository
                    .findByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED);
            default -> bookingRepository.findByItemOwnerIdOrderByStartDesc(userId);
        };

        return bookings.stream()
                .sorted(Comparator.comparing(Booking::getStart).reversed())
                .map(BookingMapper::toDto)
                .toList();
    }

    private Booking getBookingOrThrow(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(
                        "Бронирование с id=" + bookingId + " не найдено"));
    }

    private void getUserOrThrow(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
    }
}
