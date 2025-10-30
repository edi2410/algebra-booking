package hr.egraovac.alg.algebrabooking.controller.MVC;

import hr.egraovac.alg.algebrabooking.models.Room;
import hr.egraovac.alg.algebrabooking.service.RoomService;
import hr.egraovac.alg.algebrabooking.utils.enums.RoomStatus;
import hr.egraovac.alg.algebrabooking.utils.enums.RoomType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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


  @GetMapping("/new")
  @PreAuthorize("hasRole('MANAGER')")
  public String newRoom(Model model) {
    model.addAttribute("room", new Room());
    model.addAttribute("roomTypes", RoomType.values());
    model.addAttribute("roomStatuses", RoomStatus.values());
    model.addAttribute("isEdit", false);
    model.addAttribute("pageTitle", "Add New Room");
    return "room/form";
  }

  @GetMapping("/edit/{id}")
  @PreAuthorize("hasRole('MANAGER')")
  public String editRoom(@PathVariable Long id, Model model) {
    Room room = roomService.findById(id);
    if (room == null) {
      return "redirect:/";
    }
    model.addAttribute("room", room);
    model.addAttribute("roomTypes", RoomType.values());
    model.addAttribute("roomStatuses", RoomStatus.values());
    model.addAttribute("isEdit", true);
    model.addAttribute("pageTitle", "Edit Room #" + room.getRoomNumber());
    return "room/form";
  }

  @PostMapping("/save")
  @PreAuthorize("hasRole('MANAGER')")
  public String saveRoom(@ModelAttribute Room room, RedirectAttributes redirectAttributes) {
    try {

      roomService.saveRoom(room);
      redirectAttributes.addFlashAttribute("successMessage", "Room created successfully!");

      return "redirect:/room/" + room.getId();
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("errorMessage", "Error saving room: " + e.getMessage());
      return room.getId() != null ? "redirect:/room/edit/" + room.getId() : "redirect:/room/new";
    }
  }

  @PostMapping("/delete/{id}")
  @PreAuthorize("hasRole('MANAGER')")
  public String deleteRoom(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    try {
      roomService.deleteRoom(id);
      redirectAttributes.addFlashAttribute("successMessage", "Room deleted successfully!");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("errorMessage", "Error deleting room: " + e.getMessage());
    }
    return "redirect:/";
  }
}