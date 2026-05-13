package com.carmeet.ms_auth_user.exception;

import com.carmeet.ms_auth_user.dto.ApiResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.FieldError;
import org.springframework.security.access.AccessDeniedException;

import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ðŸ”´ VALIDACIÃ“N
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidation(MethodArgumentNotValidException ex) {

        Map<String, String> errores = new HashMap<>();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errores.put(error.getField(), error.getDefaultMessage());
        }

        return ResponseEntity.badRequest().body(
                ApiResponse.builder()
                        .success(false)
                        .message("ValidaciÃ³n fallida")
                        .error(errores)
                        .build());
    }

    // ðŸ” 403
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handle403(Exception ex) {
        return ResponseEntity.status(403).body(
                ApiResponse.builder()
                        .success(false)
                        .message("Acceso denegado")
                        .build());
    }

    // ðŸ”Ž 401 (login fallido)
    @ExceptionHandler(org.springframework.security.authentication.BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadCredentials(Exception ex) {
        return ResponseEntity.status(401).body(
                ApiResponse.builder()
                        .success(false)
                        .message("Credenciales invÃ¡lidas")
                        .build());
    }

    // ðŸ”Ž 404 / RuntimeException
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleNotFound(RuntimeException ex) {
        return ResponseEntity.status(404).body(
                ApiResponse.builder()
                        .success(false)
                        .message(ex.getMessage())
                        .build());
    }

    // ðŸ’¥ 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneral(Exception ex) {
        return ResponseEntity.status(500).body(
                ApiResponse.builder()
                        .success(false)
                        .message("Error interno")
                        .build());
    }
}
