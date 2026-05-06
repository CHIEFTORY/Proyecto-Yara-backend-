package com.yara.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CrearPagoDTO {

    @NotNull(message = "El grupoId es obligatorio")
    private Integer grupoId;

    @NotNull(message = "El deudorId es obligatorio")
    private Integer deudorId;

    @NotNull(message = "El acreedorId es obligatorio")
    private Integer acreedorId;

    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser mayor a 0")
    private BigDecimal monto;

    @NotNull(message = "El metodoPagoId es obligatorio")
    private Integer metodoPagoId;
}