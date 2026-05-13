package com.carmeet.ms_notification_log.exception;

import com.carmeet.ms_notification_log.dto.ApiResponse;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errores.put(error.getField(), error.getDefaultMessage());
        }
        return ResponseEntity.badRequest().body(
                ApiResponse.builder().success(false).message("Validacion fallida").error(errores).build());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handle403(AccessDeniedException ex) {
        return ResponseEntity.status(403).body(
                ApiResponse.builder().success(false).message("Acceso denegado").build());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleConflict(DataIntegrityViolationException ex) {
        String root = ex.getRootCause() != null ? ex.getRootCause().getMessage() : ex.getMessage();
        String msg = (root != null && root.contains("Duplicate entry"))
                ? "Registro duplicado: ya existe un registro con ese valor"
                : "Error de integridad de datos";
        return ResponseEntity.status(409).body(
                ApiResponse.builder().success(false).message(msg).build());
    }

    @ExceptionHandler({java.util.NoSuchElementException.class, jakarta.persistence.EntityNotFoundException.class})
    public ResponseEntity<ApiResponse<Object>> handleNotFound(Exception ex) {
        String msg = ex.getMessage() != null ? ex.getMessage() : "Recurso no encontrado";
        return ResponseEntity.status(404).body(
                ApiResponse.builder().success(false).message(msg).build());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntime(RuntimeException ex) {
        return ResponseEntity.status(400).body(
                ApiResponse.builder().success(false).message(ex.getMessage()).build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneral(Exception ex) {
        return ResponseEntity.status(500).body(
                ApiResponse.builder().success(false).message("Error interno del servidor").build());
    }
}
