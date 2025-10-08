package hr.egraovac.alg.algebrabooking.dto;

import hr.egraovac.alg.algebrabooking.utils.enums.TaskStatus;
import hr.egraovac.alg.algebrabooking.utils.enums.TaskType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HousekeepingTaskDTO {

  private Long id;
  private Long roomId;
  private TaskType taskType;
  private TaskStatus status;
  private Long assignedToId;
  private LocalDate scheduledDate;
  private LocalDateTime completedAt;
  private String notes;
}
