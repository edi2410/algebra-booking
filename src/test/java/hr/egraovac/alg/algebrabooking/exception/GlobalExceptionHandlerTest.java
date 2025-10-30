package hr.egraovac.alg.algebrabooking.exception;

import hr.egraovac.alg.algebrabooking.utils.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ExceptionTestController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class GlobalExceptionHandlerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private JwtUtil jwtUtil;

  @Test
  void handleRuntimeException_ShouldReturn500() throws Exception {
    mockMvc.perform(get("/runtime"))
        .andExpect(status().isInternalServerError())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error").value("Runtime Error"))
        .andExpect(jsonPath("$.message").value("Test runtime exception"))
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  void handleGenericException_ShouldReturn500() throws Exception {
    mockMvc.perform(get("/generic"))
        .andExpect(status().isInternalServerError())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error").value("Unexpected Error"))
        .andExpect(jsonPath("$.message").value("Test generic exception"))
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  void handleAuthorizationDenied_ShouldReturn403() throws Exception {
    mockMvc.perform(get("/auth"))
        .andExpect(status().isForbidden())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error").value("Forbidden"))
        .andExpect(jsonPath("$.message").value("Access Denied"))
        .andExpect(jsonPath("$.timestamp").exists());
  }

}

