package hr.egraovac.alg.algebrabooking.controller.MVC;

import hr.egraovac.alg.algebrabooking.configs.TestSecurityConfig;
import hr.egraovac.alg.algebrabooking.dto.RevenueStatsDTO;
import hr.egraovac.alg.algebrabooking.service.BookingService;
import hr.egraovac.alg.algebrabooking.service.UserService;
import hr.egraovac.alg.algebrabooking.utils.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ManagerStatsController.class)
@Import(TestSecurityConfig.class)
class ManagerStatsControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private BookingService bookingService;

  @MockitoBean
  private UserService userService;

  @MockitoBean
  private JwtUtil jwtUtil;

  private List<RevenueStatsDTO> monthlyRevenueList;

  @BeforeEach
  void setUp() {
    // Prepare test data
    monthlyRevenueList = new ArrayList<>();

    RevenueStatsDTO jan = new RevenueStatsDTO();
    jan.setMonth(1);
    jan.setRevenue(BigDecimal.valueOf(15000.00));
    jan.setBookingCount(50L);

    RevenueStatsDTO feb = new RevenueStatsDTO();
    feb.setMonth(2);
    feb.setRevenue(BigDecimal.valueOf(18000.00));
    feb.setBookingCount(60L);

    RevenueStatsDTO mar = new RevenueStatsDTO();
    mar.setMonth(3);
    mar.setRevenue(BigDecimal.valueOf(22000.00));
    mar.setBookingCount(75L);

    monthlyRevenueList.add(jan);
    monthlyRevenueList.add(feb);
    monthlyRevenueList.add(mar);
  }

  // ==================== MANAGER ACCESS ====================

  @Test
  @WithMockUser(username = "manager", roles = "MANAGER")
  void whenManagerAccessesRevenueReport_thenSuccess() throws Exception {
    when(bookingService.getMonthlyRevenue()).thenReturn(monthlyRevenueList);

    mockMvc.perform(get("/manager/stats/revenue"))
        .andExpect(status().isOk())
        .andExpect(view().name("reports/revenue-report"))
        .andExpect(model().attributeExists("monthlyRevenue"))
        .andExpect(model().attribute("monthlyRevenue", hasSize(3)))
        .andExpect(model().attribute("monthlyRevenue", monthlyRevenueList));

    verify(bookingService, times(1)).getMonthlyRevenue();
  }

  @Test
  @WithMockUser(username = "manager", roles = "MANAGER")
  void whenManagerAccessesRevenueReport_thenModelContainsCorrectData() throws Exception {
    when(bookingService.getMonthlyRevenue()).thenReturn(monthlyRevenueList);

    mockMvc.perform(get("/manager/stats/revenue"))
        .andExpect(status().isOk())
        .andExpect(model().attribute("monthlyRevenue", hasItem(
            allOf(
                hasProperty("month", is(1)),
                hasProperty("revenue", is(BigDecimal.valueOf(15000.00))),
                hasProperty("bookingCount", is(50L))
            )
        )))
        .andExpect(model().attribute("monthlyRevenue", hasItem(
            allOf(
                hasProperty("month", is(2)),
                hasProperty("revenue", is(BigDecimal.valueOf(18000.00))),
                hasProperty("bookingCount", is(60L))
            )
        )))
        .andExpect(model().attribute("monthlyRevenue", hasItem(
            allOf(
                hasProperty("month", is(3)),
                hasProperty("revenue", is(BigDecimal.valueOf(22000.00))),
                hasProperty("bookingCount", is(75L))
            )
        )));

    verify(bookingService, times(1)).getMonthlyRevenue();
  }

  @Test
  @WithMockUser(username = "manager", roles = "MANAGER")
  void whenManagerAccessesRevenueReport_WithEmptyData_thenSuccess() throws Exception {
    when(bookingService.getMonthlyRevenue()).thenReturn(new ArrayList<>());

    mockMvc.perform(get("/manager/stats/revenue"))
        .andExpect(status().isOk())
        .andExpect(view().name("reports/revenue-report"))
        .andExpect(model().attributeExists("monthlyRevenue"))
        .andExpect(model().attribute("monthlyRevenue", hasSize(0)));

    verify(bookingService, times(1)).getMonthlyRevenue();
  }

  @Test
  @WithMockUser(username = "manager", roles = "MANAGER")
  void whenManagerAccessesRevenueReport_WithSingleMonth_thenSuccess() throws Exception {
    List<RevenueStatsDTO> singleMonth = new ArrayList<>();
    singleMonth.add(monthlyRevenueList.getFirst());

    when(bookingService.getMonthlyRevenue()).thenReturn(singleMonth);

    mockMvc.perform(get("/manager/stats/revenue"))
        .andExpect(status().isOk())
        .andExpect(view().name("reports/revenue-report"))
        .andExpect(model().attribute("monthlyRevenue", hasSize(1)));

    verify(bookingService, times(1)).getMonthlyRevenue();
  }

  // ==================== RECEPTIONIST ACCESS ====================

  @Test
  @WithMockUser(username = "receptionist", roles = "RECEPTIONIST")
  void whenReceptionistAccessesRevenueReport_thenForbidden() throws Exception {
    mockMvc.perform(get("/manager/stats/revenue"))
        .andExpect(status().isForbidden());

    verify(bookingService, never()).getMonthlyRevenue();
  }

  // ==================== GUEST ACCESS ====================

  @Test
  @WithMockUser(username = "guest", roles = "GUEST")
  void whenGuestAccessesRevenueReport_thenForbidden() throws Exception {
    mockMvc.perform(get("/manager/stats/revenue"))
        .andExpect(status().isForbidden());

    verify(bookingService, never()).getMonthlyRevenue();
  }

  // ==================== UNAUTHENTICATED ACCESS ====================

  @Test
  void whenUnauthenticatedUserAccessesRevenueReport_thenRedirectToLogin() throws Exception {
    mockMvc.perform(get("/manager/stats/revenue"))
        .andExpect(status().is3xxRedirection());
    verify(bookingService, never()).getMonthlyRevenue();
  }

  // ==================== SERVICE EXCEPTION HANDLING ====================

  @Test
  @WithMockUser(username = "manager", roles = "MANAGER")
  void whenServiceThrowsException_thenInternalServerError() throws Exception {
    when(bookingService.getMonthlyRevenue())
        .thenThrow(new RuntimeException("Database connection error"));

    mockMvc.perform(get("/manager/stats/revenue"))
        .andExpect(status().is5xxServerError());

    verify(bookingService, times(1)).getMonthlyRevenue();
  }

  // ==================== EDGE CASES ====================

  @Test
  @WithMockUser(username = "manager", roles = "MANAGER")
  void whenManagerAccessesRevenueReport_WithZeroRevenue_thenSuccess() throws Exception {
    RevenueStatsDTO zeroRevenue = new RevenueStatsDTO();
    zeroRevenue.setMonth(1);
    zeroRevenue.setRevenue(BigDecimal.ZERO);
    zeroRevenue.setBookingCount(0L);

    List<RevenueStatsDTO> zeroRevenueList = new ArrayList<>();
    zeroRevenueList.add(zeroRevenue);

    when(bookingService.getMonthlyRevenue()).thenReturn(zeroRevenueList);

    mockMvc.perform(get("/manager/stats/revenue"))
        .andExpect(status().isOk())
        .andExpect(model().attribute("monthlyRevenue", hasItem(
            allOf(
                hasProperty("month", is(1)),
                hasProperty("revenue", is(BigDecimal.ZERO)),
                hasProperty("bookingCount", is(0L))
            )
        )));

    verify(bookingService, times(1)).getMonthlyRevenue();
  }

  @Test
  @WithMockUser(username = "manager", roles = "MANAGER")
  void whenManagerAccessesRevenueReport_WithLargeDataSet_thenSuccess() throws Exception {
    List<RevenueStatsDTO> largeDataSet = new ArrayList<>();

    // Create 12 months of data
    for (int i = 1; i <= 12; i++) {
      RevenueStatsDTO dto = new RevenueStatsDTO();
      dto.setMonth(i);
      dto.setRevenue(BigDecimal.valueOf(10000 + (i * 1000)));
      dto.setBookingCount(50L + (i * 5));
      largeDataSet.add(dto);
    }

    when(bookingService.getMonthlyRevenue()).thenReturn(largeDataSet);

    mockMvc.perform(get("/manager/stats/revenue"))
        .andExpect(status().isOk())
        .andExpect(view().name("reports/revenue-report"))
        .andExpect(model().attribute("monthlyRevenue", hasSize(12)));

    verify(bookingService, times(1)).getMonthlyRevenue();
  }

  @Test
  @WithMockUser(username = "manager", roles = "MANAGER")
  void whenManagerAccessesRevenueReport_WithVeryHighRevenue_thenSuccess() throws Exception {
    RevenueStatsDTO highRevenue = new RevenueStatsDTO();
    highRevenue.setMonth(1);
    highRevenue.setRevenue(BigDecimal.valueOf(999999.99));
    highRevenue.setBookingCount(1000L);

    List<RevenueStatsDTO> highRevenueList = new ArrayList<>();
    highRevenueList.add(highRevenue);

    when(bookingService.getMonthlyRevenue()).thenReturn(highRevenueList);

    mockMvc.perform(get("/manager/stats/revenue"))
        .andExpect(status().isOk())
        .andExpect(model().attribute("monthlyRevenue", hasItem(
            hasProperty("revenue", is(BigDecimal.valueOf(999999.99)))
        )));

    verify(bookingService, times(1)).getMonthlyRevenue();
  }

  // ==================== MULTIPLE CALLS ====================

  @Test
  @WithMockUser(username = "manager", roles = "MANAGER")
  void whenManagerAccessesRevenueReportMultipleTimes_thenServiceCalledEachTime() throws Exception {
    when(bookingService.getMonthlyRevenue()).thenReturn(monthlyRevenueList);

    // First call
    mockMvc.perform(get("/manager/stats/revenue"))
        .andExpect(status().isOk());

    // Second call
    mockMvc.perform(get("/manager/stats/revenue"))
        .andExpect(status().isOk());

    // Third call
    mockMvc.perform(get("/manager/stats/revenue"))
        .andExpect(status().isOk());

    verify(bookingService, times(3)).getMonthlyRevenue();
  }

}