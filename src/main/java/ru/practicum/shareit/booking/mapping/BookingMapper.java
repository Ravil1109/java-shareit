package ru.practicum.shareit.booking.mapping;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.mapping.ItemMapper;
import ru.practicum.shareit.user.mapping.UserMapper;

public class BookingMapper {
    private BookingMapper() {
    }

    public static BookingDto toDto(Booking booking) {
        if (booking == null) {
            return null;
        }

        return BookingDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .item(ItemMapper.toDto(booking.getItem()))
                .booker(UserMapper.toDto(booking.getBooker()))
                .status(booking.getStatus())
                .build();
    }
}
