package com.yara.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ParticipanteGastoDTO {

    @NotNull(message = "El usuarioId es obligatorio")
    private Integer usuarioId;

    @Positive(message = "El monto debe ser mayor a 0")
    private BigDecimal monto;
}