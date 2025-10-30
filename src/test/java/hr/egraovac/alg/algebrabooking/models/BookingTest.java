package hr.egraovac.alg.algebrabooking.models;

import hr.egraovac.alg.algebrabooking.utils.enums.BookingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BookingTest {

  private Booking booking;
  private User guest;
  private Room room;

  @BeforeEach
  void setUp() {
    guest = new User();
    guest.setId(1L);
    guest.setUsername("testguest");

    room = new Room();
    room.setId(10L);
    room.setRoomNumber("101");

    booking = new Booking();
  }

  @Test
  void testNoArgsConstructor() {
    Booking newBooking = new Booking();

    assertNotNull(newBooking);
    assertNull(newBooking.getId());
    assertNull(newBooking.getGuest());
    assertNull(newBooking.getRoom());
  }

  @Test
  void testAllArgsConstructor() {
    LocalDate checkIn = LocalDate.of(2025, 11, 1);
    LocalDate checkOut = LocalDate.of(2025, 11, 5);
    BigDecimal price = new BigDecimal("500.00");
    LocalDateTime created = LocalDateTime.now();
    List<BookingReminder> reminders = new ArrayList<>();

    Booking newBooking = new Booking(1L, guest, room, checkIn, checkOut,
        price, BookingStatus.CONFIRMED, "Late check-in", created, reminders);

    assertEquals(1L, newBooking.getId());
    assertEquals(guest, newBooking.getGuest());
    assertEquals(room, newBooking.getRoom());
    assertEquals(checkIn, newBooking.getCheckInDate());
    assertEquals(checkOut, newBooking.getCheckOutDate());
    assertEquals(price, newBooking.getTotalPrice());
    assertEquals(BookingStatus.CONFIRMED, newBooking.getStatus());
    assertEquals("Late check-in", newBooking.getSpecialRequests());
  }

  @Test
  void testSettersAndGetters() {
    LocalDate checkIn = LocalDate.of(2025, 11, 1);
    LocalDate checkOut = LocalDate.of(2025, 11, 5);
    BigDecimal price = new BigDecimal("500.00");

    booking.setId(100L);
    booking.setGuest(guest);
    booking.setRoom(room);
    booking.setCheckInDate(checkIn);
    booking.setCheckOutDate(checkOut);
    booking.setTotalPrice(price);
    booking.setStatus(BookingStatus.CONFIRMED);
    booking.setSpecialRequests("Extra towels");

    assertEquals(100L, booking.getId());
    assertEquals(guest, booking.getGuest());
    assertEquals(room, booking.getRoom());
    assertEquals(checkIn, booking.getCheckInDate());
    assertEquals(checkOut, booking.getCheckOutDate());
    assertEquals(price, booking.getTotalPrice());
    assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
    assertEquals("Extra towels", booking.getSpecialRequests());
  }

  @Test
  void testOnCreate_setsCreatedAtAndDefaultStatus() {
    LocalDateTime before = LocalDateTime.now().minusSeconds(1);

    booking.onCreate();

    assertNotNull(booking.getCreatedAt());
    assertTrue(booking.getCreatedAt().isAfter(before));
    assertEquals(BookingStatus.PENDING, booking.getStatus());
  }

  @Test
  void testOnCreate_doesNotOverrideExistingStatus() {
    booking.setStatus(BookingStatus.CONFIRMED);

    booking.onCreate();

    assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
  }

  @Test
  void testOnCreate_setsStatusToPendingWhenNull() {
    booking.setStatus(null);

    booking.onCreate();

    assertEquals(BookingStatus.PENDING, booking.getStatus());
  }


  @Test
  void testCreatedAtCanBeSetManually() {
    LocalDateTime customTime = LocalDateTime.of(2025, 1, 1, 10, 0);

    booking.setCreatedAt(customTime);

    assertEquals(customTime, booking.getCreatedAt());
  }
}