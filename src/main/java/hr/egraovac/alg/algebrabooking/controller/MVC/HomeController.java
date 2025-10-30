package hr.egraovac.alg.algebrabooking.controller.MVC;

import hr.egraovac.alg.algebrabooking.models.Room;
import hr.egraovac.alg.algebrabooking.models.User;
import hr.egraovac.alg.algebrabooking.service.RoomService;
import hr.egraovac.alg.algebrabooking.utils.AuthUtil;
import hr.egraovac.alg.algebrabooking.utils.enums.RoomStatus;
import hr.egraovac.alg.algebrabooking.utils.enums.RoomType;
import hr.egraovac.alg.algebrabooking.utils.enums.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
public class HomeController {

  @Autowired
  private RoomService roomService;

  @Autowired
  private AuthUtil authUtil;

  @GetMapping("/")
  public String home(
      @RequestParam(required = false) RoomType roomType,
      @RequestParam(required = false) BigDecimal maxPrice,
      @RequestParam(required = false) Integer minCapacity,
      @RequestParam(required = false) LocalDate checkIn,
      @RequestParam(required = false) LocalDate checkOut,
      Model model) {

    List<Room> rooms;

    if (authUtil.isAuthenticated()) {
      User user = authUtil.extractAuthorizedUser();

      if (user.getRoles().contains(UserRole.MANAGER)) {
        if (roomType == null && maxPrice == null && minCapacity == null
            && checkIn == null && checkOut == null) {
          rooms = roomService.allRooms();
        } else {
          rooms = roomService.searchRooms(null, roomType, maxPrice, minCapacity, checkIn, checkOut);
        }

      } else {

        if (roomType == null && maxPrice == null && minCapacity == null
            && checkIn == null && checkOut == null) {
          rooms = roomService.findAvailableRooms();
        } else {
          rooms = roomService.searchRooms(RoomStatus.AVAILABLE, roomType, maxPrice, minCapacity, checkIn, checkOut);
        }
      }

    } else {
      if (roomType == null && maxPrice == null && minCapacity == null
          && checkIn == null && checkOut == null) {
        rooms = roomService.findAvailableRooms();
      } else {
        rooms = roomService.searchRooms(RoomStatus.AVAILABLE, roomType, maxPrice, minCapacity, checkIn, checkOut);
      }
    }

    model.addAttribute("rooms", rooms);
    model.addAttribute("roomTypes", RoomType.values());
    model.addAttribute("selectedRoomType", roomType);
    model.addAttribute("maxPrice", maxPrice);
    model.addAttribute("minCapacity", minCapacity);
    model.addAttribute("checkIn", checkIn);
    model.addAttribute("checkOut", checkOut);

    return "index";
  }

}
