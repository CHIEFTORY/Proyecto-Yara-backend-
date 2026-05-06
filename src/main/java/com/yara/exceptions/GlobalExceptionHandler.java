package com.yara.exceptions;

import com.yara.dtos.ErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // =========================
    // VALIDACIONES (@Valid)
    // =========================
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> manejarValidaciones(
            MethodArgumentNotValidException ex
    ) {

        String mensaje = ex.getBindingResult()
                .getFieldError()
                .getDefaultMessage();

        ErrorResponseDTO error =
                ErrorResponseDTO.builder()
                        .mensaje(mensaje)
                        .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    // =========================
    // ERRORES DE NEGOCIO 🔥
    // =========================
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponseDTO> manejarBusiness(
            BusinessException ex
    ) {

        ErrorResponseDTO error =
                ErrorResponseDTO.builder()
                        .mensaje(ex.getMessage())
                        .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    // =========================
    // ERRORES GENERALES (500)
    // =========================
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> manejarGeneral(
            Exception ex
    ) {

        ErrorResponseDTO error =
                ErrorResponseDTO.builder()
                        .mensaje("Error interno del servidor")
                        .build();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error);
    }
}