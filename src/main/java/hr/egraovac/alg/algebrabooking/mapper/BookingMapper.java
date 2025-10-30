package hr.egraovac.alg.algebrabooking.mapper;

import hr.egraovac.alg.algebrabooking.dto.BookingDTO;
import hr.egraovac.alg.algebrabooking.models.Booking;

import java.util.List;
import java.util.stream.Collectors;

public class BookingMapper {

  public static BookingDTO toDTO(Booking booking) {
    if (booking == null) return null;

    BookingDTO dto = new BookingDTO();
    dto.setId(booking.getId());
    dto.setGuestId(booking.getGuest().getId());
    dto.setRoomId(booking.getRoom().getId());
    dto.setCheckInDate(booking.getCheckInDate());
    dto.setCheckOutDate(booking.getCheckOutDate());
    dto.setTotalPrice(booking.getTotalPrice());
    dto.setStatus(booking.getStatus());
    dto.setSpecialRequests(booking.getSpecialRequests());

    return dto;
  }

  public static List<BookingDTO> toListOfDTO(List<Booking> bookingList) {
    if (bookingList == null) return null;

    return bookingList.stream()
        .map(BookingMapper::toDTO)
        .collect(Collectors.toList());
  }
}
