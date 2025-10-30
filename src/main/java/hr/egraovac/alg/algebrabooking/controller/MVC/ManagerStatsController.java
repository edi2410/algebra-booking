package hr.egraovac.alg.algebrabooking.controller.MVC;

import hr.egraovac.alg.algebrabooking.dto.RevenueStatsDTO;
import hr.egraovac.alg.algebrabooking.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/manager/stats")
public class ManagerStatsController {


  @Autowired
  BookingService bookingService;

  @GetMapping("/revenue")
  @PreAuthorize("hasRole('MANAGER')")
  public String getRevenueReport(Model model) {
    List<RevenueStatsDTO> monthlyRevenue = bookingService.getMonthlyRevenue();
    model.addAttribute("monthlyRevenue", monthlyRevenue);
    return "reports/revenue-report";
  }
}
