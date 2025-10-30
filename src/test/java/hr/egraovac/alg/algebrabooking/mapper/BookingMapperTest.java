package hr.egraovac.alg.algebrabooking.mapper;

import hr.egraovac.alg.algebrabooking.dto.BookingDTO;
import hr.egraovac.alg.algebrabooking.models.Booking;
import hr.egraovac.alg.algebrabooking.models.Room;
import hr.egraovac.alg.algebrabooking.models.User;
import hr.egraovac.alg.algebrabooking.utils.enums.BookingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BookingMapperTest {

  private Booking booking;
  private User guest;
  private Room room;

  @BeforeEach
  void setUp() {
    guest = new User();
    guest.setId(1L);

    room = new Room();
    room.setId(10L);

    booking = new Booking();
    booking.setId(100L);
    booking.setGuest(guest);
    booking.setRoom(room);
    booking.setCheckInDate(LocalDate.of(2025, 11, 1));
    booking.setCheckOutDate(LocalDate.of(2025, 11, 5));
    booking.setTotalPrice(new BigDecimal("500.00"));
    booking.setStatus(BookingStatus.CONFIRMED);
    booking.setSpecialRequests("Late check-in");
  }

  @Test
  void toDTO_withValidBooking_shouldMapAllFields() {
    BookingDTO dto = BookingMapper.toDTO(booking);

    assertEquals(100L, dto.getId());
    assertEquals(1L, dto.getGuestId());
    assertEquals(10L, dto.getRoomId());
    assertEquals(LocalDate.of(2025, 11, 1), dto.getCheckInDate());
    assertEquals(LocalDate.of(2025, 11, 5), dto.getCheckOutDate());
    assertEquals(new BigDecimal("500.00"), dto.getTotalPrice());
    assertEquals(BookingStatus.CONFIRMED, dto.getStatus());
    assertEquals("Late check-in", dto.getSpecialRequests());
  }

  @Test
  void toDTO_withNullBooking_shouldReturnNull() {
    BookingDTO dto = BookingMapper.toDTO(null);

    assertNull(dto);
  }

  @Test
  void toDTO_withNullSpecialRequests_shouldMapCorrectly() {
    booking.setSpecialRequests(null);

    BookingDTO dto = BookingMapper.toDTO(booking);

    assertNull(dto.getSpecialRequests());
    assertEquals(100L, dto.getId());
  }

  @Test
  void toListOfDTO_withMultipleBookings_shouldMapAll() {
    Booking booking2 = new Booking();
    booking2.setId(200L);
    booking2.setGuest(guest);
    booking2.setRoom(room);
    booking2.setCheckInDate(LocalDate.of(2025, 12, 1));
    booking2.setCheckOutDate(LocalDate.of(2025, 12, 5));
    booking2.setTotalPrice(new BigDecimal("600.00"));
    booking2.setStatus(BookingStatus.PENDING);

    List<Booking> bookings = Arrays.asList(booking, booking2);

    List<BookingDTO> dtos = BookingMapper.toListOfDTO(bookings);

    assertEquals(2, dtos.size());
    assertEquals(100L, dtos.get(0).getId());
    assertEquals(200L, dtos.get(1).getId());
  }

  @Test
  void toListOfDTO_withEmptyList_shouldReturnEmptyList() {
    List<BookingDTO> dtos = BookingMapper.toListOfDTO(Collections.emptyList());

    assertNotNull(dtos);
    assertTrue(dtos.isEmpty());
  }

  @Test
  void toListOfDTO_withNullList_shouldReturnNull() {
    List<BookingDTO> dtos = BookingMapper.toListOfDTO(null);

    assertNull(dtos);
  }

  @Test
  void toListOfDTO_withSingleBooking_shouldReturnListWithOne() {
    List<Booking> bookings = Collections.singletonList(booking);

    List<BookingDTO> dtos = BookingMapper.toListOfDTO(bookings);

    assertEquals(1, dtos.size());
    assertEquals(100L, dtos.get(0).getId());
  }
}