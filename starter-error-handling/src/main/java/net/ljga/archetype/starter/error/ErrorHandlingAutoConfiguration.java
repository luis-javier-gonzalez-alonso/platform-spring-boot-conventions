package net.ljga.archetype.starter.error;

import java.net.URI;
import java.util.Map;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@AutoConfiguration
public class ErrorHandlingAutoConfiguration {

  @RestControllerAdvice
  static class ProblemDetailsAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex) {
      ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
      pd.setType(URI.create("https://errors.ljga.net/validation"));
      pd.setTitle("Validation failed");
      pd.setDetail("Request validation failed");
      pd.setProperty(
          "errors",
          ex.getBindingResult().getFieldErrors().stream()
              .map(fe -> Map.of("field", fe.getField(), "message", fe.getDefaultMessage()))
              .toList());
      return ResponseEntity.badRequest().body(pd);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ProblemDetail> handleGeneric(Exception ex, WebRequest req) {
      ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
      pd.setType(URI.create("https://errors.ljga.net/internal"));
      pd.setTitle("Internal error");
      pd.setDetail("Unexpected error");
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(pd);
    }
  }

  // Optional bean for later extension points
  @Bean
  ErrorMarker errorMarker() {
    return new ErrorMarker();
  }

  static final class ErrorMarker {}
}
