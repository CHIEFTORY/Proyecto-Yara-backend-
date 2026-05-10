package com.yara.dtos;

import com.yara.enums.EstadoGasto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class GastoResponseDTO {

    private Integer id;

    private String descripcion;

    private BigDecimal montoTotal;

    private String grupoNombre;

    private String pagadoPor;

    private String tipoDivision;

    private EstadoGasto estado;

    private LocalDateTime fecha;

    private List<CategoriaResponseDTO> categorias;

    private Integer pagadoPorId;

    private List<ParticipanteGastoDTO> participantes;
}