package hr.egraovac.alg.algebrabooking.controller.REST;

import hr.egraovac.alg.algebrabooking.dto.RoomDTO;
import hr.egraovac.alg.algebrabooking.mapper.RoomMapper;
import hr.egraovac.alg.algebrabooking.models.Room;
import hr.egraovac.alg.algebrabooking.service.RoomService;
import hr.egraovac.alg.algebrabooking.utils.enums.RoomStatus;
import hr.egraovac.alg.algebrabooking.utils.enums.RoomType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@Tag(name = "Room Management", description = "APIs for managing hotel rooms")
public class RoomRestController {

  @Autowired
  private RoomService roomService;

  @GetMapping("/filter")
  @Operation(summary = "Search and filter rooms", description = "Search for available rooms with optional filters for type, price, capacity, and date range")
  public ResponseEntity<List<RoomDTO>> getRooms(
      @RequestParam(required = false) RoomType roomType,
      @RequestParam(required = false) BigDecimal maxPrice,
      @RequestParam(required = false) Integer minCapacity,
      @RequestParam(required = false) LocalDate checkIn,
      @RequestParam(required = false) LocalDate checkOut) {

    List<Room> rooms = (roomType == null && maxPrice == null && minCapacity == null
        && checkIn == null && checkOut == null)
        ? roomService.findAvailableRooms()
        : roomService.searchRooms(RoomStatus.AVAILABLE, roomType, maxPrice, minCapacity, checkIn, checkOut);

    return ResponseEntity.ok(RoomMapper.toDtoList(rooms));
  }

  @GetMapping
  @Operation(summary = "Get all rooms", description = "Retrieves a list of all rooms in the system")
  public ResponseEntity<List<RoomDTO>> getAllRooms() {
    List<Room> rooms = roomService.allRooms();
    return ResponseEntity.ok(RoomMapper.toDtoList(rooms));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get room by ID", description = "Retrieves detailed information about a specific room")
  public ResponseEntity<RoomDTO> getRoomById(@PathVariable Long id) {
    Room room = roomService.findById(id);
    if (room == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(RoomMapper.toDto(room));
  }

  @PostMapping
  @PreAuthorize("hasRole('MANAGER')")
  @Operation(summary = "Create new room", description = "Creates a new room in the system. Requires MANAGER role")
  public ResponseEntity<RoomDTO> createNewRoom(@RequestBody Room room) {
    try {
      Room savedRoom = roomService.saveRoom(room);
      return ResponseEntity.ok(RoomMapper.toDto(savedRoom));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('MANAGER')")
  @Operation(summary = "Update room", description = "Updates an existing room's details. Requires MANAGER role")
  public ResponseEntity<Room> updateRoom(@PathVariable Long id, @RequestBody Room roomDetails) {
    Room existingRoom = roomService.findById(id);
    if (existingRoom == null) {
      return ResponseEntity.notFound().build();
    }

    existingRoom.setRoomNumber(roomDetails.getRoomNumber());
    existingRoom.setRoomType(roomDetails.getRoomType());
    existingRoom.setPricePerNight(roomDetails.getPricePerNight());
    existingRoom.setCapacity(roomDetails.getCapacity());
    existingRoom.setStatus(roomDetails.getStatus());

    Room updatedRoom = roomService.saveRoom(existingRoom);
    return ResponseEntity.ok(updatedRoom);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('MANAGER')")
  @Operation(summary = "Delete room", description = "Deletes a room from the system. Requires MANAGER role")
  public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
    Room room = roomService.findById(id);
    if (room == null) {
      return ResponseEntity.notFound().build();
    }
    roomService.deleteRoom(id);
    return ResponseEntity.noContent().build();
  }
}