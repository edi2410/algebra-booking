package hr.egraovac.alg.algebrabooking.utils.enums;

public enum UserRole {
  GUEST("Guest - Can book rooms and view reservations"),
  RECEPTIONIST("Receptionist - Manages bookings and customer details"),
  MANAGER("Manager - Views occupancy metrics and revenue reports");

  private final String description;

  UserRole(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  // Za Spring Security
  public String getAuthority() {
    return "ROLE_" + this.name();
  }
}