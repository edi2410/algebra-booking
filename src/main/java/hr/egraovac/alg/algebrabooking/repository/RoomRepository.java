package hr.egraovac.alg.algebrabooking.repository;

import hr.egraovac.alg.algebrabooking.models.Room;
import hr.egraovac.alg.algebrabooking.utils.enums.RoomStatus;
import hr.egraovac.alg.algebrabooking.utils.enums.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

  List<Room> findByStatus(RoomStatus status);

  List<Room> findByRoomType(RoomType roomType);

  @Query("SELECT r FROM Room r " +
      "WHERE (:roomStatus IS NULL OR r.status = :roomStatus) " +
      "AND (:roomType IS NULL OR r.roomType = :roomType) " +
      "AND (:maxPrice IS NULL OR r.pricePerNight <= :maxPrice) " +
      "AND (:minCapacity IS NULL OR r.capacity >= :minCapacity)")
  List<Room> searchRooms(
      @Param("roomStatus") RoomStatus roomStatus,
      @Param("roomType") RoomType roomType,
      @Param("maxPrice") BigDecimal maxPrice,
      @Param("minCapacity") Integer minCapacity
  );
}