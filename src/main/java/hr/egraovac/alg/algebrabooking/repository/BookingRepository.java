package hr.egraovac.alg.algebrabooking.repository;

import hr.egraovac.alg.algebrabooking.models.Booking;
import hr.egraovac.alg.algebrabooking.utils.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

  List<Booking> findByGuestId(Long guestId);

  @Query("""
      SELECT b FROM Booking b
      WHERE (:status IS NULL OR b.status = :status)
        AND (:guestName IS NULL OR :guestName = '' OR LOWER(b.guest.fullName) LIKE LOWER(CONCAT('%', :guestName, '%')))
      ORDER BY b.checkInDate DESC
      """)
  List<Booking> searchBookings(
      @Param("status") BookingStatus status,
      @Param("guestName") String guestName
  );

  /**
   * Check if there is any overlapping booking for a room within given dates
   */
  @Query("""
          SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END
          FROM Booking b
          WHERE b.room.id = :roomId
            AND b.status IN (
                hr.egraovac.alg.algebrabooking.utils.enums.BookingStatus.PENDING,
                hr.egraovac.alg.algebrabooking.utils.enums.BookingStatus.CONFIRMED,
                hr.egraovac.alg.algebrabooking.utils.enums.BookingStatus.CHECKED_IN
            )
            AND (b.checkInDate < :checkOut AND b.checkOutDate > :checkIn)
      """)
  boolean existsOverlappingBooking(
      @Param("roomId") Long roomId,
      @Param("checkIn") LocalDate checkIn,
      @Param("checkOut") LocalDate checkOut
  );

  long countByStatus(BookingStatus status);

  List<Booking> getBookingsByStatusAndCheckInDate(BookingStatus status, LocalDate checkInDate);
}
