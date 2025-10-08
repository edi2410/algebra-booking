package hr.egraovac.alg.algebrabooking.mapper;

import hr.egraovac.alg.algebrabooking.dto.HousekeepingTaskDTO;
import hr.egraovac.alg.algebrabooking.models.HousekeepingTask;

public class HousekeepingTaskMapper {

  public static HousekeepingTaskDTO toDTO(HousekeepingTask task) {
    if (task == null) return null;

    HousekeepingTaskDTO dto = new HousekeepingTaskDTO();
    dto.setId(task.getId());
    dto.setRoomId(task.getRoom() != null ? task.getRoom().getId() : null);
    dto.setTaskType(task.getTaskType());
    dto.setStatus(task.getStatus());
    dto.setAssignedToId(task.getAssignedTo() != null ? task.getAssignedTo().getId() : null);
    dto.setScheduledDate(task.getScheduledDate());
    dto.setCompletedAt(task.getCompletedAt());
    dto.setNotes(task.getNotes());

    return dto;
  }

  public static HousekeepingTask toEntity(HousekeepingTaskDTO dto) {
    if (dto == null) return null;

    HousekeepingTask task = new HousekeepingTask();
    task.setId(dto.getId());
    // room and assignedTo should be set manually after fetching entities
    task.setTaskType(dto.getTaskType());
    task.setStatus(dto.getStatus());
    task.setScheduledDate(dto.getScheduledDate());
    task.setCompletedAt(dto.getCompletedAt());
    task.setNotes(dto.getNotes());

    return task;
  }
}
