package net.ljga.archetype.starter.error;

import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

public interface ProblemDetailMapper {
  boolean supports(Throwable ex);

  ResponseEntity<ProblemDetail> toResponse(Throwable ex);
}
