package hr.egraovac.alg.algebrabooking.service;

import hr.egraovac.alg.algebrabooking.models.Booking;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import hr.egraovac.alg.algebrabooking.exception.EmailSendingException;

@Service
@Slf4j
public class EmailService {

  @Autowired
  private JavaMailSender mailSender;

  @Autowired
  private TemplateEngine templateEngine;

  @Value("${app.mail.from}")
  private String fromEmail;

  @Value("${app.mail.support}")
  private String supportEmail;

  @Value("${housekeeping.email}")
  private String housekeepingEmail;

  public void sendHtmlEmail(String to, String subject, String htmlContent) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setFrom(fromEmail);
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setText(htmlContent, true); // true = HTML

      mailSender.send(message);
      log.info("HTML email sent to: {}", to);
    } catch (Exception e) {
      log.error("Failed to send HTML email to: {}", to, e);
      throw new EmailSendingException("Failed to send HTML email", e);
    }
  }

  public void sendBookingReminder(String email, Booking booking) {
    try {
      Context context = new Context();
      context.setVariable("booking", booking);
      context.setVariable("guest", booking.getGuest());
      context.setVariable("checkInDate", booking.getCheckInDate());

      String htmlContent = templateEngine.process("email/booking-reminder", context);

      sendHtmlEmail(
          email,
          "Reminder: Your Stay Tomorrow at Our Hotel",
          htmlContent
      );

      log.info("Booking reminder sent to: {}", email);
    } catch (Exception e) {
      log.error("Failed to send booking reminder to: {}", email, e);
    }
  }

  public void sendNewHousekeepingTask(Booking booking) {
    try {

      Context context = new Context();
      context.setVariable("booking", booking);
      context.setVariable("guest", booking.getGuest());
      context.setVariable("checkOutDate", booking.getCheckOutDate());

      String htmlContent = templateEngine.process("email/new-housekeeping-task", context);

      sendHtmlEmail(
          housekeepingEmail,
          "NEW cleaning task assigned",
          htmlContent
      );

      log.info("New task sent to: {}", housekeepingEmail);
    } catch (Exception e) {
      log.error("Failed to send new task to: {}", housekeepingEmail, e);
    }
  }
}
