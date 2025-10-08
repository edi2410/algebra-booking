package hr.egraovac.alg.algebrabooking.mapper;

import hr.egraovac.alg.algebrabooking.dto.BookingDTO;
import hr.egraovac.alg.algebrabooking.models.Booking;

public class BookingMapper {

  public static BookingDTO toDTO(Booking booking) {
    if (booking == null) return null;

    BookingDTO dto = new BookingDTO();
    dto.setId(booking.getId());
    dto.setGuestId(booking.getGuest() != null ? booking.getGuest().getId() : null);
    dto.setRoomId(booking.getRoom() != null ? booking.getRoom().getId() : null);
    dto.setCheckInDate(booking.getCheckInDate());
    dto.setCheckOutDate(booking.getCheckOutDate());
    dto.setTotalPrice(booking.getTotalPrice());
    dto.setStatus(booking.getStatus());
    dto.setSpecialRequests(booking.getSpecialRequests());

    return dto;
  }
}
