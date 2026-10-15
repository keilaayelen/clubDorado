package com.clubdorado.config;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestControllerAdvice
public class ApiErrors {

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<Map<String, String>> badCredentials() {
    return ResponseEntity.status(401).body(Map.of("error", "Usuario o contraseña incorrectos"));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> invalid() {
    return ResponseEntity.badRequest().body(Map.of("error", "Completá usuario y contraseña"));
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<Map<String, String>> tooLarge() {
    return ResponseEntity.status(413).body(Map.of("error", "El archivo puede pesar hasta 40 MB"));
  }

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<Map<String, String>> status(ResponseStatusException exception) {
    String message = exception.getReason() == null ? "No se pudo completar" : exception.getReason();
    return ResponseEntity.status(exception.getStatusCode()).body(Map.of("error", message));
  }
}
