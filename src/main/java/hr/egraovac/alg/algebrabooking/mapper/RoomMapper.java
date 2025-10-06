package hr.egraovac.alg.algebrabooking.mapper;


import hr.egraovac.alg.algebrabooking.dto.RoomDTO;
import hr.egraovac.alg.algebrabooking.models.Room;

import java.util.List;
import java.util.stream.Collectors;

public class RoomMapper {

  public static RoomDTO toDto(Room room) {
    if (room == null) return null;

    return new RoomDTO(
        room.getId(),
        room.getRoomNumber(),
        room.getRoomType(),
        room.getPricePerNight(),
        room.getCapacity(),
        room.getStatus(),
        room.getDescription()
    );
  }

  public static List<RoomDTO> toDtoList(List<Room> rooms) {
    return rooms.stream()
        .map(RoomMapper::toDto)
        .collect(Collectors.toList());
  }
}
