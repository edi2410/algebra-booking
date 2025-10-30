package hr.egraovac.alg.algebrabooking.service;

import hr.egraovac.alg.algebrabooking.exception.RoomNotFoundException;
import hr.egraovac.alg.algebrabooking.models.Room;
import hr.egraovac.alg.algebrabooking.repository.BookingRepository;
import hr.egraovac.alg.algebrabooking.repository.RoomRepository;
import hr.egraovac.alg.algebrabooking.utils.enums.RoomStatus;
import hr.egraovac.alg.algebrabooking.utils.enums.RoomType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RoomServiceTest {

  @Mock
  private RoomRepository roomRepository;

  @Mock
  private BookingRepository bookingRepository;

  @InjectMocks
  private RoomService roomService;

  private Room room;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    room = new Room();
    room.setId(1L);
    room.setRoomNumber("101");
    room.setRoomType(RoomType.SINGLE);
    room.setPricePerNight(BigDecimal.valueOf(100));
    room.setCapacity(1);
    room.setStatus(RoomStatus.AVAILABLE);
  }


  @Test
  void findAvailableRooms_ShouldReturnAvailableRooms() {
    when(roomRepository.findByStatus(RoomStatus.AVAILABLE)).thenReturn(List.of(room));

    List<Room> result = roomService.findAvailableRooms();

    assertThat(result).containsExactly(room);
    verify(roomRepository).findByStatus(RoomStatus.AVAILABLE);
  }


  @Test
  void saveRoom_ShouldCallRepositorySave() {
    when(roomRepository.save(room)).thenReturn(room);

    Room result = roomService.saveRoom(room);

    verify(roomRepository, times(1)).save(room);
    assertThat(result).isEqualTo(room);
  }

  @Test
  void deleteRoom_ShouldCallRepositoryDelete() {
    roomService.deleteRoom(1L);

    verify(roomRepository, times(1)).deleteById(1L);
  }


  @Test
  void allRooms_ShouldReturnAllRooms() {
    when(roomRepository.findAll()).thenReturn(List.of(room));

    List<Room> result = roomService.allRooms();

    assertThat(result).containsExactly(room);
  }

  @Test
  void findById_WhenRoomExists_ShouldReturnRoom() {
    when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

    Room result = roomService.findById(1L);

    assertThat(result).isEqualTo(room);
  }

  @Test
  void findById_WhenRoomNotFound_ShouldThrowException() {
    when(roomRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> roomService.findById(99L))
        .isInstanceOf(RoomNotFoundException.class)
        .hasMessageContaining("Room not found with id: 99");
  }


  @Test
  void searchRooms_ShouldCallRepositoryAndReturnResults() {
    when(roomRepository.searchRooms(
        eq(RoomStatus.AVAILABLE),
        eq(RoomType.SINGLE),
        eq(BigDecimal.valueOf(150)),
        eq(1)
    )).thenReturn(List.of(room));

    List<Room> result = roomService.searchRooms(
        RoomStatus.AVAILABLE,
        RoomType.SINGLE,
        BigDecimal.valueOf(150),
        1,
        null,
        null
    );

    assertThat(result).containsExactly(room);
  }

  @Test
  void searchRooms_WithDates_ShouldFilterUnavailableRooms() {
    Room availableRoom = new Room();
    availableRoom.setId(2L);
    availableRoom.setRoomNumber("102");
    availableRoom.setStatus(RoomStatus.AVAILABLE);

    when(roomRepository.searchRooms(any(), any(), any(), any()))
        .thenReturn(List.of(room, availableRoom));

    // Room 1 is not available
    when(bookingRepository.existsOverlappingBooking(eq(1L), any(), any()))
        .thenReturn(true);
    // Room 2 is available
    when(bookingRepository.existsOverlappingBooking(eq(2L), any(), any()))
        .thenReturn(false);

    LocalDate checkIn = LocalDate.of(2025, 11, 1);
    LocalDate checkOut = LocalDate.of(2025, 11, 3);

    List<Room> result = roomService.searchRooms(
        RoomStatus.AVAILABLE,
        RoomType.SINGLE,
        BigDecimal.valueOf(200),
        1,
        checkIn,
        checkOut
    );

    assertThat(result).hasSize(1);
    assertThat(result.getFirst().getId()).isEqualTo(2L);
    verify(bookingRepository, times(2))
        .existsOverlappingBooking(anyLong(), eq(checkIn), eq(checkOut));
  }

  @Test
  void searchRooms_WithoutDates_ShouldSkipAvailabilityFilter() {
    when(roomRepository.searchRooms(any(), any(), any(), any()))
        .thenReturn(List.of(room));

    List<Room> result = roomService.searchRooms(
        RoomStatus.AVAILABLE,
        null,
        null,
        null,
        null,
        null
    );

    assertThat(result).containsExactly(room);
    verify(bookingRepository, never()).existsOverlappingBooking(anyLong(), any(), any());
  }


}
