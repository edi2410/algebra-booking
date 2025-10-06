package hr.egraovac.alg.algebrabooking.models;

import hr.egraovac.alg.algebrabooking.utils.enums.TaskStatus;
import hr.egraovac.alg.algebrabooking.utils.enums.TaskType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "housekeeping_tasks")
public class HousekeepingTask {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "room_id", nullable = false)
  private Room room;

  @Enumerated(EnumType.STRING)
  private TaskType taskType; // CLEANING, MAINTENANCE, INSPECTION

  @Enumerated(EnumType.STRING)
  private TaskStatus status; // PENDING, IN_PROGRESS, COMPLETED

  @ManyToOne
  @JoinColumn(name = "assigned_to")
  private User assignedTo; // staff member

  private LocalDate scheduledDate;
  private LocalDateTime completedAt;
  private String notes;
}

