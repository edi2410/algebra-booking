package hr.egraovac.alg.algebrabooking.controller.REST;

import hr.egraovac.alg.algebrabooking.dto.RoomDTO;
import hr.egraovac.alg.algebrabooking.mapper.RoomMapper;
import hr.egraovac.alg.algebrabooking.models.Room;
import hr.egraovac.alg.algebrabooking.service.RoomService;
import hr.egraovac.alg.algebrabooking.utils.enums.RoomType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomRestController {

  @Autowired
  private RoomService roomService;

  @GetMapping
  public ResponseEntity<List<RoomDTO>> getRooms(
      @RequestParam(required = false) RoomType roomType,
      @RequestParam(required = false) BigDecimal maxPrice,
      @RequestParam(required = false) Integer minCapacity,
      @RequestParam(required = false) LocalDate checkIn,
      @RequestParam(required = false) LocalDate checkOut) {

    List<Room> rooms = (roomType == null && maxPrice == null && minCapacity == null
        && checkIn == null && checkOut == null)
        ? roomService.findAvailableRooms()
        : roomService.searchRooms(roomType, maxPrice, minCapacity, checkIn, checkOut);

    return ResponseEntity.ok(RoomMapper.toDtoList(rooms));
  }

  @GetMapping("/{id}")
  public ResponseEntity<RoomDTO> getRoomById(@PathVariable Long id) {
    Room room = roomService.findById(id);
    if (room == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
    return ResponseEntity.ok(RoomMapper.toDto(room));
  }
}