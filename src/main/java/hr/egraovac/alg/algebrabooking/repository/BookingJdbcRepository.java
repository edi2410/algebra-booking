package hr.egraovac.alg.algebrabooking.repository;

import hr.egraovac.alg.algebrabooking.dto.RevenueStatsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BookingJdbcRepository {

  private final JdbcTemplate jdbcTemplate;

  @Autowired
  public BookingJdbcRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public List<RevenueStatsDTO> getMonthlyRevenue() {
    String sql = """
            SELECT 
                EXTRACT(YEAR FROM b.check_in_date) AS year,
                EXTRACT(MONTH FROM b.check_in_date) AS month,
                COALESCE(SUM(b.total_price), 0) AS total_revenue,
                COUNT(*) AS booking_count
            FROM bookings b
            WHERE b.status IN ('CONFIRMED', 'CHECKED_IN', 'CHECKED_OUT')
            GROUP BY EXTRACT(YEAR FROM b.check_in_date), EXTRACT(MONTH FROM b.check_in_date)
            ORDER BY EXTRACT(YEAR FROM b.check_in_date) DESC, EXTRACT(MONTH FROM b.check_in_date) DESC
        """;

    return jdbcTemplate.query(sql, (rs, rowNum) -> new RevenueStatsDTO(
        rs.getInt("year"),
        rs.getInt("month"),
        rs.getBigDecimal("total_revenue"),
        rs.getLong("booking_count")
    ));
  }
}
