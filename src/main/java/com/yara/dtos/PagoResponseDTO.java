package com.yara.dtos;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class PagoResponseDTO {

    private Integer id;

    private String deudor;

    private String acreedor;

    private BigDecimal monto;

    private String metodoPago;

    private String estado;

    private LocalDateTime fecha;
}