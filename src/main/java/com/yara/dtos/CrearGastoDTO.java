package com.yara.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class CrearGastoDTO {

    @NotNull(message = "El grupoId es obligatorio")
    private Integer grupoId;

    @NotBlank(message = "La descripción es obligatoria")
    private String descripcion;

    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser mayor a 0")
    private BigDecimal monto;

    @NotBlank(message = "El tipo de división es obligatorio")
    private String tipoDivision;

    @Valid
    @NotEmpty(message = "Debe haber participantes")
    private List<ParticipanteGastoDTO> participantes;

    @NotNull(message = "El pagador es obligatorio")
    private Integer pagadoPorId;

    @Valid
    @NotEmpty(message = "Debe haber al menos una categoría")
    private List<CategoriaGastoDTO> categorias;
}