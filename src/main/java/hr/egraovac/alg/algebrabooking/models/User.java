package hr.egraovac.alg.algebrabooking.models;

import hr.egraovac.alg.algebrabooking.utils.enums.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  private String username;

  @Column(nullable = false)
  private String password;

  @Column(nullable = false)
  private String email;

  private String fullName;
  private String phone;

  @CreatedDate
  private LocalDateTime createdAt;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
  @Enumerated(EnumType.STRING)
  @Column(name = "role")
  private Set<UserRole> roles = new HashSet<>();

  @OneToMany(mappedBy = "guest")
  private List<Booking> bookings;

  // Helper metode
  public void addRole(UserRole role) {
    this.roles.add(role);
  }

  public boolean hasRole(UserRole role) {
    return this.roles.contains(role);
  }

  public boolean isGuest() {
    return roles.contains(UserRole.GUEST);
  }

  public boolean isReceptionist() {
    return roles.contains(UserRole.RECEPTIONIST);
  }

  public boolean isManager() {
    return roles.contains(UserRole.MANAGER);
  }
}