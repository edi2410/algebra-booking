package hr.egraovac.alg.algebrabooking.service;

import hr.egraovac.alg.algebrabooking.exception.RoomNotFoundException;
import hr.egraovac.alg.algebrabooking.models.Room;
import hr.egraovac.alg.algebrabooking.repository.BookingRepository;
import hr.egraovac.alg.algebrabooking.repository.RoomRepository;
import hr.egraovac.alg.algebrabooking.utils.enums.RoomStatus;
import hr.egraovac.alg.algebrabooking.utils.enums.RoomType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomService {

  @Autowired
  private RoomRepository roomRepository;

  @Autowired
  private BookingRepository bookingRepository;

  public List<Room> findAvailableRooms() {
    return roomRepository.findByStatus(RoomStatus.AVAILABLE);
  }

  public List<Room> searchRooms(RoomType roomType, BigDecimal maxPrice,
      Integer minCapacity, LocalDate checkIn, LocalDate checkOut) {

    List<Room> rooms = roomRepository.findByStatus(RoomStatus.AVAILABLE);

    if (roomType != null) {
      rooms = rooms.stream()
          .filter(room -> room.getRoomType().equals(roomType))
          .collect(Collectors.toList());
    }

    if (maxPrice != null) {
      rooms = rooms.stream()
          .filter(room -> room.getPricePerNight().compareTo(maxPrice) <= 0)
          .collect(Collectors.toList());
    }

    if (minCapacity != null) {
      rooms = rooms.stream()
          .filter(room -> room.getCapacity() >= minCapacity)
          .collect(Collectors.toList());
    }

    if (checkIn != null && checkOut != null) {
      rooms = rooms.stream()
          .filter(room -> isRoomAvailableForDates(room.getId(), checkIn, checkOut))
          .collect(Collectors.toList());
    }

    return rooms;
  }

  private boolean isRoomAvailableForDates(Long roomId, LocalDate checkIn, LocalDate checkOut) {
    return !bookingRepository.existsOverlappingBooking(roomId, checkIn, checkOut);
  }

  public Room findById(Long id) {
    return roomRepository.findById(id)
        .orElseThrow(() -> new RoomNotFoundException(id));
  }
}