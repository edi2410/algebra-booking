package hr.egraovac.alg.algebrabooking.controller.MVC;

import hr.egraovac.alg.algebrabooking.models.Room;
import hr.egraovac.alg.algebrabooking.service.RoomService;
import hr.egraovac.alg.algebrabooking.utils.enums.RoomStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/room")
public class RoomController {

  @Autowired
  private RoomService roomService;

  @GetMapping("/{id}")
  public String roomDetails(@PathVariable Long id, Model model) {
    Room room = roomService.findById(id);
    model.addAttribute("room", room);
    model.addAttribute("isRoomAvailable", room.getStatus() == RoomStatus.AVAILABLE);
    return "room/details";
  }
}
