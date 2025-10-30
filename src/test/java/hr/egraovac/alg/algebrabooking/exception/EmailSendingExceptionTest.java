package hr.egraovac.alg.algebrabooking.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmailSendingExceptionTest {

  @Test
  void constructor_ShouldSetMessageAndCauseProperly() {
    String expectedMessage = "Failed to send email";
    Throwable expectedCause = new RuntimeException("SMTP error");

    EmailSendingException exception = new EmailSendingException(expectedMessage, expectedCause);

    assertThat(exception).isInstanceOf(RuntimeException.class);
    assertThat(exception.getMessage()).isEqualTo(expectedMessage);
    assertThat(exception.getCause()).isEqualTo(expectedCause);
  }
}
