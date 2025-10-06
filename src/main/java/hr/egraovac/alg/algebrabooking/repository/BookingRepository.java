package hr.egraovac.alg.algebrabooking.repository;

import hr.egraovac.alg.algebrabooking.models.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

  @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Booking b " +
      "WHERE b.room.id = :roomId " +
      "AND b.status NOT IN ('CANCELLED', 'CHECKED_OUT') " +
      "AND NOT (b.checkOutDate <= :checkIn OR b.checkInDate >= :checkOut)")
  boolean existsOverlappingBooking(
      @Param("roomId") Long roomId,
      @Param("checkIn") LocalDate checkIn,
      @Param("checkOut") LocalDate checkOut
  );
}