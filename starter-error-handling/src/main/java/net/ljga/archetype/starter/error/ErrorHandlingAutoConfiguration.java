package net.ljga.archetype.starter.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@AutoConfiguration
@EnableConfigurationProperties(ErrorHandlingProperties.class)
@ConditionalOnBooleanProperty(value = "app.errors.enabled", matchIfMissing = true)
public class ErrorHandlingAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  List<ProblemDetailMapper> problemDetailMappers(List<ProblemDetailMapper> mappers) {
    // make it explicit: always inject a list (possibly empty)
    return mappers;
  }

  @RestControllerAdvice
  @Order(Ordered.HIGHEST_PRECEDENCE)
  @ConditionalOnClass(ProblemDetail.class)
  @AllArgsConstructor(access = AccessLevel.PACKAGE)
  static class ProblemDetailsAdvice {

    private final ErrorHandlingProperties props;
    private final List<ProblemDetailMapper> mappers;

    // 1) Extension point: let business mappers win first
    @ExceptionHandler(Throwable.class)
    ResponseEntity<@NonNull ProblemDetail> handleAny(Throwable ex, HttpServletRequest req) {
      for (ProblemDetailMapper mapper : mappers) {
        if (mapper.supports(ex)) {
          // Assume mapper produces a complete response
          return mapper.toResponse(ex);
        }
      }

      // 2) Platform defaults (non-business)
      if (ex instanceof MethodArgumentNotValidException manve) {
        return handleValidation(manve, req);
      }
      if (ex instanceof ConstraintViolationException cve) {
        return handleConstraintViolation(cve, req);
      }
      if (ex instanceof HttpMessageNotReadableException hmnre) {
        return handleNotReadable(hmnre, req);
      }
      if (ex instanceof MethodArgumentTypeMismatchException matme) {
        return handleTypeMismatch(matme, req);
      }
      if (ex instanceof MissingServletRequestParameterException msrpe) {
        return handleMissingParam(msrpe, req);
      }
      if (ex instanceof MissingRequestHeaderException mrhe) {
        return handleMissingHeader(mrhe, req);
      }

      // 3) Fallback (optional)
      if (props.fallbackEnabled()) {
        return handleFallback(ex, req);
      }

      // If fallback disabled, rethrow to let default handlers do their thing
      if (ex instanceof RuntimeException re) throw re;
      throw new RuntimeException(ex);
    }

    private ResponseEntity<@NonNull ProblemDetail> handleValidation(
        MethodArgumentNotValidException ex, HttpServletRequest req) {

      ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
      pd.setType(typeUri("/validation"));
      pd.setTitle("Validation failed");
      pd.setDetail("Request validation failed");
      pd.setProperty(
          "errors",
          ex.getBindingResult().getFieldErrors().stream()
              .map(
                  fe ->
                      Map.of(
                          "field", fe.getField(),
                          "message", fe.getDefaultMessage()))
              .toList());

      enrich(pd, req);
      return ResponseEntity.badRequest().body(pd);
    }

    private ResponseEntity<@NonNull ProblemDetail> handleConstraintViolation(
        ConstraintViolationException ex, HttpServletRequest req) {

      ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
      pd.setType(typeUri("/validation"));
      pd.setTitle("Validation failed");
      pd.setDetail("Constraint violations");

      pd.setProperty(
          "errors",
          ex.getConstraintViolations().stream()
              .map(
                  v ->
                      Map.of(
                          "path", v.getPropertyPath().toString(),
                          "message", v.getMessage()))
              .toList());

      enrich(pd, req);
      return ResponseEntity.badRequest().body(pd);
    }

    private ResponseEntity<@NonNull ProblemDetail> handleNotReadable(
        HttpMessageNotReadableException ex, HttpServletRequest req) {

      ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
      pd.setType(typeUri("/bad-request"));
      pd.setTitle("Malformed request");
      pd.setDetail("Request body could not be read");
      enrich(pd, req);
      return ResponseEntity.badRequest().body(pd);
    }

    private ResponseEntity<@NonNull ProblemDetail> handleTypeMismatch(
        MethodArgumentTypeMismatchException ex, HttpServletRequest req) {

      ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
      pd.setType(typeUri("/bad-request"));
      pd.setTitle("Invalid parameter");
      pd.setDetail("Request parameter has an invalid type");
      pd.setProperty("parameter", ex.getName());
      pd.setProperty("value", ex.getValue());
      enrich(pd, req);
      return ResponseEntity.badRequest().body(pd);
    }

    private ResponseEntity<@NonNull ProblemDetail> handleMissingParam(
        MissingServletRequestParameterException ex, HttpServletRequest req) {

      ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
      pd.setType(typeUri("/bad-request"));
      pd.setTitle("Missing parameter");
      pd.setDetail("A required request parameter is missing");
      pd.setProperty("parameter", ex.getParameterName());
      enrich(pd, req);
      return ResponseEntity.badRequest().body(pd);
    }

    private ResponseEntity<@NonNull ProblemDetail> handleMissingHeader(
        MissingRequestHeaderException ex, HttpServletRequest req) {

      ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
      pd.setType(typeUri("/bad-request"));
      pd.setTitle("Missing header");
      pd.setDetail("A required request header is missing");
      pd.setProperty("header", ex.getHeaderName());
      enrich(pd, req);
      return ResponseEntity.badRequest().body(pd);
    }

    private ResponseEntity<@NonNull ProblemDetail> handleFallback(
        Throwable ex, HttpServletRequest req) {

      ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
      pd.setType(typeUri("/internal"));
      pd.setTitle("Internal server error");
      pd.setDetail("An unexpected error occurred");
      enrich(pd, req);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(pd);
    }

    private URI typeUri(String suffix) {
      String base = props.typeBase();
      if (base.endsWith("/")) base = base.substring(0, base.length() - 1);
      return URI.create(base + suffix);
    }

    private void enrich(ProblemDetail pd, HttpServletRequest req) {
      if (props.includeInstance()) {
        pd.setInstance(URI.create(req.getRequestURI()));
      }
      if (props.includeCorrelationId()) {
        String cid = MDC.get(props.correlationMdcKey());
        if (cid != null && !cid.isBlank()) {
          pd.setProperty("correlationId", cid);
        }
      }
    }
  }
}
