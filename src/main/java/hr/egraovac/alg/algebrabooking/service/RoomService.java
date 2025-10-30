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

  public Room saveRoom(Room room) {
    roomRepository.save(room);
    return room;
  }

  public void deleteRoom(Long id){
    roomRepository.deleteById(id);
  }

  public List<Room> allRooms(){
    return roomRepository.findAll();
  }

  public List<Room> searchRooms(RoomStatus roomStatus, RoomType roomType, BigDecimal maxPrice,
      Integer minCapacity, LocalDate checkIn, LocalDate checkOut) {

    List<Room> rooms = roomRepository.searchRooms(
        roomStatus,
        roomType,
        maxPrice,
        minCapacity
    );

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